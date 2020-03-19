/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.unchai.maven.checkstyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

@Mojo(name = "checkstyle-github")
public class MainMojo extends AbstractMojo {
    @Parameter(property = "github.endpoint", defaultValue = "https://api.github.com")
    String ghEndpoint;

    @Parameter(property = "github.token", required = true)
    String ghToken;

    @Parameter(property = "github.repository", required = true)
    String ghRepository;

    @Parameter(property = "github.pullRequest", required = true)
    Integer ghPullRequest;

    @Parameter(defaultValue = "${project.basedir}", required = true, readonly = true)
    File projectBasedir;

    @Parameter(property = "checkstyle-github.configLocation", required = true)
    String configLocation;

    @Override
    public void execute() throws MojoExecutionException {
        final GithubHelper githubHelper;

        try {
            final GitHub github = new GitHubBuilder().withEndpoint(ghEndpoint).withOAuthToken(ghToken).build();
            githubHelper = new GithubHelper(github, ghRepository, ghPullRequest);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot connect github.");
        }

        githubHelper.changeStatus(GHCommitState.PENDING, null);

        final CheckstyleExecutor checkstyleExecutor = new CheckstyleExecutor(configLocation);

        final List<ChangedFile> changedFiles = githubHelper.listChangedFile();

        final Map<String, ChangedFile> changedFileMap =
            changedFiles
                .stream()
                .filter(changedFile -> changedFile.getPath().endsWith(".java"))
                .collect(Collectors.toMap(ChangedFile::getPath, Function.identity()));

        final List<CheckstyleError> checkstyleErrors =
            checkstyleExecutor.execute(projectBasedir, changedFiles.stream().map(ChangedFile::getPath).collect(Collectors.toList()))
                .stream()
                .filter(checkstyleError -> contains(checkstyleError, changedFileMap))
                .collect(Collectors.toList());

        final Collection<Comment> comments = buildComments(changedFileMap, checkstyleErrors);
        final Map<SeverityLevel, Integer> severityLevelCountMap = buildSeverityLevelCountMap(checkstyleErrors);

        githubHelper.removeAllComment();

        for (Comment comment : comments) {
            githubHelper.createComment(comment);
        }

        if (severityLevelCountMap.get(SeverityLevel.WARNING) > 0
            || severityLevelCountMap.get(SeverityLevel.ERROR) > 0) {
            githubHelper.changeStatus(
                GHCommitState.FAILURE,
                String.format(
                    "reported %d warnings, %d errors.",
                    severityLevelCountMap.get(SeverityLevel.WARNING),
                    severityLevelCountMap.get(SeverityLevel.ERROR)
                )
            );
        } else {
            githubHelper.changeStatus(GHCommitState.SUCCESS, "Good job! You kept all the rules.");
        }

    }

    private boolean contains(CheckstyleError checkstyleError, Map<String, ChangedFile> changedFileMap) {
        return changedFileMap.containsKey(checkstyleError.getPath())
            && changedFileMap.get(checkstyleError.getPath()).getLinePositionMap().containsKey(checkstyleError.getLine());
    }

    private Map<SeverityLevel, Integer> buildSeverityLevelCountMap(List<CheckstyleError> errors) {
        final Map<SeverityLevel, Integer> map = new EnumMap<>(SeverityLevel.class);

        for (SeverityLevel severityLevel : SeverityLevel.values()) {
            map.put(severityLevel, 0);
        }

        for (CheckstyleError error : errors) {
            map.put(error.getSeverityLevel(), map.get(error.getSeverityLevel()) + 1);
        }

        return map;
    }

    private Collection<Comment> buildComments(
        Map<String, ChangedFile> changedFileMap,
        List<CheckstyleError> errors
    ) {
        final Map<String, Comment> commentMap = new HashMap<>();

        for (CheckstyleError error : errors) {
            final String path = error.getPath();
            final String key = path + "|" + error.getLine();

            if (commentMap.containsKey(key)) {
                final Comment comment = commentMap.get(key);
                comment.getCheckstyleErrors().add(error);
            } else {
                final List<CheckstyleError> checkstyleErrors = new ArrayList<>();
                checkstyleErrors.add(error);

                final Comment comment = new Comment();
                comment.setPath(path);
                comment.setPosition(changedFileMap.get(path).getLinePositionMap().get(error.getLine()));
                comment.setCheckstyleErrors(checkstyleErrors);

                commentMap.put(key, comment);
            }
        }

        return commentMap.values();
    }
}

package com.github.unchai.maven.checkstyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

public class GithubHelper {
    private GHRepository repo;
    private GHPullRequest pr;

    public GithubHelper() {
    }

    Map<Integer, Integer> parsePatch(String patch) {
        int lineNo = 0;
        int pathNo = 0;

        final Map<Integer, Integer> map = new HashMap<>();
        for (String line : patch.split("\\r?\\n")) {
            if (line.startsWith("@@")) {
                final Matcher matcher = Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@.*").matcher(line);

                if (matcher.matches()) {
                    lineNo = Integer.parseInt(matcher.group(3));
                }
            } else if (line.startsWith(" ")) {
                lineNo++;
            } else if (line.startsWith("+")) {
                map.put(lineNo++, pathNo);
            }

            pathNo++;
        }

        return map;
    }

    void connect(String endpoint, String token, String repository, int pullRequest) throws MojoExecutionException {
        try {
            final GitHub github = new GitHubBuilder()
                .withEndpoint(endpoint)
                .withOAuthToken(token)
                .build();

            this.repo = github.getRepository(repository);
            this.pr = this.repo.getPullRequest(pullRequest);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    List<ChangedFile> listChangedFile() {
        final List<ChangedFile> linePositionMap = new ArrayList<>();

        for (GHPullRequestFileDetail fileDetail : this.pr.listFiles()) {
            if (fileDetail.getPatch() == null) {
                continue;
            }

            final Map<Integer, Integer> diffMap = parsePatch(fileDetail.getPatch());

            if (!diffMap.isEmpty()) {
                final ChangedFile changedFile = new ChangedFile();
                changedFile.setPath(fileDetail.getFilename());
                changedFile.setLinePositionMap(diffMap);

                linePositionMap.add(changedFile);
            }
        }

        return linePositionMap;
    }
}

package com.github.unchai.maven.checkstyle;

import java.io.File;
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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kohsuke.github.GHCommitState;

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

    @Parameter(defaultValue = "${project.basedir}", required = true)
    String projectBasedir;

    @Parameter(property = "checkstyle-github.configLocation", required = true)
    String configLocation;

    @Component(role = GithubHelper.class)
    GithubHelper githubHelper;

    @Component(role = CheckstyleExecutor.class)
    CheckstyleExecutor checkstyleExecutor;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        githubHelper.connect(ghEndpoint, ghToken, ghRepository, ghPullRequest);
        githubHelper.changeStatus(GHCommitState.PENDING, null);

        final List<ChangedFile> changedFiles = githubHelper.listChangedFile();
        final Map<String, ChangedFile> changedFileMap =
            changedFiles
                .stream()
                .collect(Collectors.toMap(ChangedFile::getPath, Function.identity()));

        final List<File> files =
            changedFiles
                .stream()
                .map(changedFile -> new File(projectBasedir, changedFile.getPath()))
                .filter(file -> file.getName().endsWith(".java"))
                .collect(Collectors.toList());

        final List<CheckstyleError> checkstyleErrors =
            checkstyleExecutor.execute(configLocation, files)
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
        final String path = stripBasedir(checkstyleError.getFilename());

        return changedFileMap.containsKey(path)
            && changedFileMap.get(path).getLinePositionMap().containsKey(checkstyleError.getLine());
    }

    private String stripBasedir(String filepath) {
        return filepath.replace(projectBasedir, "").substring(1);
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
            final String path = stripBasedir(error.getFilename());
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

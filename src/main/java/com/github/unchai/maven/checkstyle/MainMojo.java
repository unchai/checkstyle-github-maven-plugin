package com.github.unchai.maven.checkstyle;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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

    @Component
    private GithubHelper githubHelper;

    @Component
    private CheckstyleExecutor checkstyleExecutor;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        githubHelper.connect(ghEndpoint, ghToken, ghRepository, ghPullRequest);
    }
}

package com.github.unchai.maven.checkstyle;

import java.io.File;
import java.util.List;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public class CheckstyleExecutor {
    private Checker checker;

    public CheckstyleExecutor() {
        this.checker = new Checker();
    }

    public List<CheckstyleError> execute(String config, List<File> files) throws CheckstyleException {
        final Configuration configuration = ConfigurationLoader.loadConfiguration(
            config,
            new PropertiesExpander(System.getProperties())
        );

        final CheckstyleAuditListener listener = new CheckstyleAuditListener();

        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(configuration);
        checker.addListener(listener);
        checker.process(files);

        return listener.getErrors();
    }
}

package com.github.unchai.maven.checkstyle;

import java.util.ArrayList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

public class CheckstyleAuditListener implements AuditListener {
    private List<CheckstyleError> errors = new ArrayList<>();

    @Override
    public void auditStarted(AuditEvent event) {
        // do nothing
    }

    @Override
    public void auditFinished(AuditEvent event) {
        // do nothing
    }

    @Override
    public void fileStarted(AuditEvent event) {
        // do nothing
    }

    @Override
    public void fileFinished(AuditEvent event) {
        // do nothing
    }

    @Override
    public void addError(AuditEvent event) {
        final CheckstyleError error = new CheckstyleError();
        error.setSeverityLevel(event.getSeverityLevel());
        error.setFilename(event.getFileName());
        error.setLine(event.getLine());
        error.setMessage(event.getMessage());

        this.errors.add(error);
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        // do nothing
    }

    public List<CheckstyleError> getErrors() {
        return errors;
    }
}

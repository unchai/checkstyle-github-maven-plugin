package com.github.unchai.maven.checkstyle;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Comment {
    private String path;
    private int position;
    private List<CheckstyleError> checkstyleErrors;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<CheckstyleError> getCheckstyleErrors() {
        return checkstyleErrors;
    }

    public void setCheckstyleErrors(List<CheckstyleError> checkstyleErrors) {
        this.checkstyleErrors = checkstyleErrors;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

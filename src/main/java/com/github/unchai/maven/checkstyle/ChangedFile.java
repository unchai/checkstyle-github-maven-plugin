package com.github.unchai.maven.checkstyle;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ChangedFile {
    private String path;
    private Map<Integer, Integer> linePositionMap;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<Integer, Integer> getLinePositionMap() {
        return linePositionMap;
    }

    public void setLinePositionMap(Map<Integer, Integer> linePositionMap) {
        this.linePositionMap = linePositionMap;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

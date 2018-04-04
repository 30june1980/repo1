package com.shutterfly.missioncontrol.listener;

import java.util.List;

public class TestRailPOJO {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    TestRailPOJO(List<Result> results) {
        this.results = results;
    }
}
package com.shutterfly.missioncontrol.listener;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {
    Result(Integer caseId, Integer statusId, String comment) {
        this.case_Id = caseId;
        this.status_Id = statusId;
        this.comment = comment;
    }
    @JsonProperty("case_id")
    private Integer case_Id;

    @JsonProperty("status_id")
    private Integer status_Id;
    private String comment;

    @JsonProperty("case_id")
    public Integer getCaseId() {
        return case_Id;
    }

    @JsonProperty("case_id")
    public void setCaseId(Integer caseId) {
        this.case_Id = caseId;
    }

    @JsonProperty("status_id")
    public Integer getStatusId() {
        return status_Id;
    }

    @JsonProperty("status_id")
    public void setStatusId(Integer statusId) {
        this.status_Id = statusId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
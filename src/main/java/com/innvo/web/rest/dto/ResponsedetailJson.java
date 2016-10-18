package com.innvo.web.rest.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponsedetailJson {

	@JsonProperty("questiongroups")
	public List<Questiongroup> questiongroups;
	
	
	public ResponsedetailJson() {	
	}
	
	public List<Questiongroup> getQuestiongroups() {
		return questiongroups;
	}
	
	public void setQuestiongroups(List<Questiongroup> questiongroups) {
		this.questiongroups = questiongroups;
	}
	
}

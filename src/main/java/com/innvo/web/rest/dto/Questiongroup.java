package com.innvo.web.rest.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Questiongroup {
	
	@JsonProperty("questiongroup")
	String questiongroup;
	@JsonProperty("questions")
    List<Question> questions;

	public Questiongroup() {
	}

	public String getQuestiongroup() {
		return questiongroup;
	}

	public void setQuestiongroup(String questiongroup) {
		this.questiongroup = questiongroup;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

}

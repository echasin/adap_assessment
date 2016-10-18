package com.innvo.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Question {

	@JsonProperty("question")
	String question;
	
	@JsonProperty("subquestion")
	String subquestion;
	
	@JsonProperty("response")
	String response;		
	
	public Question() {

	}

	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public String getSubquestion() {
		return subquestion;
	}

	public void setSubquestion(String subquestion) {
		this.subquestion = subquestion;
	}

	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
}

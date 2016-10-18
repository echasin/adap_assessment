package com.innvo.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Responsedetail.
 */
@Entity
@Table(name = "responsedetail")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "responsedetail")
public class Responsedetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "response_id")
    private Long responseId;

    @Column(name = "questionnaire_id")
    private Long questionnaireId;

    @Column(name = "questiongroup_id")
    private Long questiongroupId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "subquestion_id")
    private Long subquestionId;

    @Column(name = "response")
    private String response;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResponseId() {
        return responseId;
    }

    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    public Long getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(Long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public Long getQuestiongroupId() {
        return questiongroupId;
    }

    public void setQuestiongroupId(Long questiongroupId) {
        this.questiongroupId = questiongroupId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getSubquestionId() {
        return subquestionId;
    }

    public void setSubquestionId(Long subquestionId) {
        this.subquestionId = subquestionId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Responsedetail responsedetail = (Responsedetail) o;
        if(responsedetail.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, responsedetail.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Responsedetail{" +
            "id=" + id +
            ", responseId='" + responseId + "'" +
            ", questionnaireId='" + questionnaireId + "'" +
            ", questiongroupId='" + questiongroupId + "'" +
            ", questionId='" + questionId + "'" +
            ", subquestionId='" + subquestionId + "'" +
            ", response='" + response + "'" +
            '}';
    }
}

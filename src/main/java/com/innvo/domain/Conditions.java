package com.innvo.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Conditions.
 */
@Entity
@Table(name = "conditions")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "conditions")
public class Conditions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "action", nullable = false)
    private String action;

    @NotNull
    @Column(name = "operator", nullable = false)
    private String operator;

    @NotNull
    @Column(name = "response", nullable = false)
    private String response;

    @ManyToOne
    @NotNull
    private Question displayedquestion;

    @OneToOne
    @JoinColumn(unique = true)
    private Question question;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Question getDisplayedquestion() {
        return displayedquestion;
    }

    public void setDisplayedquestion(Question question) {
        this.displayedquestion = question;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Conditions conditions = (Conditions) o;
        if(conditions.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, conditions.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Conditions{" +
            "id=" + id +
            ", action='" + action + "'" +
            ", operator='" + operator + "'" +
            ", response='" + response + "'" +
            '}';
    }
}

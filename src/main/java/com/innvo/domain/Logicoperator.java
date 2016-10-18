package com.innvo.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Logicoperator.
 */
@Entity
@Table(name = "logicoperator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "logicoperator")
public class Logicoperator implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "operator")
    private String operator;

    @OneToOne
    @JoinColumn(unique = true)
    private Question firstquestion;

    @OneToOne
    @JoinColumn(unique = true)
    private Question secondquestion;

    @ManyToOne
    private Questionnaire questionnaire;

    @ManyToOne
    private Subquestion firstsubquestion;

    @ManyToOne
    private Subquestion secondsubquestion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Question getFirstquestion() {
        return firstquestion;
    }

    public void setFirstquestion(Question question) {
        this.firstquestion = question;
    }

    public Question getSecondquestion() {
        return secondquestion;
    }

    public void setSecondquestion(Question question) {
        this.secondquestion = question;
    }

    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }

    public Subquestion getFirstsubquestion() {
        return firstsubquestion;
    }

    public void setFirstsubquestion(Subquestion subquestion) {
        this.firstsubquestion = subquestion;
    }

    public Subquestion getSecondsubquestion() {
        return secondsubquestion;
    }

    public void setSecondsubquestion(Subquestion subquestion) {
        this.secondsubquestion = subquestion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Logicoperator logicoperator = (Logicoperator) o;
        if(logicoperator.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, logicoperator.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Logicoperator{" +
            "id=" + id +
            ", operator='" + operator + "'" +
            '}';
    }
}

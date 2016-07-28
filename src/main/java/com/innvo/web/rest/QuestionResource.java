package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Question;
import com.innvo.repository.QuestionRepository;
import com.innvo.repository.search.QuestionSearchRepository;
import com.innvo.web.rest.util.HeaderUtil;
import com.innvo.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Question.
 */
@RestController
@RequestMapping("/api")
public class QuestionResource {

    private final Logger log = LoggerFactory.getLogger(QuestionResource.class);
        
    @Inject
    private QuestionRepository questionRepository;
    
    @Inject
    private QuestionSearchRepository questionSearchRepository;
    
    /**
     * POST  /questions : Create a new question.
     *
     * @param question the question to create
     * @return the ResponseEntity with status 201 (Created) and with body the new question, or with status 400 (Bad Request) if the question has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/questions",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody Question question) throws URISyntaxException {
        log.debug("REST request to save Question : {}", question);
        if (question.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("question", "idexists", "A new question cannot already have an ID")).body(null);
        }
        Question result = questionRepository.save(question);
        questionSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/questions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("question", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /questions : Updates an existing question.
     *
     * @param question the question to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated question,
     * or with status 400 (Bad Request) if the question is not valid,
     * or with status 500 (Internal Server Error) if the question couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/questions",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Question> updateQuestion(@Valid @RequestBody Question question) throws URISyntaxException {
        log.debug("REST request to update Question : {}", question);
        if (question.getId() == null) {
            return createQuestion(question);
        }
        Question result = questionRepository.save(question);
        questionSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("question", question.getId().toString()))
            .body(result);
    }

    /**
     * GET  /questions : get all the questions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of questions in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/questions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Question>> getAllQuestions(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Questions");
        Page<Question> page = questionRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/questions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /questions/:id : get the "id" question.
     *
     * @param id the id of the question to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the question, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/questions/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Question> getQuestion(@PathVariable Long id) {
        log.debug("REST request to get Question : {}", id);
        Question question = questionRepository.findOne(id);
        return Optional.ofNullable(question)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /questions/:id : delete the "id" question.
     *
     * @param id the id of the question to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/questions/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        log.debug("REST request to delete Question : {}", id);
        questionRepository.delete(id);
        questionSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("question", id.toString())).build();
    }

    /**
     * SEARCH  /_search/questions?query=:query : search for the question corresponding
     * to the query.
     *
     * @param query the query of the question search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/questions",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Question>> searchQuestions(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Questions for query {}", query);
        Page<Question> page = questionSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/questions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    

    /**
     * GET  /questions/:id : get questions by question group.
     *
     * @param id the id of the question to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the question, or with status 404 (Not Found)
     * @throws URISyntaxException 
     */
    @RequestMapping(value = "/questionsByQuestionGroup/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Question>> getQuestionByQuestionGroup(@PathVariable Long id,Pageable pageable
    		 ) throws URISyntaxException {
        log.debug("REST request to get Question By Question Group: {}", id);
        Page<Question> page = questionRepository.findByQuestiongroupId(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/questions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
  
    }
}

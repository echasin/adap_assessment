package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Answer;
import com.innvo.repository.AnswerRepository;
import com.innvo.repository.search.AnswerSearchRepository;
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
 * REST controller for managing Answer.
 */
@RestController
@RequestMapping("/api")
public class AnswerResource {

    private final Logger log = LoggerFactory.getLogger(AnswerResource.class);
        
    @Inject
    private AnswerRepository answerRepository;
    
    @Inject
    private AnswerSearchRepository answerSearchRepository;
    
    /**
     * POST  /answers : Create a new answer.
     *
     * @param answer the answer to create
     * @return the ResponseEntity with status 201 (Created) and with body the new answer, or with status 400 (Bad Request) if the answer has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/answers",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Answer> createAnswer(@Valid @RequestBody Answer answer) throws URISyntaxException {
        log.debug("REST request to save Answer : {}", answer);
        if (answer.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("answer", "idexists", "A new answer cannot already have an ID")).body(null);
        }
        Answer result = answerRepository.save(answer);
        answerSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/answers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("answer", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /answers : Updates an existing answer.
     *
     * @param answer the answer to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated answer,
     * or with status 400 (Bad Request) if the answer is not valid,
     * or with status 500 (Internal Server Error) if the answer couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/answers",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Answer> updateAnswer(@Valid @RequestBody Answer answer) throws URISyntaxException {
        log.debug("REST request to update Answer : {}", answer);
        if (answer.getId() == null) {
            return createAnswer(answer);
        }
        Answer result = answerRepository.save(answer);
        answerSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("answer", answer.getId().toString()))
            .body(result);
    }

    /**
     * GET  /answers : get all the answers.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of answers in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/answers",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Answer>> getAllAnswers(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Answers");
        Page<Answer> page = answerRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/answers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /answers/:id : get the "id" answer.
     *
     * @param id the id of the answer to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the answer, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/answers/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Answer> getAnswer(@PathVariable Long id) {
        log.debug("REST request to get Answer : {}", id);
        Answer answer = answerRepository.findOne(id);
        return Optional.ofNullable(answer)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /answers/:id : delete the "id" answer.
     *
     * @param id the id of the answer to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/answers/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        log.debug("REST request to delete Answer : {}", id);
        answerRepository.delete(id);
        answerSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("answer", id.toString())).build();
    }

    /**
     * SEARCH  /_search/answers?query=:query : search for the answer corresponding
     * to the query.
     *
     * @param query the query of the answer search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/answers",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Answer>> searchAnswers(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Answers for query {}", query);
        Page<Answer> page = answerSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/answers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /answers by question/:id : get the "id" answer.
     *
     * @param id the id of the answer to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the answer, or with status 404 (Not Found)
     * @throws URISyntaxException 
     */
    @RequestMapping(value = "/answersByQuestion/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Answer>> getAnswerByQuestion(@PathVariable Long id,Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get Answer by Question : {}", id);
        Page<Answer> page = answerRepository.findByQuestionId(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/answers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);    
    }
}

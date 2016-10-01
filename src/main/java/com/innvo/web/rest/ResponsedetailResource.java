package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innvo.domain.Response;
import com.innvo.domain.Responsedetail;
import com.innvo.repository.ResponseRepository;
import com.innvo.repository.ResponsedetailRepository;
import com.innvo.repository.search.ResponsedetailSearchRepository;
import com.innvo.web.rest.dto.Question;
import com.innvo.web.rest.dto.Questiongroup;
import com.innvo.web.rest.dto.ResponsedetailJson;
import com.innvo.web.rest.util.HeaderUtil;
import com.innvo.web.rest.util.PaginationUtil;

import org.json.JSONException;
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Responsedetail.
 */
@RestController
@RequestMapping("/api")
public class ResponsedetailResource {

    private final Logger log = LoggerFactory.getLogger(ResponsedetailResource.class);
        
    @Inject
    private ResponsedetailRepository responsedetailRepository;
    
    @Inject
    private ResponsedetailSearchRepository responsedetailSearchRepository;
    
    @Inject
    ResponseRepository responseRepository;
    
    /**
     * POST  /responsedetails : Create a new responsedetail.
     *
     * @param responsedetail the responsedetail to create
     * @return the ResponseEntity with status 201 (Created) and with body the new responsedetail, or with status 400 (Bad Request) if the responsedetail has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responsedetails",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Responsedetail> createResponsedetail(@RequestBody Responsedetail responsedetail) throws URISyntaxException {
        log.debug("REST request to save Responsedetail : {}", responsedetail);
        if (responsedetail.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("responsedetail", "idexists", "A new responsedetail cannot already have an ID")).body(null);
        }
        Responsedetail result = responsedetailRepository.save(responsedetail);
        responsedetailSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/responsedetails/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("responsedetail", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /responsedetails : Updates an existing responsedetail.
     *
     * @param responsedetail the responsedetail to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated responsedetail,
     * or with status 400 (Bad Request) if the responsedetail is not valid,
     * or with status 500 (Internal Server Error) if the responsedetail couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responsedetails",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Responsedetail> updateResponsedetail(@RequestBody Responsedetail responsedetail) throws URISyntaxException {
        log.debug("REST request to update Responsedetail : {}", responsedetail);
        if (responsedetail.getId() == null) {
            return createResponsedetail(responsedetail);
        }
        Responsedetail result = responsedetailRepository.save(responsedetail);
        responsedetailSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("responsedetail", responsedetail.getId().toString()))
            .body(result);
    }

    /**
     * GET  /responsedetails : get all the responsedetails.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of responsedetails in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/responsedetails",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Responsedetail>> getAllResponsedetails(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Responsedetails");
        Page<Responsedetail> page = responsedetailRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/responsedetails");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /responsedetails/:id : get the "id" responsedetail.
     *
     * @param id the id of the responsedetail to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the responsedetail, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/responsedetails/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Responsedetail> getResponsedetail(@PathVariable Long id) {
        log.debug("REST request to get Responsedetail : {}", id);
        Responsedetail responsedetail = responsedetailRepository.findOne(id);
        return Optional.ofNullable(responsedetail)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /responsedetails/:id : delete the "id" responsedetail.
     *
     * @param id the id of the responsedetail to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/responsedetails/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteResponsedetail(@PathVariable Long id) {
        log.debug("REST request to delete Responsedetail : {}", id);
        responsedetailRepository.delete(id);
        responsedetailSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("responsedetail", id.toString())).build();
    }

    /**
     * SEARCH  /_search/responsedetails?query=:query : search for the responsedetail corresponding
     * to the query.
     *
     * @param query the query of the responsedetail search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/responsedetails",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Responsedetail>> searchResponsedetails(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Responsedetails for query {}", query);
        Page<Responsedetail> page = responsedetailSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/responsedetails");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * 
     * @param id
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     * @throws JSONException
     */
    @RequestMapping(value = "/saveResponseDetail/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void saveResponseDetail(@PathVariable Long id) throws JsonParseException, JsonMappingException, IOException, JSONException {
        log.debug("REST request to  save ResponseDetail : {}", id);
        Response response = responseRepository.findOne(id);
    
        ObjectMapper mapper = new ObjectMapper();
        
        ResponsedetailJson responsedetailJson = mapper.readValue(response.getDetails(), ResponsedetailJson.class);

        for(Questiongroup questiongroup:responsedetailJson.questiongroups){
        System.out.println(questiongroup.getQuestiongroup());
        for(Question question:questiongroup.getQuestions()){
        	Responsedetail responsedetail=new Responsedetail();
        	System.out.println(question.getQuestion());
        	System.out.println(question.getSubquestion());
        	System.out.println(question.getResponse());
        	responsedetail.setQuestiongroupId(Long.parseLong(questiongroup.getQuestiongroup()));
        	responsedetail.setQuestionId(Long.parseLong(question.getQuestion()));
        	if(question.getSubquestion()!=null){
        	responsedetail.setSubquestionId(Long.parseLong(question.getSubquestion()));
        	}
        	responsedetail.setResponseId(id);
        	responsedetail.setResponse(question.getResponse());
        	responsedetailRepository.save(responsedetail);
         }	
       }
     }


}

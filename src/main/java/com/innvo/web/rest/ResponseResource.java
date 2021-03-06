package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innvo.domain.Questionnaire;
import com.innvo.domain.Response;
import com.innvo.domain.Responsedetail;
import com.innvo.domain.Responsembr;
import com.innvo.repository.QuestionnaireRepository;
import com.innvo.repository.ResponseRepository;
import com.innvo.repository.ResponsedetailRepository;
import com.innvo.repository.ResponsembrRepository;
import com.innvo.repository.search.ResponseSearchRepository;
import com.innvo.security.SpringSecurityAuditorAware;
import com.innvo.web.rest.dto.Question;
import com.innvo.web.rest.dto.Questiongroup;
import com.innvo.web.rest.dto.ResponsedetailJson;
import com.innvo.web.rest.util.HeaderUtil;
import com.innvo.web.rest.util.PaginationUtil;

import org.boon.core.Sys;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Response.
 */
@RestController
@RequestMapping("/api")
public class ResponseResource {

    private final Logger log = LoggerFactory.getLogger(ResponseResource.class);
        
    @Inject
    private ResponseRepository responseRepository;
    
    @Inject
    private ResponseSearchRepository responseSearchRepository;
   
    @Inject
    private QuestionnaireRepository questionnaireRepository;
   
    @Inject
    SpringSecurityAuditorAware springSecurityAuditorAware; 
    
    @Inject
    ResponsembrRepository responsembrRepository;
    
    @Inject
    ResponsedetailRepository responsedetailRepository;
    
    
    /**
     * POST  /responses : Create a new response.
     *
     * @param response the response to create
     * @return the ResponseEntity with status 201 (Created) and with body the new response, or with status 400 (Bad Request) if the response has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responses",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> createResponse(@Valid @RequestBody Response response) throws URISyntaxException {
        log.debug("REST request to save Response : {}", response);
        if (response.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("response", "idexists", "A new response cannot already have an ID")).body(null);
        }
        Response result = responseRepository.save(response);
        responseSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/responses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("response", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /responses : Updates an existing response.
     *
     * @param response the response to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated response,
     * or with status 400 (Bad Request) if the response is not valid,
     * or with status 500 (Internal Server Error) if the response couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responses",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> updateResponse(@Valid @RequestBody Response response) throws URISyntaxException {
        log.debug("REST request to update Response : {}", response);
        if (response.getId() == null) {
            return createResponse(response);
        }
        Response result = responseRepository.save(response);
        responseSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("response", response.getId().toString()))
            .body(result);
    }

    /**
     * GET  /responses : get all the responses.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of responses in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/responses",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Response>> getAllResponses(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Responses");
        Page<Response> page = responseRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/responses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /responses/:id : get the "id" response.
     *
     * @param id the id of the response to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the response, or with status 404 (Not Found)
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     * @throws JSONException 
     */
    @RequestMapping(value = "/responses/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Response> getResponse(@PathVariable Long id) throws JsonParseException, JsonMappingException, IOException, JSONException {
        log.debug("REST request to get Response : {}", id);
        Response response = responseRepository.findOne(id);   
        return Optional.ofNullable(response)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /responses/:id : delete the "id" response.
     *
     * @param id the id of the response to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/responses/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteResponse(@PathVariable Long id) {
        log.debug("REST request to delete Response : {}", id);
        responseRepository.delete(id);
        responseSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("response", id.toString())).build();
    }

    /**
     * SEARCH  /_search/responses?query=:query : search for the response corresponding
     * to the query.
     *
     * @param query the query of the response search
     * @return the result of the search
     */
    @RequestMapping(value = "/_search/responses",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Response>> searchResponses(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Responses for query {}", query);
        Page<Response> page = responseSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/responses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
  
    /**
     * GET  /save response
     *
     * @param id the id of the questionnaire
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @RequestMapping(value = "/saveResponse/{id}/{details}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void saveResponse(@PathVariable("id") Long id,
    		                 @PathVariable("details") String details) throws JsonParseException, JsonMappingException, IOException {
        log.debug("REST request to save Response : {}", id);
        String login=springSecurityAuditorAware.getCurrentAuditor();
        Response response = new Response();
        Questionnaire questionnaire=questionnaireRepository.getOne(id);
        response.setQuestionnaire(questionnaire);
        response.setDetails(details);
        response.setDomain("DEMO");
        response.setLastmodifiedby(login);
        response.setStatus("Active");
        //response.setUsername(login);
        Date date=new Date();
        ZonedDateTime lastmodifieddatetime = ZonedDateTime.ofInstant(date.toInstant(),
                ZoneId.systemDefault());
        response.setLastmodifieddatetime(lastmodifieddatetime);
        Response saveResponse=responseRepository.save(response);
        
        ObjectMapper mapper = new ObjectMapper();
        
        ResponsedetailJson responsedetailJson = mapper.readValue(saveResponse.getDetails(), ResponsedetailJson.class);

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
    
    
    /**
     * GET  /save response
     *
     * @param id the id of the questionnaire
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @RequestMapping(value = "/updateResponse/{id}/{rId}/{details}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void updateResponse(@PathVariable("id") Long id,
    		                   @PathVariable("rId") Long rId,
    		                   @PathVariable("details") String details) throws JsonParseException, JsonMappingException, IOException {
    	   log.debug("REST request to save Response : {}", id);
           String login=springSecurityAuditorAware.getCurrentAuditor();
           Response response = responseRepository.findOne(rId);
           Questionnaire questionnaire=questionnaireRepository.getOne(id);
           response.setQuestionnaire(questionnaire);
           response.setDetails(details);
           response.setDomain("DEMO");
           response.setLastmodifiedby("echasin");
           response.setStatus("Active");
           //response.setUsername(login);
           Date date=new Date();
           ZonedDateTime lastmodifieddatetime = ZonedDateTime.ofInstant(date.toInstant(),
                   ZoneId.systemDefault());
           response.setLastmodifieddatetime(lastmodifieddatetime);
           Response saveResponse=responseRepository.save(response);
           
           ObjectMapper mapper = new ObjectMapper();
           
           ResponsedetailJson responsedetailJson = mapper.readValue(saveResponse.getDetails(), ResponsedetailJson.class);

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

    /**
     * 
     * @param response
     * @throws URISyntaxException
     */
    @RequestMapping(value = "/saveResponseAndResponsembr/{id}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
        @Timed
        public void saveResponseAndResponsembr(@Valid @RequestBody Response response,@PathVariable("id") Long id) throws URISyntaxException {
    	    log.debug("REST request to save Response : {}", response);
            Responsembr responsembr=new Responsembr();
            Date date=new Date();
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(),
                    ZoneId.systemDefault());
            response.setLastmodifieddatetime(zonedDateTime);
            responsembr.setResponse(response);
            responsembr.setDomain(response.getDomain());
            responsembr.setLastmodifiedby(response.getLastmodifiedby());
            responsembr.setStatus(response.getStatus());
            responsembr.setLastmodifieddatetime(zonedDateTime);
            responsembr.setAssetId(id);
            Response savedResponse = responseRepository.save(response);
            Responsembr savedResponsembr=responsembrRepository.save(responsembr);
        }
 
    
    
    /**
     * 
     * @param response
     * @throws URISyntaxException
     */
    @RequestMapping(value = "/updateResponseAndResponsembr/{id}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
        @Timed
        public void updateResponseAndResponsembr(@Valid @RequestBody Response response,@PathVariable("id") Long id) throws URISyntaxException {
            log.debug("REST request to save Response : {}", response);
            Responsembr responsembr=new Responsembr();
            responsembr.setResponse(response);
            responsembr.setDomain(response.getDomain());
            responsembr.setLastmodifiedby(response.getLastmodifiedby());
            responsembr.setStatus(response.getStatus());
            responsembr.setAssetId(id);
            Response savedResponse = responseRepository.save(response);
            Responsembr savedResponsembr=responsembrRepository.save(responsembr);
            
        }
    
    /**
     * 
     * @param id
     * @return
     * @throws IOException
     * @throws URISyntaxException 
     */
    @RequestMapping(value = "/responseByAsset/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
        @Timed
       // public ResponseEntity<List<Response>> getResponseByAsset(@PathVariable Long id) throws  IOException {
    	public ResponseEntity<List<Response>> getResponseByAsset(@PathVariable Long id,Pageable pageable) throws  IOException, URISyntaxException {
            log.debug("REST request to get Response : {}", id);
            List<Response> responses=new ArrayList<Response>();
            List<Responsembr> responsembrs = responsembrRepository.findByAssetId(id);
            for(Responsembr responsembr:responsembrs){
                Response response = responseRepository.findOne(responsembr.getResponse().getId());
                responses.add(response);
         //   }
         //   return new ResponseEntity<>(responses, HttpStatus.OK);
            } 
            Page<Response> usersPage = new PageImpl<Response>(responses,pageable, responses.size()); 
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(usersPage, "/api/responses");
            return new ResponseEntity<>(responses,headers,HttpStatus.OK);
        }
}
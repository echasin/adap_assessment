package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Responsembr;

import com.innvo.repository.ResponsembrRepository;
import com.innvo.repository.search.ResponsembrSearchRepository;
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
 * REST controller for managing Responsembr.
 */
@RestController
@RequestMapping("/api")
public class ResponsembrResource {

    private final Logger log = LoggerFactory.getLogger(ResponsembrResource.class);
        
    @Inject
    private ResponsembrRepository responsembrRepository;

    @Inject
    private ResponsembrSearchRepository responsembrSearchRepository;

    /**
     * POST  /responsembrs : Create a new responsembr.
     *
     * @param responsembr the responsembr to create
     * @return the ResponseEntity with status 201 (Created) and with body the new responsembr, or with status 400 (Bad Request) if the responsembr has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responsembrs",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Responsembr> createResponsembr(@Valid @RequestBody Responsembr responsembr) throws URISyntaxException {
        log.debug("REST request to save Responsembr : {}", responsembr);
        if (responsembr.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("responsembr", "idexists", "A new responsembr cannot already have an ID")).body(null);
        }
        Responsembr result = responsembrRepository.save(responsembr);
        responsembrSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/responsembrs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("responsembr", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /responsembrs : Updates an existing responsembr.
     *
     * @param responsembr the responsembr to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated responsembr,
     * or with status 400 (Bad Request) if the responsembr is not valid,
     * or with status 500 (Internal Server Error) if the responsembr couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/responsembrs",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Responsembr> updateResponsembr(@Valid @RequestBody Responsembr responsembr) throws URISyntaxException {
        log.debug("REST request to update Responsembr : {}", responsembr);
        if (responsembr.getId() == null) {
            return createResponsembr(responsembr);
        }
        Responsembr result = responsembrRepository.save(responsembr);
        responsembrSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("responsembr", responsembr.getId().toString()))
            .body(result);
    }

    /**
     * GET  /responsembrs : get all the responsembrs.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of responsembrs in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/responsembrs",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Responsembr>> getAllResponsembrs(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Responsembrs");
        Page<Responsembr> page = responsembrRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/responsembrs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /responsembrs/:id : get the "id" responsembr.
     *
     * @param id the id of the responsembr to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the responsembr, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/responsembrs/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Responsembr> getResponsembr(@PathVariable Long id) {
        log.debug("REST request to get Responsembr : {}", id);
        Responsembr responsembr = responsembrRepository.findOne(id);
        return Optional.ofNullable(responsembr)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /responsembrs/:id : delete the "id" responsembr.
     *
     * @param id the id of the responsembr to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/responsembrs/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteResponsembr(@PathVariable Long id) {
        log.debug("REST request to delete Responsembr : {}", id);
        responsembrRepository.delete(id);
        responsembrSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("responsembr", id.toString())).build();
    }

    /**
     * SEARCH  /_search/responsembrs?query=:query : search for the responsembr corresponding
     * to the query.
     *
     * @param query the query of the responsembr search 
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/_search/responsembrs",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Responsembr>> searchResponsembrs(@RequestParam String query, Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Responsembrs for query {}", query);
        Page<Responsembr> page = responsembrSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/responsembrs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}

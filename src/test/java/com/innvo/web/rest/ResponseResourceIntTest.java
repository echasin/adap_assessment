package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Response;
import com.innvo.repository.ResponseRepository;
import com.innvo.repository.search.ResponseSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ResponseResource REST controller.
 *
 * @see ResponseResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdapAssessmentApp.class)
public class ResponseResourceIntTest {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));
    private static final String DEFAULT_DETAILS = "AAAAA";
    private static final String UPDATED_DETAILS = "BBBBB";
    private static final String DEFAULT_STATUS = "AAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_LASTMODIFIEDBY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_LASTMODIFIEDBY = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_LASTMODIFIEDDATETIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_LASTMODIFIEDDATETIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_LASTMODIFIEDDATETIME_STR = dateTimeFormatter.format(DEFAULT_LASTMODIFIEDDATETIME);
    private static final String DEFAULT_DOMAIN = "AAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_DOMAIN = "BBBBBBBBBBBBBBBBBBBBBBBBB";

    @Inject
    private ResponseRepository responseRepository;

    @Inject
    private ResponseSearchRepository responseSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restResponseMockMvc;

    private Response response;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ResponseResource responseResource = new ResponseResource();
        ReflectionTestUtils.setField(responseResource, "responseSearchRepository", responseSearchRepository);
        ReflectionTestUtils.setField(responseResource, "responseRepository", responseRepository);
        this.restResponseMockMvc = MockMvcBuilders.standaloneSetup(responseResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Response createEntity(EntityManager em) {
        Response response = new Response();
        response = new Response();
        response.setDetails(DEFAULT_DETAILS);
        response.setStatus(DEFAULT_STATUS);
        response.setLastmodifiedby(DEFAULT_LASTMODIFIEDBY);
        response.setLastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME);
        response.setDomain(DEFAULT_DOMAIN);
        return response;
    }

    @Before
    public void initTest() {
        responseSearchRepository.deleteAll();
        response = createEntity(em);
    }

    @Test
    @Transactional
    public void createResponse() throws Exception {
        int databaseSizeBeforeCreate = responseRepository.findAll().size();

        // Create the Response

        restResponseMockMvc.perform(post("/api/responses")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(response)))
                .andExpect(status().isCreated());

        // Validate the Response in the database
        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeCreate + 1);
        Response testResponse = responses.get(responses.size() - 1);
        assertThat(testResponse.getDetails()).isEqualTo(DEFAULT_DETAILS);
        assertThat(testResponse.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testResponse.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testResponse.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testResponse.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Response in ElasticSearch
        Response responseEs = responseSearchRepository.findOne(testResponse.getId());
        assertThat(responseEs).isEqualToComparingFieldByField(testResponse);
    }

    @Test
    @Transactional
    public void checkDetailsIsRequired() throws Exception {
        int databaseSizeBeforeTest = responseRepository.findAll().size();
        // set the field null
        response.setDetails(null);

        // Create the Response, which fails.

        restResponseMockMvc.perform(post("/api/responses")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(response)))
                .andExpect(status().isBadRequest());

        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = responseRepository.findAll().size();
        // set the field null
        response.setStatus(null);

        // Create the Response, which fails.

        restResponseMockMvc.perform(post("/api/responses")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(response)))
                .andExpect(status().isBadRequest());

        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = responseRepository.findAll().size();
        // set the field null
        response.setLastmodifiedby(null);

        // Create the Response, which fails.

        restResponseMockMvc.perform(post("/api/responses")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(response)))
                .andExpect(status().isBadRequest());

        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = responseRepository.findAll().size();
        // set the field null
        response.setLastmodifieddatetime(null);

        // Create the Response, which fails.

        restResponseMockMvc.perform(post("/api/responses")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(response)))
                .andExpect(status().isBadRequest());

        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = responseRepository.findAll().size();
        // set the field null
        response.setDomain(null);

        // Create the Response, which fails.

        restResponseMockMvc.perform(post("/api/responses")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(response)))
                .andExpect(status().isBadRequest());

        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllResponses() throws Exception {
        // Initialize the database
        responseRepository.saveAndFlush(response);

        // Get all the responses
        restResponseMockMvc.perform(get("/api/responses?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(response.getId().intValue())))
                .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS.toString())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
                .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }

    @Test
    @Transactional
    public void getResponse() throws Exception {
        // Initialize the database
        responseRepository.saveAndFlush(response);

        // Get the response
        restResponseMockMvc.perform(get("/api/responses/{id}", response.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(response.getId().intValue()))
            .andExpect(jsonPath("$.details").value(DEFAULT_DETAILS.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastmodifiedby").value(DEFAULT_LASTMODIFIEDBY.toString()))
            .andExpect(jsonPath("$.lastmodifieddatetime").value(DEFAULT_LASTMODIFIEDDATETIME_STR))
            .andExpect(jsonPath("$.domain").value(DEFAULT_DOMAIN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingResponse() throws Exception {
        // Get the response
        restResponseMockMvc.perform(get("/api/responses/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateResponse() throws Exception {
        // Initialize the database
        responseRepository.saveAndFlush(response);
        responseSearchRepository.save(response);
        int databaseSizeBeforeUpdate = responseRepository.findAll().size();

        // Update the response
        Response updatedResponse = responseRepository.findOne(response.getId());
        updatedResponse.setDetails(UPDATED_DETAILS);
        updatedResponse.setStatus(UPDATED_STATUS);
        updatedResponse.setLastmodifiedby(UPDATED_LASTMODIFIEDBY);
        updatedResponse.setLastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME);
        updatedResponse.setDomain(UPDATED_DOMAIN);

        restResponseMockMvc.perform(put("/api/responses")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedResponse)))
                .andExpect(status().isOk());

        // Validate the Response in the database
        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeUpdate);
        Response testResponse = responses.get(responses.size() - 1);
        assertThat(testResponse.getDetails()).isEqualTo(UPDATED_DETAILS);
        assertThat(testResponse.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testResponse.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testResponse.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testResponse.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Response in ElasticSearch
        Response responseEs = responseSearchRepository.findOne(testResponse.getId());
        assertThat(responseEs).isEqualToComparingFieldByField(testResponse);
    }

    @Test
    @Transactional
    public void deleteResponse() throws Exception {
        // Initialize the database
        responseRepository.saveAndFlush(response);
        responseSearchRepository.save(response);
        int databaseSizeBeforeDelete = responseRepository.findAll().size();

        // Get the response
        restResponseMockMvc.perform(delete("/api/responses/{id}", response.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean responseExistsInEs = responseSearchRepository.exists(response.getId());
        assertThat(responseExistsInEs).isFalse();

        // Validate the database is empty
        List<Response> responses = responseRepository.findAll();
        assertThat(responses).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchResponse() throws Exception {
        // Initialize the database
        responseRepository.saveAndFlush(response);
        responseSearchRepository.save(response);

        // Search the response
        restResponseMockMvc.perform(get("/api/_search/responses?query=id:" + response.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(response.getId().intValue())))
            .andExpect(jsonPath("$.[*].details").value(hasItem(DEFAULT_DETAILS.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
            .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }
}

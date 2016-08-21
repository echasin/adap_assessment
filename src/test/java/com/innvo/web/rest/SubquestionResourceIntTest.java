package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Subquestion;
import com.innvo.repository.SubquestionRepository;
import com.innvo.repository.search.SubquestionSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the SubquestionResource REST controller.
 *
 * @see SubquestionResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapAssessmentApp.class)
@WebAppConfiguration
@IntegrationTest
public class SubquestionResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_SUBQUESTION = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_SUBQUESTION = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_CODE = "AAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBBBBBBBBBBBBBBBBB";

    private static final Integer DEFAULT_POSITION = 1;
    private static final Integer UPDATED_POSITION = 2;
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
    private SubquestionRepository subquestionRepository;

    @Inject
    private SubquestionSearchRepository subquestionSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restSubquestionMockMvc;

    private Subquestion subquestion;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        SubquestionResource subquestionResource = new SubquestionResource();
        ReflectionTestUtils.setField(subquestionResource, "subquestionSearchRepository", subquestionSearchRepository);
        ReflectionTestUtils.setField(subquestionResource, "subquestionRepository", subquestionRepository);
        this.restSubquestionMockMvc = MockMvcBuilders.standaloneSetup(subquestionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        subquestionSearchRepository.deleteAll();
        subquestion = new Subquestion();
        subquestion.setSubquestion(DEFAULT_SUBQUESTION);
        subquestion.setCode(DEFAULT_CODE);
        subquestion.setPosition(DEFAULT_POSITION);
        subquestion.setStatus(DEFAULT_STATUS);
        subquestion.setLastmodifiedby(DEFAULT_LASTMODIFIEDBY);
        subquestion.setLastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME);
        subquestion.setDomain(DEFAULT_DOMAIN);
    }

    @Test
    @Transactional
    public void createSubquestion() throws Exception {
        int databaseSizeBeforeCreate = subquestionRepository.findAll().size();

        // Create the Subquestion

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isCreated());

        // Validate the Subquestion in the database
        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeCreate + 1);
        Subquestion testSubquestion = subquestions.get(subquestions.size() - 1);
        assertThat(testSubquestion.getSubquestion()).isEqualTo(DEFAULT_SUBQUESTION);
        assertThat(testSubquestion.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testSubquestion.getPosition()).isEqualTo(DEFAULT_POSITION);
        assertThat(testSubquestion.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testSubquestion.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testSubquestion.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testSubquestion.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Subquestion in ElasticSearch
        Subquestion subquestionEs = subquestionSearchRepository.findOne(testSubquestion.getId());
        assertThat(subquestionEs).isEqualToComparingFieldByField(testSubquestion);
    }

    @Test
    @Transactional
    public void checkSubquestionIsRequired() throws Exception {
        int databaseSizeBeforeTest = subquestionRepository.findAll().size();
        // set the field null
        subquestion.setSubquestion(null);

        // Create the Subquestion, which fails.

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isBadRequest());

        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = subquestionRepository.findAll().size();
        // set the field null
        subquestion.setCode(null);

        // Create the Subquestion, which fails.

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isBadRequest());

        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPositionIsRequired() throws Exception {
        int databaseSizeBeforeTest = subquestionRepository.findAll().size();
        // set the field null
        subquestion.setPosition(null);

        // Create the Subquestion, which fails.

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isBadRequest());

        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = subquestionRepository.findAll().size();
        // set the field null
        subquestion.setStatus(null);

        // Create the Subquestion, which fails.

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isBadRequest());

        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = subquestionRepository.findAll().size();
        // set the field null
        subquestion.setLastmodifiedby(null);

        // Create the Subquestion, which fails.

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isBadRequest());

        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = subquestionRepository.findAll().size();
        // set the field null
        subquestion.setLastmodifieddatetime(null);

        // Create the Subquestion, which fails.

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isBadRequest());

        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = subquestionRepository.findAll().size();
        // set the field null
        subquestion.setDomain(null);

        // Create the Subquestion, which fails.

        restSubquestionMockMvc.perform(post("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(subquestion)))
                .andExpect(status().isBadRequest());

        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSubquestions() throws Exception {
        // Initialize the database
        subquestionRepository.saveAndFlush(subquestion);

        // Get all the subquestions
        restSubquestionMockMvc.perform(get("/api/subquestions?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(subquestion.getId().intValue())))
                .andExpect(jsonPath("$.[*].subquestion").value(hasItem(DEFAULT_SUBQUESTION.toString())))
                .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
                .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
                .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }

    @Test
    @Transactional
    public void getSubquestion() throws Exception {
        // Initialize the database
        subquestionRepository.saveAndFlush(subquestion);

        // Get the subquestion
        restSubquestionMockMvc.perform(get("/api/subquestions/{id}", subquestion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(subquestion.getId().intValue()))
            .andExpect(jsonPath("$.subquestion").value(DEFAULT_SUBQUESTION.toString()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.position").value(DEFAULT_POSITION))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastmodifiedby").value(DEFAULT_LASTMODIFIEDBY.toString()))
            .andExpect(jsonPath("$.lastmodifieddatetime").value(DEFAULT_LASTMODIFIEDDATETIME_STR))
            .andExpect(jsonPath("$.domain").value(DEFAULT_DOMAIN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSubquestion() throws Exception {
        // Get the subquestion
        restSubquestionMockMvc.perform(get("/api/subquestions/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSubquestion() throws Exception {
        // Initialize the database
        subquestionRepository.saveAndFlush(subquestion);
        subquestionSearchRepository.save(subquestion);
        int databaseSizeBeforeUpdate = subquestionRepository.findAll().size();

        // Update the subquestion
        Subquestion updatedSubquestion = new Subquestion();
        updatedSubquestion.setId(subquestion.getId());
        updatedSubquestion.setSubquestion(UPDATED_SUBQUESTION);
        updatedSubquestion.setCode(UPDATED_CODE);
        updatedSubquestion.setPosition(UPDATED_POSITION);
        updatedSubquestion.setStatus(UPDATED_STATUS);
        updatedSubquestion.setLastmodifiedby(UPDATED_LASTMODIFIEDBY);
        updatedSubquestion.setLastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME);
        updatedSubquestion.setDomain(UPDATED_DOMAIN);

        restSubquestionMockMvc.perform(put("/api/subquestions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedSubquestion)))
                .andExpect(status().isOk());

        // Validate the Subquestion in the database
        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeUpdate);
        Subquestion testSubquestion = subquestions.get(subquestions.size() - 1);
        assertThat(testSubquestion.getSubquestion()).isEqualTo(UPDATED_SUBQUESTION);
        assertThat(testSubquestion.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testSubquestion.getPosition()).isEqualTo(UPDATED_POSITION);
        assertThat(testSubquestion.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testSubquestion.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testSubquestion.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testSubquestion.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Subquestion in ElasticSearch
        Subquestion subquestionEs = subquestionSearchRepository.findOne(testSubquestion.getId());
        assertThat(subquestionEs).isEqualToComparingFieldByField(testSubquestion);
    }

    @Test
    @Transactional
    public void deleteSubquestion() throws Exception {
        // Initialize the database
        subquestionRepository.saveAndFlush(subquestion);
        subquestionSearchRepository.save(subquestion);
        int databaseSizeBeforeDelete = subquestionRepository.findAll().size();

        // Get the subquestion
        restSubquestionMockMvc.perform(delete("/api/subquestions/{id}", subquestion.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean subquestionExistsInEs = subquestionSearchRepository.exists(subquestion.getId());
        assertThat(subquestionExistsInEs).isFalse();

        // Validate the database is empty
        List<Subquestion> subquestions = subquestionRepository.findAll();
        assertThat(subquestions).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchSubquestion() throws Exception {
        // Initialize the database
        subquestionRepository.saveAndFlush(subquestion);
        subquestionSearchRepository.save(subquestion);

        // Search the subquestion
        restSubquestionMockMvc.perform(get("/api/_search/subquestions?query=id:" + subquestion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(subquestion.getId().intValue())))
            .andExpect(jsonPath("$.[*].subquestion").value(hasItem(DEFAULT_SUBQUESTION.toString())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
            .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }
}

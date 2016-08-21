package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Questionnaire;
import com.innvo.repository.QuestionnaireRepository;
import com.innvo.repository.search.QuestionnaireSearchRepository;

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
 * Test class for the QuestionnaireResource REST controller.
 *
 * @see QuestionnaireResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapAssessmentApp.class)
@WebAppConfiguration
@IntegrationTest
public class QuestionnaireResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_TITLE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
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
    private QuestionnaireRepository questionnaireRepository;

    @Inject
    private QuestionnaireSearchRepository questionnaireSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restQuestionnaireMockMvc;

    private Questionnaire questionnaire;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        QuestionnaireResource questionnaireResource = new QuestionnaireResource();
        ReflectionTestUtils.setField(questionnaireResource, "questionnaireSearchRepository", questionnaireSearchRepository);
        ReflectionTestUtils.setField(questionnaireResource, "questionnaireRepository", questionnaireRepository);
        this.restQuestionnaireMockMvc = MockMvcBuilders.standaloneSetup(questionnaireResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        questionnaireSearchRepository.deleteAll();
        questionnaire = new Questionnaire();
        questionnaire.setTitle(DEFAULT_TITLE);
        questionnaire.setStatus(DEFAULT_STATUS);
        questionnaire.setLastmodifiedby(DEFAULT_LASTMODIFIEDBY);
        questionnaire.setLastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME);
        questionnaire.setDomain(DEFAULT_DOMAIN);
    }

    @Test
    @Transactional
    public void createQuestionnaire() throws Exception {
        int databaseSizeBeforeCreate = questionnaireRepository.findAll().size();

        // Create the Questionnaire

        restQuestionnaireMockMvc.perform(post("/api/questionnaires")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questionnaire)))
                .andExpect(status().isCreated());

        // Validate the Questionnaire in the database
        List<Questionnaire> questionnaires = questionnaireRepository.findAll();
        assertThat(questionnaires).hasSize(databaseSizeBeforeCreate + 1);
        Questionnaire testQuestionnaire = questionnaires.get(questionnaires.size() - 1);
        assertThat(testQuestionnaire.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testQuestionnaire.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testQuestionnaire.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testQuestionnaire.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testQuestionnaire.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Questionnaire in ElasticSearch
        Questionnaire questionnaireEs = questionnaireSearchRepository.findOne(testQuestionnaire.getId());
        assertThat(questionnaireEs).isEqualToComparingFieldByField(testQuestionnaire);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = questionnaireRepository.findAll().size();
        // set the field null
        questionnaire.setStatus(null);

        // Create the Questionnaire, which fails.

        restQuestionnaireMockMvc.perform(post("/api/questionnaires")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questionnaire)))
                .andExpect(status().isBadRequest());

        List<Questionnaire> questionnaires = questionnaireRepository.findAll();
        assertThat(questionnaires).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = questionnaireRepository.findAll().size();
        // set the field null
        questionnaire.setLastmodifiedby(null);

        // Create the Questionnaire, which fails.

        restQuestionnaireMockMvc.perform(post("/api/questionnaires")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questionnaire)))
                .andExpect(status().isBadRequest());

        List<Questionnaire> questionnaires = questionnaireRepository.findAll();
        assertThat(questionnaires).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = questionnaireRepository.findAll().size();
        // set the field null
        questionnaire.setLastmodifieddatetime(null);

        // Create the Questionnaire, which fails.

        restQuestionnaireMockMvc.perform(post("/api/questionnaires")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questionnaire)))
                .andExpect(status().isBadRequest());

        List<Questionnaire> questionnaires = questionnaireRepository.findAll();
        assertThat(questionnaires).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = questionnaireRepository.findAll().size();
        // set the field null
        questionnaire.setDomain(null);

        // Create the Questionnaire, which fails.

        restQuestionnaireMockMvc.perform(post("/api/questionnaires")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questionnaire)))
                .andExpect(status().isBadRequest());

        List<Questionnaire> questionnaires = questionnaireRepository.findAll();
        assertThat(questionnaires).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllQuestionnaires() throws Exception {
        // Initialize the database
        questionnaireRepository.saveAndFlush(questionnaire);

        // Get all the questionnaires
        restQuestionnaireMockMvc.perform(get("/api/questionnaires?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(questionnaire.getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
                .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }

    @Test
    @Transactional
    public void getQuestionnaire() throws Exception {
        // Initialize the database
        questionnaireRepository.saveAndFlush(questionnaire);

        // Get the questionnaire
        restQuestionnaireMockMvc.perform(get("/api/questionnaires/{id}", questionnaire.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(questionnaire.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastmodifiedby").value(DEFAULT_LASTMODIFIEDBY.toString()))
            .andExpect(jsonPath("$.lastmodifieddatetime").value(DEFAULT_LASTMODIFIEDDATETIME_STR))
            .andExpect(jsonPath("$.domain").value(DEFAULT_DOMAIN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingQuestionnaire() throws Exception {
        // Get the questionnaire
        restQuestionnaireMockMvc.perform(get("/api/questionnaires/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateQuestionnaire() throws Exception {
        // Initialize the database
        questionnaireRepository.saveAndFlush(questionnaire);
        questionnaireSearchRepository.save(questionnaire);
        int databaseSizeBeforeUpdate = questionnaireRepository.findAll().size();

        // Update the questionnaire
        Questionnaire updatedQuestionnaire = new Questionnaire();
        updatedQuestionnaire.setId(questionnaire.getId());
        updatedQuestionnaire.setTitle(UPDATED_TITLE);
        updatedQuestionnaire.setStatus(UPDATED_STATUS);
        updatedQuestionnaire.setLastmodifiedby(UPDATED_LASTMODIFIEDBY);
        updatedQuestionnaire.setLastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME);
        updatedQuestionnaire.setDomain(UPDATED_DOMAIN);

        restQuestionnaireMockMvc.perform(put("/api/questionnaires")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedQuestionnaire)))
                .andExpect(status().isOk());

        // Validate the Questionnaire in the database
        List<Questionnaire> questionnaires = questionnaireRepository.findAll();
        assertThat(questionnaires).hasSize(databaseSizeBeforeUpdate);
        Questionnaire testQuestionnaire = questionnaires.get(questionnaires.size() - 1);
        assertThat(testQuestionnaire.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testQuestionnaire.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testQuestionnaire.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testQuestionnaire.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testQuestionnaire.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Questionnaire in ElasticSearch
        Questionnaire questionnaireEs = questionnaireSearchRepository.findOne(testQuestionnaire.getId());
        assertThat(questionnaireEs).isEqualToComparingFieldByField(testQuestionnaire);
    }

    @Test
    @Transactional
    public void deleteQuestionnaire() throws Exception {
        // Initialize the database
        questionnaireRepository.saveAndFlush(questionnaire);
        questionnaireSearchRepository.save(questionnaire);
        int databaseSizeBeforeDelete = questionnaireRepository.findAll().size();

        // Get the questionnaire
        restQuestionnaireMockMvc.perform(delete("/api/questionnaires/{id}", questionnaire.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean questionnaireExistsInEs = questionnaireSearchRepository.exists(questionnaire.getId());
        assertThat(questionnaireExistsInEs).isFalse();

        // Validate the database is empty
        List<Questionnaire> questionnaires = questionnaireRepository.findAll();
        assertThat(questionnaires).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchQuestionnaire() throws Exception {
        // Initialize the database
        questionnaireRepository.saveAndFlush(questionnaire);
        questionnaireSearchRepository.save(questionnaire);

        // Search the questionnaire
        restQuestionnaireMockMvc.perform(get("/api/_search/questionnaires?query=id:" + questionnaire.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(questionnaire.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
            .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }
}

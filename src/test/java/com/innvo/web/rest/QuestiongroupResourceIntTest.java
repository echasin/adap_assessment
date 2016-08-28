package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Questiongroup;
import com.innvo.repository.QuestiongroupRepository;
import com.innvo.repository.search.QuestiongroupSearchRepository;

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
 * Test class for the QuestiongroupResource REST controller.
 *
 * @see QuestiongroupResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdapAssessmentApp.class)
public class QuestiongroupResourceIntTest {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));
    private static final String DEFAULT_TITLE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";

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
    private QuestiongroupRepository questiongroupRepository;

    @Inject
    private QuestiongroupSearchRepository questiongroupSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restQuestiongroupMockMvc;

    private Questiongroup questiongroup;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        QuestiongroupResource questiongroupResource = new QuestiongroupResource();
        ReflectionTestUtils.setField(questiongroupResource, "questiongroupSearchRepository", questiongroupSearchRepository);
        ReflectionTestUtils.setField(questiongroupResource, "questiongroupRepository", questiongroupRepository);
        this.restQuestiongroupMockMvc = MockMvcBuilders.standaloneSetup(questiongroupResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Questiongroup createEntity(EntityManager em) {
        Questiongroup questiongroup = new Questiongroup();
        questiongroup = new Questiongroup();
        questiongroup.setTitle(DEFAULT_TITLE);
        questiongroup.setDescription(DEFAULT_DESCRIPTION);
        questiongroup.setPosition(DEFAULT_POSITION);
        questiongroup.setStatus(DEFAULT_STATUS);
        questiongroup.setLastmodifiedby(DEFAULT_LASTMODIFIEDBY);
        questiongroup.setLastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME);
        questiongroup.setDomain(DEFAULT_DOMAIN);
        return questiongroup;
    }

    @Before
    public void initTest() {
        questiongroupSearchRepository.deleteAll();
        questiongroup = createEntity(em);
    }

    @Test
    @Transactional
    public void createQuestiongroup() throws Exception {
        int databaseSizeBeforeCreate = questiongroupRepository.findAll().size();

        // Create the Questiongroup

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isCreated());

        // Validate the Questiongroup in the database
        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeCreate + 1);
        Questiongroup testQuestiongroup = questiongroups.get(questiongroups.size() - 1);
        assertThat(testQuestiongroup.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testQuestiongroup.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testQuestiongroup.getPosition()).isEqualTo(DEFAULT_POSITION);
        assertThat(testQuestiongroup.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testQuestiongroup.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testQuestiongroup.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testQuestiongroup.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Questiongroup in ElasticSearch
        Questiongroup questiongroupEs = questiongroupSearchRepository.findOne(testQuestiongroup.getId());
        assertThat(questiongroupEs).isEqualToComparingFieldByField(testQuestiongroup);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = questiongroupRepository.findAll().size();
        // set the field null
        questiongroup.setTitle(null);

        // Create the Questiongroup, which fails.

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isBadRequest());

        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = questiongroupRepository.findAll().size();
        // set the field null
        questiongroup.setDescription(null);

        // Create the Questiongroup, which fails.

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isBadRequest());

        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPositionIsRequired() throws Exception {
        int databaseSizeBeforeTest = questiongroupRepository.findAll().size();
        // set the field null
        questiongroup.setPosition(null);

        // Create the Questiongroup, which fails.

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isBadRequest());

        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = questiongroupRepository.findAll().size();
        // set the field null
        questiongroup.setStatus(null);

        // Create the Questiongroup, which fails.

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isBadRequest());

        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = questiongroupRepository.findAll().size();
        // set the field null
        questiongroup.setLastmodifiedby(null);

        // Create the Questiongroup, which fails.

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isBadRequest());

        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = questiongroupRepository.findAll().size();
        // set the field null
        questiongroup.setLastmodifieddatetime(null);

        // Create the Questiongroup, which fails.

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isBadRequest());

        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = questiongroupRepository.findAll().size();
        // set the field null
        questiongroup.setDomain(null);

        // Create the Questiongroup, which fails.

        restQuestiongroupMockMvc.perform(post("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(questiongroup)))
                .andExpect(status().isBadRequest());

        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllQuestiongroups() throws Exception {
        // Initialize the database
        questiongroupRepository.saveAndFlush(questiongroup);

        // Get all the questiongroups
        restQuestiongroupMockMvc.perform(get("/api/questiongroups?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(questiongroup.getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
                .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
                .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }

    @Test
    @Transactional
    public void getQuestiongroup() throws Exception {
        // Initialize the database
        questiongroupRepository.saveAndFlush(questiongroup);

        // Get the questiongroup
        restQuestiongroupMockMvc.perform(get("/api/questiongroups/{id}", questiongroup.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(questiongroup.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.position").value(DEFAULT_POSITION))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastmodifiedby").value(DEFAULT_LASTMODIFIEDBY.toString()))
            .andExpect(jsonPath("$.lastmodifieddatetime").value(DEFAULT_LASTMODIFIEDDATETIME_STR))
            .andExpect(jsonPath("$.domain").value(DEFAULT_DOMAIN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingQuestiongroup() throws Exception {
        // Get the questiongroup
        restQuestiongroupMockMvc.perform(get("/api/questiongroups/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateQuestiongroup() throws Exception {
        // Initialize the database
        questiongroupRepository.saveAndFlush(questiongroup);
        questiongroupSearchRepository.save(questiongroup);
        int databaseSizeBeforeUpdate = questiongroupRepository.findAll().size();

        // Update the questiongroup
        Questiongroup updatedQuestiongroup = questiongroupRepository.findOne(questiongroup.getId());
        updatedQuestiongroup.setTitle(UPDATED_TITLE);
        updatedQuestiongroup.setDescription(UPDATED_DESCRIPTION);
        updatedQuestiongroup.setPosition(UPDATED_POSITION);
        updatedQuestiongroup.setStatus(UPDATED_STATUS);
        updatedQuestiongroup.setLastmodifiedby(UPDATED_LASTMODIFIEDBY);
        updatedQuestiongroup.setLastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME);
        updatedQuestiongroup.setDomain(UPDATED_DOMAIN);

        restQuestiongroupMockMvc.perform(put("/api/questiongroups")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedQuestiongroup)))
                .andExpect(status().isOk());

        // Validate the Questiongroup in the database
        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeUpdate);
        Questiongroup testQuestiongroup = questiongroups.get(questiongroups.size() - 1);
        assertThat(testQuestiongroup.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testQuestiongroup.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testQuestiongroup.getPosition()).isEqualTo(UPDATED_POSITION);
        assertThat(testQuestiongroup.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testQuestiongroup.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testQuestiongroup.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testQuestiongroup.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Questiongroup in ElasticSearch
        Questiongroup questiongroupEs = questiongroupSearchRepository.findOne(testQuestiongroup.getId());
        assertThat(questiongroupEs).isEqualToComparingFieldByField(testQuestiongroup);
    }

    @Test
    @Transactional
    public void deleteQuestiongroup() throws Exception {
        // Initialize the database
        questiongroupRepository.saveAndFlush(questiongroup);
        questiongroupSearchRepository.save(questiongroup);
        int databaseSizeBeforeDelete = questiongroupRepository.findAll().size();

        // Get the questiongroup
        restQuestiongroupMockMvc.perform(delete("/api/questiongroups/{id}", questiongroup.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean questiongroupExistsInEs = questiongroupSearchRepository.exists(questiongroup.getId());
        assertThat(questiongroupExistsInEs).isFalse();

        // Validate the database is empty
        List<Questiongroup> questiongroups = questiongroupRepository.findAll();
        assertThat(questiongroups).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchQuestiongroup() throws Exception {
        // Initialize the database
        questiongroupRepository.saveAndFlush(questiongroup);
        questiongroupSearchRepository.save(questiongroup);

        // Search the questiongroup
        restQuestiongroupMockMvc.perform(get("/api/_search/questiongroups?query=id:" + questiongroup.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(questiongroup.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
            .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }
}

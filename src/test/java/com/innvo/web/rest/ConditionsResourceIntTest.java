package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Conditions;
import com.innvo.repository.ConditionsRepository;
import com.innvo.repository.search.ConditionsSearchRepository;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the ConditionsResource REST controller.
 *
 * @see ConditionsResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapAssessmentApp.class)
@WebAppConfiguration
@IntegrationTest
public class ConditionsResourceIntTest {

    private static final String DEFAULT_ACTION = "AAAAA";
    private static final String UPDATED_ACTION = "BBBBB";
    private static final String DEFAULT_OPERATOR = "AAAAA";
    private static final String UPDATED_OPERATOR = "BBBBB";
    private static final String DEFAULT_RESPONSE = "AAAAA";
    private static final String UPDATED_RESPONSE = "BBBBB";

    @Inject
    private ConditionsRepository conditionsRepository;

    @Inject
    private ConditionsSearchRepository conditionsSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restConditionsMockMvc;

    private Conditions conditions;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ConditionsResource conditionsResource = new ConditionsResource();
        ReflectionTestUtils.setField(conditionsResource, "conditionsSearchRepository", conditionsSearchRepository);
        ReflectionTestUtils.setField(conditionsResource, "conditionsRepository", conditionsRepository);
        this.restConditionsMockMvc = MockMvcBuilders.standaloneSetup(conditionsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        conditionsSearchRepository.deleteAll();
        conditions = new Conditions();
        conditions.setAction(DEFAULT_ACTION);
        conditions.setOperator(DEFAULT_OPERATOR);
        conditions.setResponse(DEFAULT_RESPONSE);
    }

    @Test
    @Transactional
    public void createConditions() throws Exception {
        int databaseSizeBeforeCreate = conditionsRepository.findAll().size();

        // Create the Conditions

        restConditionsMockMvc.perform(post("/api/conditions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(conditions)))
                .andExpect(status().isCreated());

        // Validate the Conditions in the database
        List<Conditions> conditions = conditionsRepository.findAll();
        assertThat(conditions).hasSize(databaseSizeBeforeCreate + 1);
        Conditions testConditions = conditions.get(conditions.size() - 1);
        assertThat(testConditions.getAction()).isEqualTo(DEFAULT_ACTION);
        assertThat(testConditions.getOperator()).isEqualTo(DEFAULT_OPERATOR);
        assertThat(testConditions.getResponse()).isEqualTo(DEFAULT_RESPONSE);

        // Validate the Conditions in ElasticSearch
        Conditions conditionsEs = conditionsSearchRepository.findOne(testConditions.getId());
        assertThat(conditionsEs).isEqualToComparingFieldByField(testConditions);
    }

    @Test
    @Transactional
    public void checkActionIsRequired() throws Exception {
        int databaseSizeBeforeTest = conditionsRepository.findAll().size();
        // set the field null
        conditions.setAction(null);

        // Create the Conditions, which fails.

        restConditionsMockMvc.perform(post("/api/conditions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(conditions)))
                .andExpect(status().isBadRequest());

        List<Conditions> conditions = conditionsRepository.findAll();
        assertThat(conditions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkOperatorIsRequired() throws Exception {
        int databaseSizeBeforeTest = conditionsRepository.findAll().size();
        // set the field null
        conditions.setOperator(null);

        // Create the Conditions, which fails.

        restConditionsMockMvc.perform(post("/api/conditions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(conditions)))
                .andExpect(status().isBadRequest());

        List<Conditions> conditions = conditionsRepository.findAll();
        assertThat(conditions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkResponseIsRequired() throws Exception {
        int databaseSizeBeforeTest = conditionsRepository.findAll().size();
        // set the field null
        conditions.setResponse(null);

        // Create the Conditions, which fails.

        restConditionsMockMvc.perform(post("/api/conditions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(conditions)))
                .andExpect(status().isBadRequest());

        List<Conditions> conditions = conditionsRepository.findAll();
        assertThat(conditions).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllConditions() throws Exception {
        // Initialize the database
        conditionsRepository.saveAndFlush(conditions);

        // Get all the conditions
        restConditionsMockMvc.perform(get("/api/conditions?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(conditions.getId().intValue())))
                .andExpect(jsonPath("$.[*].action").value(hasItem(DEFAULT_ACTION.toString())))
                .andExpect(jsonPath("$.[*].operator").value(hasItem(DEFAULT_OPERATOR.toString())))
                .andExpect(jsonPath("$.[*].response").value(hasItem(DEFAULT_RESPONSE.toString())));
    }

    @Test
    @Transactional
    public void getConditions() throws Exception {
        // Initialize the database
        conditionsRepository.saveAndFlush(conditions);

        // Get the conditions
        restConditionsMockMvc.perform(get("/api/conditions/{id}", conditions.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(conditions.getId().intValue()))
            .andExpect(jsonPath("$.action").value(DEFAULT_ACTION.toString()))
            .andExpect(jsonPath("$.operator").value(DEFAULT_OPERATOR.toString()))
            .andExpect(jsonPath("$.response").value(DEFAULT_RESPONSE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingConditions() throws Exception {
        // Get the conditions
        restConditionsMockMvc.perform(get("/api/conditions/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateConditions() throws Exception {
        // Initialize the database
        conditionsRepository.saveAndFlush(conditions);
        conditionsSearchRepository.save(conditions);
        int databaseSizeBeforeUpdate = conditionsRepository.findAll().size();

        // Update the conditions
        Conditions updatedConditions = new Conditions();
        updatedConditions.setId(conditions.getId());
        updatedConditions.setAction(UPDATED_ACTION);
        updatedConditions.setOperator(UPDATED_OPERATOR);
        updatedConditions.setResponse(UPDATED_RESPONSE);

        restConditionsMockMvc.perform(put("/api/conditions")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedConditions)))
                .andExpect(status().isOk());

        // Validate the Conditions in the database
        List<Conditions> conditions = conditionsRepository.findAll();
        assertThat(conditions).hasSize(databaseSizeBeforeUpdate);
        Conditions testConditions = conditions.get(conditions.size() - 1);
        assertThat(testConditions.getAction()).isEqualTo(UPDATED_ACTION);
        assertThat(testConditions.getOperator()).isEqualTo(UPDATED_OPERATOR);
        assertThat(testConditions.getResponse()).isEqualTo(UPDATED_RESPONSE);

        // Validate the Conditions in ElasticSearch
        Conditions conditionsEs = conditionsSearchRepository.findOne(testConditions.getId());
        assertThat(conditionsEs).isEqualToComparingFieldByField(testConditions);
    }

    @Test
    @Transactional
    public void deleteConditions() throws Exception {
        // Initialize the database
        conditionsRepository.saveAndFlush(conditions);
        conditionsSearchRepository.save(conditions);
        int databaseSizeBeforeDelete = conditionsRepository.findAll().size();

        // Get the conditions
        restConditionsMockMvc.perform(delete("/api/conditions/{id}", conditions.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean conditionsExistsInEs = conditionsSearchRepository.exists(conditions.getId());
        assertThat(conditionsExistsInEs).isFalse();

        // Validate the database is empty
        List<Conditions> conditions = conditionsRepository.findAll();
        assertThat(conditions).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchConditions() throws Exception {
        // Initialize the database
        conditionsRepository.saveAndFlush(conditions);
        conditionsSearchRepository.save(conditions);

        // Search the conditions
        restConditionsMockMvc.perform(get("/api/_search/conditions?query=id:" + conditions.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(conditions.getId().intValue())))
            .andExpect(jsonPath("$.[*].action").value(hasItem(DEFAULT_ACTION.toString())))
            .andExpect(jsonPath("$.[*].operator").value(hasItem(DEFAULT_OPERATOR.toString())))
            .andExpect(jsonPath("$.[*].response").value(hasItem(DEFAULT_RESPONSE.toString())));
    }
}

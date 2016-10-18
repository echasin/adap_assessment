package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Logicoperator;
import com.innvo.repository.LogicoperatorRepository;
import com.innvo.repository.search.LogicoperatorSearchRepository;

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
 * Test class for the LogicoperatorResource REST controller.
 *
 * @see LogicoperatorResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapAssessmentApp.class)
@WebAppConfiguration
@IntegrationTest
public class LogicoperatorResourceIntTest {

    private static final String DEFAULT_OPERATOR = "AAAAA";
    private static final String UPDATED_OPERATOR = "BBBBB";

    @Inject
    private LogicoperatorRepository logicoperatorRepository;

    @Inject
    private LogicoperatorSearchRepository logicoperatorSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restLogicoperatorMockMvc;

    private Logicoperator logicoperator;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LogicoperatorResource logicoperatorResource = new LogicoperatorResource();
        ReflectionTestUtils.setField(logicoperatorResource, "logicoperatorSearchRepository", logicoperatorSearchRepository);
        ReflectionTestUtils.setField(logicoperatorResource, "logicoperatorRepository", logicoperatorRepository);
        this.restLogicoperatorMockMvc = MockMvcBuilders.standaloneSetup(logicoperatorResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        logicoperatorSearchRepository.deleteAll();
        logicoperator = new Logicoperator();
        logicoperator.setOperator(DEFAULT_OPERATOR);
    }

    @Test
    @Transactional
    public void createLogicoperator() throws Exception {
        int databaseSizeBeforeCreate = logicoperatorRepository.findAll().size();

        // Create the Logicoperator

        restLogicoperatorMockMvc.perform(post("/api/logicoperators")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(logicoperator)))
                .andExpect(status().isCreated());

        // Validate the Logicoperator in the database
        List<Logicoperator> logicoperators = logicoperatorRepository.findAll();
        assertThat(logicoperators).hasSize(databaseSizeBeforeCreate + 1);
        Logicoperator testLogicoperator = logicoperators.get(logicoperators.size() - 1);
        assertThat(testLogicoperator.getOperator()).isEqualTo(DEFAULT_OPERATOR);

        // Validate the Logicoperator in ElasticSearch
        Logicoperator logicoperatorEs = logicoperatorSearchRepository.findOne(testLogicoperator.getId());
        assertThat(logicoperatorEs).isEqualToComparingFieldByField(testLogicoperator);
    }

    @Test
    @Transactional
    public void getAllLogicoperators() throws Exception {
        // Initialize the database
        logicoperatorRepository.saveAndFlush(logicoperator);

        // Get all the logicoperators
        restLogicoperatorMockMvc.perform(get("/api/logicoperators?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(logicoperator.getId().intValue())))
                .andExpect(jsonPath("$.[*].operator").value(hasItem(DEFAULT_OPERATOR.toString())));
    }

    @Test
    @Transactional
    public void getLogicoperator() throws Exception {
        // Initialize the database
        logicoperatorRepository.saveAndFlush(logicoperator);

        // Get the logicoperator
        restLogicoperatorMockMvc.perform(get("/api/logicoperators/{id}", logicoperator.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(logicoperator.getId().intValue()))
            .andExpect(jsonPath("$.operator").value(DEFAULT_OPERATOR.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingLogicoperator() throws Exception {
        // Get the logicoperator
        restLogicoperatorMockMvc.perform(get("/api/logicoperators/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateLogicoperator() throws Exception {
        // Initialize the database
        logicoperatorRepository.saveAndFlush(logicoperator);
        logicoperatorSearchRepository.save(logicoperator);
        int databaseSizeBeforeUpdate = logicoperatorRepository.findAll().size();

        // Update the logicoperator
        Logicoperator updatedLogicoperator = new Logicoperator();
        updatedLogicoperator.setId(logicoperator.getId());
        updatedLogicoperator.setOperator(UPDATED_OPERATOR);

        restLogicoperatorMockMvc.perform(put("/api/logicoperators")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedLogicoperator)))
                .andExpect(status().isOk());

        // Validate the Logicoperator in the database
        List<Logicoperator> logicoperators = logicoperatorRepository.findAll();
        assertThat(logicoperators).hasSize(databaseSizeBeforeUpdate);
        Logicoperator testLogicoperator = logicoperators.get(logicoperators.size() - 1);
        assertThat(testLogicoperator.getOperator()).isEqualTo(UPDATED_OPERATOR);

        // Validate the Logicoperator in ElasticSearch
        Logicoperator logicoperatorEs = logicoperatorSearchRepository.findOne(testLogicoperator.getId());
        assertThat(logicoperatorEs).isEqualToComparingFieldByField(testLogicoperator);
    }

    @Test
    @Transactional
    public void deleteLogicoperator() throws Exception {
        // Initialize the database
        logicoperatorRepository.saveAndFlush(logicoperator);
        logicoperatorSearchRepository.save(logicoperator);
        int databaseSizeBeforeDelete = logicoperatorRepository.findAll().size();

        // Get the logicoperator
        restLogicoperatorMockMvc.perform(delete("/api/logicoperators/{id}", logicoperator.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean logicoperatorExistsInEs = logicoperatorSearchRepository.exists(logicoperator.getId());
        assertThat(logicoperatorExistsInEs).isFalse();

        // Validate the database is empty
        List<Logicoperator> logicoperators = logicoperatorRepository.findAll();
        assertThat(logicoperators).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchLogicoperator() throws Exception {
        // Initialize the database
        logicoperatorRepository.saveAndFlush(logicoperator);
        logicoperatorSearchRepository.save(logicoperator);

        // Search the logicoperator
        restLogicoperatorMockMvc.perform(get("/api/_search/logicoperators?query=id:" + logicoperator.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(logicoperator.getId().intValue())))
            .andExpect(jsonPath("$.[*].operator").value(hasItem(DEFAULT_OPERATOR.toString())));
    }
}

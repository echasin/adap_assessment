package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Responsedetail;
import com.innvo.repository.ResponsedetailRepository;
import com.innvo.repository.search.ResponsedetailSearchRepository;

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
 * Test class for the ResponsedetailResource REST controller.
 *
 * @see ResponsedetailResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdapAssessmentApp.class)
@WebAppConfiguration
@IntegrationTest
public class ResponsedetailResourceIntTest {


    private static final Long DEFAULT_RESPONSE_ID = 1L;
    private static final Long UPDATED_RESPONSE_ID = 2L;

    private static final Long DEFAULT_QUESTIONNAIRE_ID = 1L;
    private static final Long UPDATED_QUESTIONNAIRE_ID = 2L;

    private static final Long DEFAULT_QUESTIONGROUP_ID = 1L;
    private static final Long UPDATED_QUESTIONGROUP_ID = 2L;

    private static final Long DEFAULT_QUESTION_ID = 1L;
    private static final Long UPDATED_QUESTION_ID = 2L;

    private static final Long DEFAULT_SUBQUESTION_ID = 1L;
    private static final Long UPDATED_SUBQUESTION_ID = 2L;
    private static final String DEFAULT_RESPONSE = "AAAAA";
    private static final String UPDATED_RESPONSE = "BBBBB";

    @Inject
    private ResponsedetailRepository responsedetailRepository;

    @Inject
    private ResponsedetailSearchRepository responsedetailSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restResponsedetailMockMvc;

    private Responsedetail responsedetail;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ResponsedetailResource responsedetailResource = new ResponsedetailResource();
        ReflectionTestUtils.setField(responsedetailResource, "responsedetailSearchRepository", responsedetailSearchRepository);
        ReflectionTestUtils.setField(responsedetailResource, "responsedetailRepository", responsedetailRepository);
        this.restResponsedetailMockMvc = MockMvcBuilders.standaloneSetup(responsedetailResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        responsedetailSearchRepository.deleteAll();
        responsedetail = new Responsedetail();
        responsedetail.setResponseId(DEFAULT_RESPONSE_ID);
        responsedetail.setQuestionnaireId(DEFAULT_QUESTIONNAIRE_ID);
        responsedetail.setQuestiongroupId(DEFAULT_QUESTIONGROUP_ID);
        responsedetail.setQuestionId(DEFAULT_QUESTION_ID);
        responsedetail.setSubquestionId(DEFAULT_SUBQUESTION_ID);
        responsedetail.setResponse(DEFAULT_RESPONSE);
    }

    @Test
    @Transactional
    public void createResponsedetail() throws Exception {
        int databaseSizeBeforeCreate = responsedetailRepository.findAll().size();

        // Create the Responsedetail

        restResponsedetailMockMvc.perform(post("/api/responsedetails")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(responsedetail)))
                .andExpect(status().isCreated());

        // Validate the Responsedetail in the database
        List<Responsedetail> responsedetails = responsedetailRepository.findAll();
        assertThat(responsedetails).hasSize(databaseSizeBeforeCreate + 1);
        Responsedetail testResponsedetail = responsedetails.get(responsedetails.size() - 1);
        assertThat(testResponsedetail.getResponseId()).isEqualTo(DEFAULT_RESPONSE_ID);
        assertThat(testResponsedetail.getQuestionnaireId()).isEqualTo(DEFAULT_QUESTIONNAIRE_ID);
        assertThat(testResponsedetail.getQuestiongroupId()).isEqualTo(DEFAULT_QUESTIONGROUP_ID);
        assertThat(testResponsedetail.getQuestionId()).isEqualTo(DEFAULT_QUESTION_ID);
        assertThat(testResponsedetail.getSubquestionId()).isEqualTo(DEFAULT_SUBQUESTION_ID);
        assertThat(testResponsedetail.getResponse()).isEqualTo(DEFAULT_RESPONSE);

        // Validate the Responsedetail in ElasticSearch
        Responsedetail responsedetailEs = responsedetailSearchRepository.findOne(testResponsedetail.getId());
        assertThat(responsedetailEs).isEqualToComparingFieldByField(testResponsedetail);
    }

    @Test
    @Transactional
    public void getAllResponsedetails() throws Exception {
        // Initialize the database
        responsedetailRepository.saveAndFlush(responsedetail);

        // Get all the responsedetails
        restResponsedetailMockMvc.perform(get("/api/responsedetails?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(responsedetail.getId().intValue())))
                .andExpect(jsonPath("$.[*].responseId").value(hasItem(DEFAULT_RESPONSE_ID.intValue())))
                .andExpect(jsonPath("$.[*].questionnaireId").value(hasItem(DEFAULT_QUESTIONNAIRE_ID.intValue())))
                .andExpect(jsonPath("$.[*].questiongroupId").value(hasItem(DEFAULT_QUESTIONGROUP_ID.intValue())))
                .andExpect(jsonPath("$.[*].questionId").value(hasItem(DEFAULT_QUESTION_ID.intValue())))
                .andExpect(jsonPath("$.[*].subquestionId").value(hasItem(DEFAULT_SUBQUESTION_ID.intValue())))
                .andExpect(jsonPath("$.[*].response").value(hasItem(DEFAULT_RESPONSE.toString())));
    }

    @Test
    @Transactional
    public void getResponsedetail() throws Exception {
        // Initialize the database
        responsedetailRepository.saveAndFlush(responsedetail);

        // Get the responsedetail
        restResponsedetailMockMvc.perform(get("/api/responsedetails/{id}", responsedetail.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(responsedetail.getId().intValue()))
            .andExpect(jsonPath("$.responseId").value(DEFAULT_RESPONSE_ID.intValue()))
            .andExpect(jsonPath("$.questionnaireId").value(DEFAULT_QUESTIONNAIRE_ID.intValue()))
            .andExpect(jsonPath("$.questiongroupId").value(DEFAULT_QUESTIONGROUP_ID.intValue()))
            .andExpect(jsonPath("$.questionId").value(DEFAULT_QUESTION_ID.intValue()))
            .andExpect(jsonPath("$.subquestionId").value(DEFAULT_SUBQUESTION_ID.intValue()))
            .andExpect(jsonPath("$.response").value(DEFAULT_RESPONSE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingResponsedetail() throws Exception {
        // Get the responsedetail
        restResponsedetailMockMvc.perform(get("/api/responsedetails/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateResponsedetail() throws Exception {
        // Initialize the database
        responsedetailRepository.saveAndFlush(responsedetail);
        responsedetailSearchRepository.save(responsedetail);
        int databaseSizeBeforeUpdate = responsedetailRepository.findAll().size();

        // Update the responsedetail
        Responsedetail updatedResponsedetail = new Responsedetail();
        updatedResponsedetail.setId(responsedetail.getId());
        updatedResponsedetail.setResponseId(UPDATED_RESPONSE_ID);
        updatedResponsedetail.setQuestionnaireId(UPDATED_QUESTIONNAIRE_ID);
        updatedResponsedetail.setQuestiongroupId(UPDATED_QUESTIONGROUP_ID);
        updatedResponsedetail.setQuestionId(UPDATED_QUESTION_ID);
        updatedResponsedetail.setSubquestionId(UPDATED_SUBQUESTION_ID);
        updatedResponsedetail.setResponse(UPDATED_RESPONSE);

        restResponsedetailMockMvc.perform(put("/api/responsedetails")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedResponsedetail)))
                .andExpect(status().isOk());

        // Validate the Responsedetail in the database
        List<Responsedetail> responsedetails = responsedetailRepository.findAll();
        assertThat(responsedetails).hasSize(databaseSizeBeforeUpdate);
        Responsedetail testResponsedetail = responsedetails.get(responsedetails.size() - 1);
        assertThat(testResponsedetail.getResponseId()).isEqualTo(UPDATED_RESPONSE_ID);
        assertThat(testResponsedetail.getQuestionnaireId()).isEqualTo(UPDATED_QUESTIONNAIRE_ID);
        assertThat(testResponsedetail.getQuestiongroupId()).isEqualTo(UPDATED_QUESTIONGROUP_ID);
        assertThat(testResponsedetail.getQuestionId()).isEqualTo(UPDATED_QUESTION_ID);
        assertThat(testResponsedetail.getSubquestionId()).isEqualTo(UPDATED_SUBQUESTION_ID);
        assertThat(testResponsedetail.getResponse()).isEqualTo(UPDATED_RESPONSE);

        // Validate the Responsedetail in ElasticSearch
        Responsedetail responsedetailEs = responsedetailSearchRepository.findOne(testResponsedetail.getId());
        assertThat(responsedetailEs).isEqualToComparingFieldByField(testResponsedetail);
    }

    @Test
    @Transactional
    public void deleteResponsedetail() throws Exception {
        // Initialize the database
        responsedetailRepository.saveAndFlush(responsedetail);
        responsedetailSearchRepository.save(responsedetail);
        int databaseSizeBeforeDelete = responsedetailRepository.findAll().size();

        // Get the responsedetail
        restResponsedetailMockMvc.perform(delete("/api/responsedetails/{id}", responsedetail.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean responsedetailExistsInEs = responsedetailSearchRepository.exists(responsedetail.getId());
        assertThat(responsedetailExistsInEs).isFalse();

        // Validate the database is empty
        List<Responsedetail> responsedetails = responsedetailRepository.findAll();
        assertThat(responsedetails).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchResponsedetail() throws Exception {
        // Initialize the database
        responsedetailRepository.saveAndFlush(responsedetail);
        responsedetailSearchRepository.save(responsedetail);

        // Search the responsedetail
        restResponsedetailMockMvc.perform(get("/api/_search/responsedetails?query=id:" + responsedetail.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(responsedetail.getId().intValue())))
            .andExpect(jsonPath("$.[*].responseId").value(hasItem(DEFAULT_RESPONSE_ID.intValue())))
            .andExpect(jsonPath("$.[*].questionnaireId").value(hasItem(DEFAULT_QUESTIONNAIRE_ID.intValue())))
            .andExpect(jsonPath("$.[*].questiongroupId").value(hasItem(DEFAULT_QUESTIONGROUP_ID.intValue())))
            .andExpect(jsonPath("$.[*].questionId").value(hasItem(DEFAULT_QUESTION_ID.intValue())))
            .andExpect(jsonPath("$.[*].subquestionId").value(hasItem(DEFAULT_SUBQUESTION_ID.intValue())))
            .andExpect(jsonPath("$.[*].response").value(hasItem(DEFAULT_RESPONSE.toString())));
    }
}

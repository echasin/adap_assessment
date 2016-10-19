package com.innvo.web.rest;

import com.innvo.AdapAssessmentApp;
import com.innvo.domain.Responsembr;
import com.innvo.domain.Asset;
import com.innvo.domain.Response;
import com.innvo.repository.ResponsembrRepository;
import com.innvo.repository.search.ResponsembrSearchRepository;

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
 * Test class for the ResponsembrResource REST controller.
 *
 * @see ResponsembrResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdapAssessmentApp.class)
public class ResponsembrResourceIntTest {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));
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
    private ResponsembrRepository responsembrRepository;

    @Inject
    private ResponsembrSearchRepository responsembrSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restResponsembrMockMvc;

    private Responsembr responsembr;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ResponsembrResource responsembrResource = new ResponsembrResource();
        ReflectionTestUtils.setField(responsembrResource, "responsembrSearchRepository", responsembrSearchRepository);
        ReflectionTestUtils.setField(responsembrResource, "responsembrRepository", responsembrRepository);
        this.restResponsembrMockMvc = MockMvcBuilders.standaloneSetup(responsembrResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Responsembr createEntity(EntityManager em) {
        Responsembr responsembr = new Responsembr();
        responsembr = new Responsembr()
                .status(DEFAULT_STATUS)
                .lastmodifiedby(DEFAULT_LASTMODIFIEDBY)
                .lastmodifieddatetime(DEFAULT_LASTMODIFIEDDATETIME)
                .domain(DEFAULT_DOMAIN);
        // Add required entity
        Asset asset = AssetResourceIntTest.createEntity(em);
        em.persist(asset);
        em.flush();
        responsembr.setAsset(asset);
        // Add required entity
        Response response = ResponseResourceIntTest.createEntity(em);
        em.persist(response);
        em.flush();
        responsembr.setResponse(response);
        return responsembr;
    }

    @Before
    public void initTest() {
        responsembrSearchRepository.deleteAll();
        responsembr = createEntity(em);
    }

    @Test
    @Transactional
    public void createResponsembr() throws Exception {
        int databaseSizeBeforeCreate = responsembrRepository.findAll().size();

        // Create the Responsembr

        restResponsembrMockMvc.perform(post("/api/responsembrs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(responsembr)))
                .andExpect(status().isCreated());

        // Validate the Responsembr in the database
        List<Responsembr> responsembrs = responsembrRepository.findAll();
        assertThat(responsembrs).hasSize(databaseSizeBeforeCreate + 1);
        Responsembr testResponsembr = responsembrs.get(responsembrs.size() - 1);
        assertThat(testResponsembr.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testResponsembr.getLastmodifiedby()).isEqualTo(DEFAULT_LASTMODIFIEDBY);
        assertThat(testResponsembr.getLastmodifieddatetime()).isEqualTo(DEFAULT_LASTMODIFIEDDATETIME);
        assertThat(testResponsembr.getDomain()).isEqualTo(DEFAULT_DOMAIN);

        // Validate the Responsembr in ElasticSearch
        Responsembr responsembrEs = responsembrSearchRepository.findOne(testResponsembr.getId());
        assertThat(responsembrEs).isEqualToComparingFieldByField(testResponsembr);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = responsembrRepository.findAll().size();
        // set the field null
        responsembr.setStatus(null);

        // Create the Responsembr, which fails.

        restResponsembrMockMvc.perform(post("/api/responsembrs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(responsembr)))
                .andExpect(status().isBadRequest());

        List<Responsembr> responsembrs = responsembrRepository.findAll();
        assertThat(responsembrs).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifiedbyIsRequired() throws Exception {
        int databaseSizeBeforeTest = responsembrRepository.findAll().size();
        // set the field null
        responsembr.setLastmodifiedby(null);

        // Create the Responsembr, which fails.

        restResponsembrMockMvc.perform(post("/api/responsembrs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(responsembr)))
                .andExpect(status().isBadRequest());

        List<Responsembr> responsembrs = responsembrRepository.findAll();
        assertThat(responsembrs).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLastmodifieddatetimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = responsembrRepository.findAll().size();
        // set the field null
        responsembr.setLastmodifieddatetime(null);

        // Create the Responsembr, which fails.

        restResponsembrMockMvc.perform(post("/api/responsembrs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(responsembr)))
                .andExpect(status().isBadRequest());

        List<Responsembr> responsembrs = responsembrRepository.findAll();
        assertThat(responsembrs).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDomainIsRequired() throws Exception {
        int databaseSizeBeforeTest = responsembrRepository.findAll().size();
        // set the field null
        responsembr.setDomain(null);

        // Create the Responsembr, which fails.

        restResponsembrMockMvc.perform(post("/api/responsembrs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(responsembr)))
                .andExpect(status().isBadRequest());

        List<Responsembr> responsembrs = responsembrRepository.findAll();
        assertThat(responsembrs).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllResponsembrs() throws Exception {
        // Initialize the database
        responsembrRepository.saveAndFlush(responsembr);

        // Get all the responsembrs
        restResponsembrMockMvc.perform(get("/api/responsembrs?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(responsembr.getId().intValue())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
                .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
                .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }

    @Test
    @Transactional
    public void getResponsembr() throws Exception {
        // Initialize the database
        responsembrRepository.saveAndFlush(responsembr);

        // Get the responsembr
        restResponsembrMockMvc.perform(get("/api/responsembrs/{id}", responsembr.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(responsembr.getId().intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.lastmodifiedby").value(DEFAULT_LASTMODIFIEDBY.toString()))
            .andExpect(jsonPath("$.lastmodifieddatetime").value(DEFAULT_LASTMODIFIEDDATETIME_STR))
            .andExpect(jsonPath("$.domain").value(DEFAULT_DOMAIN.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingResponsembr() throws Exception {
        // Get the responsembr
        restResponsembrMockMvc.perform(get("/api/responsembrs/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateResponsembr() throws Exception {
        // Initialize the database
        responsembrRepository.saveAndFlush(responsembr);
        responsembrSearchRepository.save(responsembr);
        int databaseSizeBeforeUpdate = responsembrRepository.findAll().size();

        // Update the responsembr
        Responsembr updatedResponsembr = responsembrRepository.findOne(responsembr.getId());
        updatedResponsembr
                .status(UPDATED_STATUS)
                .lastmodifiedby(UPDATED_LASTMODIFIEDBY)
                .lastmodifieddatetime(UPDATED_LASTMODIFIEDDATETIME)
                .domain(UPDATED_DOMAIN);

        restResponsembrMockMvc.perform(put("/api/responsembrs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedResponsembr)))
                .andExpect(status().isOk());

        // Validate the Responsembr in the database
        List<Responsembr> responsembrs = responsembrRepository.findAll();
        assertThat(responsembrs).hasSize(databaseSizeBeforeUpdate);
        Responsembr testResponsembr = responsembrs.get(responsembrs.size() - 1);
        assertThat(testResponsembr.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testResponsembr.getLastmodifiedby()).isEqualTo(UPDATED_LASTMODIFIEDBY);
        assertThat(testResponsembr.getLastmodifieddatetime()).isEqualTo(UPDATED_LASTMODIFIEDDATETIME);
        assertThat(testResponsembr.getDomain()).isEqualTo(UPDATED_DOMAIN);

        // Validate the Responsembr in ElasticSearch
        Responsembr responsembrEs = responsembrSearchRepository.findOne(testResponsembr.getId());
        assertThat(responsembrEs).isEqualToComparingFieldByField(testResponsembr);
    }

    @Test
    @Transactional
    public void deleteResponsembr() throws Exception {
        // Initialize the database
        responsembrRepository.saveAndFlush(responsembr);
        responsembrSearchRepository.save(responsembr);
        int databaseSizeBeforeDelete = responsembrRepository.findAll().size();

        // Get the responsembr
        restResponsembrMockMvc.perform(delete("/api/responsembrs/{id}", responsembr.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean responsembrExistsInEs = responsembrSearchRepository.exists(responsembr.getId());
        assertThat(responsembrExistsInEs).isFalse();

        // Validate the database is empty
        List<Responsembr> responsembrs = responsembrRepository.findAll();
        assertThat(responsembrs).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchResponsembr() throws Exception {
        // Initialize the database
        responsembrRepository.saveAndFlush(responsembr);
        responsembrSearchRepository.save(responsembr);

        // Search the responsembr
        restResponsembrMockMvc.perform(get("/api/_search/responsembrs?query=id:" + responsembr.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(responsembr.getId().intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].lastmodifiedby").value(hasItem(DEFAULT_LASTMODIFIEDBY.toString())))
            .andExpect(jsonPath("$.[*].lastmodifieddatetime").value(hasItem(DEFAULT_LASTMODIFIEDDATETIME_STR)))
            .andExpect(jsonPath("$.[*].domain").value(hasItem(DEFAULT_DOMAIN.toString())));
    }
}

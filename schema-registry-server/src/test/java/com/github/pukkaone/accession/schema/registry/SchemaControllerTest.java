package com.github.pukkaone.accession.schema.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pukkaone.accession.schema.registry.controller.CustomExceptionHandler;
import com.github.pukkaone.accession.schema.registry.controller.MediaTypes;
import com.github.pukkaone.accession.schema.registry.controller.SchemaController;
import com.github.pukkaone.accession.schema.registry.controller.SubjectController;
import com.github.pukkaone.accession.schema.registry.model.ErrorResponse;
import com.github.pukkaone.accession.schema.registry.model.SchemaRequest;
import com.github.pukkaone.accession.schema.registry.model.SchemaResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

/**
 * Tests API endpoints.
 */
@AutoConfigureMockMvc
@Import(GitTestConfiguration.class)
@SpringBootTest
class SchemaControllerTest {

  @TestConfiguration
  static class MyConfiguration {
    @Bean
    public MockMvcBuilderCustomizer mockMvcBuilderCustomizer() {
      return builder -> builder.defaultRequest(
          post("/")
              .contentType(MediaTypes.APPLICATION_SCHEMA_REGISTRY));
    }
  }

  private static final BasicJsonTester JSON = new BasicJsonTester(SchemaControllerTest.class);
  private static final String SUBJECT1 = "topic1-value";
  private static final String SUBJECT2 = "topic2-value";

  @Value("classpath:001.avsc")
  private Resource schemaFile1;

  @Value("classpath:002.avsc")
  private Resource schemaFile2;

  @Value("classpath:unregistered.avsc")
  private Resource unregisteredSchemaFile;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private static String copyToString(Resource resource) throws IOException {
    return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
  }

  @Test
  void when_get_schema_by_schema_id() throws Exception {
    String uri = SchemaController.BASE_PATH + SchemaController.GET_SCHEMA_BY_SCHEMA_ID_PATH;
    var response = mockMvc.perform(
            get(uri, 1234567001))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var registration =
        objectMapper.readValue(response.getContentAsByteArray(), SchemaResponse.class);
    assertThat(registration.getSchema()).isEqualTo(copyToString(schemaFile1));
  }

  @Test
  void when_get_subjects() throws Exception {
    var response = mockMvc.perform(
            get(SubjectController.BASE_PATH))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var responseJson = JSON.from(response.getContentAsByteArray());
    assertThat(responseJson).extractingJsonPathArrayValue("$")
        .containsExactly(SUBJECT1, SUBJECT2);
  }

  @Test
  void when_get_registration_by_schema() throws Exception {
    String uri = SubjectController.BASE_PATH +
        SubjectController.GET_REGISTRATION_BY_SUBJECT_AND_SCHEMA_PATH;
    var requestBody = SchemaRequest.builder()
        .schema(copyToString(schemaFile1))
        .build();
    var response = mockMvc.perform(
            post(uri, SUBJECT1)
            .content(objectMapper.writeValueAsBytes(requestBody)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var registration =
        objectMapper.readValue(response.getContentAsByteArray(), SchemaResponse.class);
    assertThat(registration.getId()).isEqualTo(1234567001);
    assertThat(registration.getVersion()).isEqualTo(1);
  }

  @Test
  void when_get_versions_by_subject() throws Exception {
    String uri = SubjectController.BASE_PATH +
        SubjectController.GET_SCHEMA_ID_BY_SUBJECT_AND_SCHEMA_PATH;
    var response = mockMvc.perform(
            get(uri, SUBJECT1))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var responseJson = JSON.from(response.getContentAsByteArray());
    assertThat(responseJson).extractingJsonPathArrayValue("$")
        .containsExactly(1, 2);
  }

  @Test
  void when_get_schema_id_by_schema() throws Exception {
    String uri = SubjectController.BASE_PATH +
        SubjectController.GET_SCHEMA_ID_BY_SUBJECT_AND_SCHEMA_PATH;
    var requestBody = SchemaRequest.builder()
        .schema(copyToString(schemaFile1))
        .build();
    var response = mockMvc.perform(
            post(uri, SUBJECT1)
                .content(objectMapper.writeValueAsBytes(requestBody)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var registration =
        objectMapper.readValue(response.getContentAsByteArray(), SchemaResponse.class);
    assertThat(registration.getId()).isEqualTo(1234567001);
  }

  @Test
  void given_unregistered_schema_when_get_schema_id_by_schema_then_error() throws Exception {
    String uri = SubjectController.BASE_PATH +
        SubjectController.GET_SCHEMA_ID_BY_SUBJECT_AND_SCHEMA_PATH;
    var requestBody = SchemaRequest.builder()
        .schema(copyToString(unregisteredSchemaFile))
        .build();
    var response = mockMvc.perform(
            post(uri, SUBJECT1)
                .content(objectMapper.writeValueAsBytes(requestBody)))
        .andExpect(status().isUnprocessableEntity())
        .andReturn()
        .getResponse();

    var errorResponse =
        objectMapper.readValue(response.getContentAsByteArray(), ErrorResponse.class);
    assertThat(errorResponse.getErrorCode()).isEqualTo(CustomExceptionHandler.NOT_REGISTERED_CODE);
  }

  @Test
  void when_get_registration_by_subject_and_version() throws Exception {
    String uri = SubjectController.BASE_PATH +
        SubjectController.GET_REGISTRATION_BY_SUBJECT_AND_VERSION_PATH;
    var response = mockMvc.perform(
            get(uri, SUBJECT1, 1))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var registration =
        objectMapper.readValue(response.getContentAsByteArray(), SchemaResponse.class);
    assertThat(registration.getId()).isEqualTo(1234567001);
    assertThat(registration.getVersion()).isEqualTo(1);
    assertThat(registration.getSchema()).isEqualTo(copyToString(schemaFile1));
  }

  @Test
  void when_get_registration_by_subject_and_latest_version() throws Exception {
    String uri = SubjectController.BASE_PATH +
        SubjectController.GET_REGISTRATION_BY_SUBJECT_AND_VERSION_PATH;
    var response = mockMvc.perform(
            get(uri, SUBJECT1, "latest"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var registration =
        objectMapper.readValue(response.getContentAsByteArray(), SchemaResponse.class);
    assertThat(registration.getId()).isEqualTo(1234567002);
    assertThat(registration.getVersion()).isEqualTo(2);
    assertThat(registration.getSchema()).isEqualTo(copyToString(schemaFile2));
  }

  @Test
  void when_get_schema_by_subject_and_version() throws Exception {
    String uri = SubjectController.BASE_PATH +
        SubjectController.GET_SCHEMA_BY_SUBJECT_AND_VERSION_PATH;
    var response = mockMvc.perform(
            get(uri, SUBJECT1, 1))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    var schema = response.getContentAsString();
    assertThat(schema).isEqualTo(copyToString(schemaFile1));
  }
}

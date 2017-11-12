package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@WebMvcTest
public class RamlResourceSnippetIntegrationTest implements RamlResourceSnippetTestTrait {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private String operationName;

    private ResultActions resultActions;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        operationName = UUID.randomUUID().toString();
    }

    @Test
    @SneakyThrows
    public void should_document_request() {
        givenEndpointInvoked();

        whenRamlResourceSnippetDocumentedWithoutParameters();

        then(generatedRamlFragmentFile()).exists();
        then(generatedRequestJsonFile()).exists();
        then(generatedResponseJsonFile()).exists();
    }

    @Test
    @SneakyThrows
    public void should_document_request_with_fields() {
        givenEndpointInvoked();

        whenRamlResourceSnippetDocumentedWithRequestAndResponseFields();

        then(generatedRamlFragmentFile()).exists();
        then(generatedRequestJsonFile()).exists();
        then(generatedResponseJsonFile()).exists();

        then(generatedRequestSchemaFile()).exists();
        then(generatedResponseSchemaFile()).exists();
    }

    private void whenRamlResourceSnippetDocumentedWithoutParameters() throws Exception {
        resultActions
                .andDo(
                        document(operationName, ramlResource())
                );
    }

    private void whenRamlResourceSnippetDocumentedWithRequestAndResponseFields() throws Exception {
        resultActions
                .andDo(
                        document(operationName, ramlResource(RamlResourceSnippetParameters.builder()
                                .requestFields(fieldDescriptors())
                                .responseFields(fieldDescriptors())
                                .build()))
                );
    }

    private FieldDescriptor[] fieldDescriptors() {
        return new FieldDescriptor[]{fieldWithPath("comment").description("the comment"),
                fieldWithPath("flag").description("the flag"),
                fieldWithPath("count").description("the count")};
    }

    private void givenEndpointInvoked() throws Exception {
        resultActions = mockMvc.perform(post("/some/{id}", "id")
                .contentType(APPLICATION_JSON)
                .content("{\n" +
                        "    \"comment\": \"some\",\n" +
                        "    \"flag\": true,\n" +
                        "    \"count\": 1\n" +
                        "}"))
                .andExpect(status().isOk());
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public File getRootOutputDirectory() {
        return new File("build/generated-snippets");
    }

    @SpringBootApplication
    static class TestApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestApplication.class, args);
        }
    }

    @RestController
    static class TestController {

        @PostMapping(path = "/some/{id}")
        public ResponseEntity<TestDateHolder> doSomething(@PathVariable String id,
                                                          @RequestBody TestDateHolder testDateHolder) {
            return ResponseEntity.ok(testDateHolder);
        }
    }

    @RequiredArgsConstructor
    @Getter
    static class TestDateHolder {
        private final String comment;
        private final boolean flag;
        private int count;
    }
}

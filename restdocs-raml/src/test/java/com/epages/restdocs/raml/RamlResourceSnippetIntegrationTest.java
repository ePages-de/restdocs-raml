package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.ParameterDescriptorWithRamlType.RamlScalarType.INTEGER;
import static com.epages.restdocs.raml.ParameterDescriptorWithRamlType.RamlScalarType.STRING;
import static com.epages.restdocs.raml.RamlResourceDocumentation.parameterWithName;
import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@WebMvcTest
public class RamlResourceSnippetIntegrationTest implements RamlResourceSnippetTestTrait {

    @Rule

    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    protected String operationName;

    protected ResultActions resultActions;

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
    public void should_document_request_with_description() {
        givenEndpointInvoked();

        whenRamlResourceSnippetDocumentedWithDescription();

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

    @Test
    @SneakyThrows
    public void should_document_request_with_null_field() {
        givenEndpointInvoked("null");

        assertThatCode(
            this::whenRamlResourceSnippetDocumentedWithRequestAndResponseFields
        ).doesNotThrowAnyException();
    }

    private void whenRamlResourceSnippetDocumentedWithoutParameters() throws Exception {
        resultActions
                .andDo(document(operationName, ramlResource()));
    }

    private void whenRamlResourceSnippetDocumentedWithDescription() throws Exception {
        resultActions
                .andDo(document(operationName, ramlResource("A description")));
    }

    private void whenRamlResourceSnippetDocumentedWithRequestAndResponseFields() throws Exception {
        resultActions
                .andDo(document(operationName, buildFullRamlResourceSnippet()));
    }

    protected RamlResourceSnippet buildFullRamlResourceSnippet() {
        return ramlResource(RamlResourceSnippetParameters.builder()
                .requestFields(fieldDescriptors())
                .responseFields(fieldDescriptors().and(fieldWithPath("id").description("id")))
                .requestHeaders(
                        headerWithName("X-Custom-Header").description("A custom header"),
                        headerWithName(ACCEPT).description("Accept")
                )
                .responseHeaders(
                        headerWithName("X-Custom-Header").description("A custom header"),
                        headerWithName(CONTENT_TYPE).description("ContentType")
                )
                .pathParameters(
                        parameterWithName("someId").description("some id").type(STRING),
                        parameterWithName("otherId").description("otherId id").type(INTEGER))
                .links(
                        linkWithRel("self").description("some"),
                        linkWithRel("multiple").description("multiple")
                )
                .build());
    }

    protected FieldDescriptors fieldDescriptors() {
        final ConstrainedFields fields = new ConstrainedFields(TestDataHolder.class);
        return RamlResourceDocumentation.fields(
                fields.withPath("comment").description("the comment").optional(),
                fields.withPath("flag").description("the flag"),
                fields.withMappedPath("count", "count").description("the count")
        );
    }

    protected void givenEndpointInvoked() throws Exception {
        givenEndpointInvoked("true");
    }

    protected void givenEndpointInvoked(String flagValue) throws Exception {
        resultActions = mockMvc.perform(post("/some/{someId}/other/{otherId}", "id", 1)
                .contentType(APPLICATION_JSON)
                .header("X-Custom-Header", "test")
                .accept(HAL_JSON)
                .content(String.format("{\n" +
                        "    \"comment\": \"some\",\n" +
                        "    \"flag\": %s,\n" +
                        "    \"count\": 1\n" +
                        "}", flagValue)))
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

        @PostMapping(path = "/some/{someId}/other/{otherId}")
        public ResponseEntity<Resource<TestDataHolder>> doSomething(@PathVariable String someId,
                                                                    @PathVariable Integer otherId,
                                                                    @RequestHeader("X-Custom-Header") String customHeader,
                                                                    @RequestBody TestDataHolder testDataHolder) {
            testDataHolder.setId(UUID.randomUUID().toString());
            Resource<TestDataHolder> resource = new Resource<>(testDataHolder);
            resource.add(linkTo(methodOn(TestController.class).doSomething(someId, otherId, null, null)).withSelfRel());
            resource.add(linkTo(methodOn(TestController.class).doSomething(someId, otherId, null, null)).withRel("multiple"));
            resource.add(linkTo(methodOn(TestController.class).doSomething(someId, otherId, null, null)).withRel("multiple"));
            return ResponseEntity
                    .ok()
                    .header("X-Custom-Header", customHeader)
                    .body(resource);
        }
    }

    @RequiredArgsConstructor
    @Getter
    static class TestDataHolder {
        @NotNull
        private final String comment;
        private final boolean flag;
        private int count;

        @Setter
        private String id;
    }
}

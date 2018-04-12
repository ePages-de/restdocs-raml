package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.ParameterDescriptorWithRamlType.RamlScalarType.STRING;
import static com.epages.restdocs.raml.RamlResourceDocumentation.parameterWithName;
import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.restdocs.operation.Operation;

import com.epages.restdocs.raml.RamlResourceSnippet.MissingUrlTemplateException;
import com.epages.restdocs.raml.RamlResourceSnippetParameters.RamlResourceSnippetParametersBuilder;

import lombok.SneakyThrows;

public class RamlResourceSnippetTest implements RamlResourceSnippetTestTrait {

    private static final String OPERATION_NAME = "test";

    private Operation operation;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private RamlResourceSnippetParametersBuilder parametersBuilder;

    @Before
    public void setUp() {
        parametersBuilder = RamlResourceSnippetParameters.builder();
    }

    @Test
    @SneakyThrows
    public void should_generate_raml_fragment_for_operation_with_request_body() {
        givenOperationWithRequestBody();
        givenRequestFieldDescriptors();

        whenRamlSnippetInvoked();

        thenFragmentFileExists();
        then(generatedRamlFragmentFile()).hasSameContentAs(new File("src/test/resources/expected-snippet-request-only.adoc"));
        then(generatedRequestJsonFile()).exists();
        then(generatedRequestJsonFile()).hasContent(operation.getRequest().getContentAsString());
        then(generatedResponseJsonFile()).doesNotExist();
        then(generatedRequestSchemaFile()).exists();
        then(generatedResponseSchemaFile()).doesNotExist();
    }

    @Test
    @SneakyThrows
    public void should_generate_raml_fragment_for_operation_with_request_and_response_body() {
        givenOperationWithRequestAndResponseBody();
        givenRequestFieldDescriptors();
        givenResponseFieldDescriptors();
        givenPathParameterDescriptors();
        givenRequestParameterDescriptors();

        whenRamlSnippetInvoked();

        thenFragmentFileExists();
        then(generatedRamlFragmentFile()).hasSameContentAs(new File("src/test/resources/expected-snippet-request-response.adoc"));
        then(generatedRequestJsonFile()).exists();
        then(generatedRequestJsonFile()).hasContent(operation.getRequest().getContentAsString());
        then(generatedResponseJsonFile()).exists();
        then(generatedResponseJsonFile()).hasContent(operation.getResponse().getContentAsString());
        then(generatedRequestSchemaFile()).exists();
        then(generatedResponseSchemaFile()).exists();
    }

    @Test
    @SneakyThrows
    public void should_generate_raml_fragment_for_operation_without_body() {
        givenOperationWithoutBody();

        whenRamlSnippetInvoked();

        thenFragmentFileExists();
        then(generatedRamlFragmentFile()).hasSameContentAs(new File("src/test/resources/expected-snippet-no-body.adoc"));
        then(generatedRequestJsonFile()).doesNotExist();
        then(generatedResponseJsonFile()).doesNotExist();

        then(generatedRequestSchemaFile()).doesNotExist();
        then(generatedResponseSchemaFile()).doesNotExist();
    }

    @Test
    @SneakyThrows
    public void should_fail_on_missing_url_template() {
        givenOperationWithoutUrlTemplate();

        thenThrownBy(this::whenRamlSnippetInvoked).isInstanceOf(MissingUrlTemplateException.class);
    }

    private void givenPathParameterDescriptors() {
        parametersBuilder.pathParameters(parameterWithName("id").type(STRING).description("an id"));
    }

    private void givenRequestParameterDescriptors() {
        parametersBuilder.requestParameters(parameterWithName("test-param").type(STRING).description("test param"));
    }

    @SneakyThrows
    private void thenFragmentFileExists() {
        then(generatedRamlFragmentFile()).exists();
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public File getRootOutputDirectory() {
        return temporaryFolder.getRoot();
    }

    private void givenOperationWithoutBody() {
        final OperationBuilder operationBuilder = new OperationBuilder("test", temporaryFolder.getRoot())
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}");
        operationBuilder
                .request("http://localhost:8080/some/123")
                .method("POST");
        operationBuilder
                .response()
                .status(201);
        operation = operationBuilder.build();
    }

    private void givenOperationWithoutUrlTemplate() {
        final OperationBuilder operationBuilder = new OperationBuilder("test", temporaryFolder.getRoot());
        operationBuilder
                .request("http://localhost:8080/some/123")
                .method("POST");
        operationBuilder
                .response()
                .status(201);
        operation = operationBuilder.build();
    }

    private void givenOperationWithRequestBody() {
        operation = new OperationBuilder("test", temporaryFolder.getRoot())
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}")
                .request("http://localhost:8080/some/123")
                .method("POST")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .content("{\"comment\": \"some\"}")
                .build();
    }

    private void givenRequestFieldDescriptors() {
        parametersBuilder.requestFields(fieldWithPath("comment").description("description"));
    }

    private void givenResponseFieldDescriptors() {
        parametersBuilder.responseFields(fieldWithPath("comment").description("description"));
    }

    private void givenOperationWithRequestAndResponseBody() {
        final OperationBuilder operationBuilder = new OperationBuilder("test", temporaryFolder.getRoot())
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}");
        final String content = "{\"comment\": \"some\"}";
        operationBuilder
                .request("http://localhost:8080/some/123")
                .param("test-param", "1")
                .method("POST")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .content(content);
        operationBuilder
                .response()
                .status(201)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .content(content);
        operation = operationBuilder.build();

    }

    private void whenRamlSnippetInvoked() throws IOException {
        ramlResource(parametersBuilder
                .description("some resource")
                .build()).document(operation);
    }
}

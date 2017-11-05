package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.restdocs.operation.Operation;

import lombok.SneakyThrows;

public class RamlResourceSnippetTest implements RamlResourceSnippetTestTrait {

    private static final String OPERATION_NAME = "test";

    private Operation operation;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private List<String> fragmentFileContentLines;

    @Test
    @SneakyThrows
    public void should_generate_raml_fragment_for_operation_with_request_body() {
        givenOperationWithRequestBody();

        whenRamlSnippetInvoked();

        thenFragmentFileExists();
        then(generatedRamlFragmentFile()).hasSameContentAs(new File("src/test/resources/expected-snippet-request-only.adoc"));
        then(generatedRequestJsonFile()).exists();
        then(generatedRequestJsonFile()).hasContent(operation.getRequest().getContentAsString());
        then(generatedResponseJsonFile()).doesNotExist();
    }

    @Test
    @SneakyThrows
    public void should_generate_raml_fragment_for_operation_with_request_and_response_body() {
        givenOperationWithRequestAndResponseBody();

        whenRamlSnippetInvoked();

        thenFragmentFileExists();
        then(generatedRamlFragmentFile()).hasSameContentAs(new File("src/test/resources/expected-snippet-request-response.adoc"));
        then(generatedRequestJsonFile()).exists();
        then(generatedRequestJsonFile()).hasContent(operation.getRequest().getContentAsString());
        then(generatedResponseJsonFile()).exists();
        then(generatedResponseJsonFile()).hasContent(operation.getResponse().getContentAsString());
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
    }

    @SneakyThrows
    private void thenFragmentFileExists() {
        then(generatedRamlFragmentFile()).exists();
        fragmentFileContentLines = Files.readAllLines(generatedRamlFragmentFile().toPath());
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
        final String content = "{\"comment\": \"some\"}";
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

    private void givenOperationWithRequestAndResponseBody() {
        final OperationBuilder operationBuilder = new OperationBuilder("test", temporaryFolder.getRoot())
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}");
        final String content = "{\"comment\": \"some\"}";
        operationBuilder
                .request("http://localhost:8080/some/123")
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
        ramlResource(RamlResourceSnippetParameters.builder()
                .description("some resource")
                .requestFieldDescriptors(fieldWithPath("comment").description("description"))
                .build()).document(operation);
    }
}
package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.HeaderHandler.requestHeaderHandler;
import static com.epages.restdocs.raml.HeaderHandler.responseHeaderHandler;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;

import java.util.List;
import java.util.Map;

import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.SnippetException;

public class HeaderHandlerTest {

    private Operation operation;
    private RamlResourceSnippetParameters snippetParameters;
    private Map<String, Object> model;

    @Test
    public void should_add_request_model_header() {
        givenRequestWithHeaders();
        givenDocumentedRequestHeaders();

        whenRequestModelGenerated();

        then(model).containsEntry("requestHeadersPresent", true);
        then(model).containsKey("requestHeaders");
        thenHeadersModelContainsTuples("requestHeaders",
                tuple(AUTHORIZATION, "Authorization", "Basic some"),
                tuple(ACCEPT, "Accept", HAL_JSON_VALUE)
        );
    }

    @Test
    public void should_add_response_model_header() {
        givenRequestWithHeaders();
        givenDocumentedResponseHeaders();

        whenResponseModelGenerated();

        then(model).containsEntry("responseHeadersPresent", true);
        then(model).containsKey("responseHeaders");
        thenHeadersModelContainsTuples("responseHeaders",
                tuple(CONTENT_TYPE, "ContentType", HAL_JSON_VALUE)
        );
    }

    @Test
    public void should_do_nothing_if_no_headers_documented() {
        givenRequestWithoutHeaders();
        givenNoHeadersDocumented();

        whenResponseModelGenerated();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_missing_documented_header() {
        givenRequestWithoutHeaders();
        givenDocumentedResponseHeaders();

        thenThrownBy(this::whenResponseModelGenerated).isInstanceOf(SnippetException.class);
    }

    @SuppressWarnings("unchecked")
    private void thenHeadersModelContainsTuples(String headersModelAttributeName, Tuple... expectedTuples) {
        then((List<Map<String, Object>>) model.get(headersModelAttributeName))
                .extracting(
                        m -> m.get("name"),
                        m -> m.get("description"),
                        m -> m.get("example")
                )
                .containsOnly(expectedTuples);
    }

    private void whenRequestModelGenerated() {
        model = requestHeaderHandler().generateModel(operation, snippetParameters);
    }

    private void whenResponseModelGenerated() {
        model = responseHeaderHandler().generateModel(operation, snippetParameters);
    }

    private void givenDocumentedRequestHeaders() {
        snippetParameters = RamlResourceSnippetParameters.builder()
                .requestHeaders(
                        headerWithName(AUTHORIZATION).description("Authorization"),
                        headerWithName(ACCEPT).description("Accept")
                )
                .build();
    }

    private void givenDocumentedResponseHeaders() {
        snippetParameters = RamlResourceSnippetParameters.builder()
                .responseHeaders(
                        headerWithName(CONTENT_TYPE).description("ContentType")
                )
                .build();
    }

    private void givenNoHeadersDocumented() {
        snippetParameters = RamlResourceSnippetParameters.builder().build();
    }

    private void givenRequestWithHeaders() {
        OperationBuilder operationBuilder = new OperationBuilder();

        operationBuilder
                .request("http://localhost:8080/some")
                .header(AUTHORIZATION, "Basic some")
                .header(ACCEPT, HAL_JSON_VALUE)
                .method("POST");

        operationBuilder.response()
                .header(CONTENT_TYPE, HAL_JSON_VALUE);

        operation = operationBuilder.build();
    }

    private void givenRequestWithoutHeaders() {
        OperationBuilder operationBuilder = new OperationBuilder();

        operation = operationBuilder
                .request("http://localhost:8080/some")
                .build();
    }
}

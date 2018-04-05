package com.epages.restdocs.raml;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@WebMvcTest
public class RamlDocumentationIntegrationTest extends RamlResourceSnippetIntegrationTest implements RestResourceSnippetTestTrait {

    @Test
    @SneakyThrows
    public void should_document_both_restdocs_and_raml() {
        givenEndpointInvoked();

        whenDocumentedWithRestdocsAndRaml();

        thenRestdocsAndRamlFilesExist();
    }

    @Test
    @SneakyThrows
    public void should_document_both_restdocs_and_raml_as_private_resource() {
        givenEndpointInvoked();

        whenDocumentedAsPrivateResource();

        thenRestdocsAndRamlFilesExist();
    }

    @Test
    @SneakyThrows
    public void should_document_using_the_passed_raml_snippet() {
        givenEndpointInvoked();

        whenDocumentedWithRamlSnippet();

        thenRestdocsAndRamlFilesExist();
    }

    @Test
    @SneakyThrows
    public void should_value_ignored_fields_and_links() {
        givenEndpointInvoked();

        assertThatCode(
            this::whenDocumentedWithAllFieldsLinksIgnored
        ).doesNotThrowAnyException();
    }

    private void whenDocumentedWithRestdocsAndRaml() throws Exception {
        resultActions
            .andDo(
                RamlDocumentation.document(operationName,
                    requestFields(fieldDescriptors().getFieldDescriptors()),
                    responseFields(
                        fieldWithPath("comment").description("the comment"),
                        fieldWithPath("flag").description("the flag"),
                        fieldWithPath("count").description("the count"),
                        fieldWithPath("id").description("id"),
                        subsectionWithPath("_links").ignored()
                    ),
                    links(
                            linkWithRel("self").description("some"),
                            linkWithRel("multiple").description("multiple")
                    )
                )
            );
    }

    private void whenDocumentedWithRamlSnippet() throws Exception {
        resultActions
            .andDo(
                RamlDocumentation.document(operationName,
                                           buildFullRamlResourceSnippet())
            );
    }

    private void whenDocumentedWithAllFieldsLinksIgnored() throws Exception {
        resultActions
            .andDo(
                RamlDocumentation.document(operationName,
                    requestFields(fieldDescriptors().getFieldDescriptors()),
                    responseFields(
                        fieldWithPath("comment").ignored(),
                        fieldWithPath("flag").ignored(),
                        fieldWithPath("count").ignored(),
                        fieldWithPath("id").ignored(),
                        subsectionWithPath("_links").ignored()
                    ),
                    links(
                            linkWithRel("self").optional().ignored(),
                            linkWithRel("multiple").optional().ignored()
                    )
                )
            );
    }

    private void whenDocumentedAsPrivateResource() throws Exception {
        OperationRequestPreprocessor operationRequestPreprocessor = r -> { return r;};
        resultActions
                .andDo(
                        RamlDocumentation.document(operationName,
                                true,
                                operationRequestPreprocessor,
                                requestFields(fieldDescriptors().getFieldDescriptors()),
                                responseFields(
                                        fieldWithPath("comment").description("the comment"),
                                        fieldWithPath("flag").description("the flag"),
                                        fieldWithPath("count").description("the count"),
                                        fieldWithPath("id").description("id"),
                                        subsectionWithPath("_links").ignored()
                                ),
                                links(
                                        linkWithRel("self").description("some"),
                                        linkWithRel("multiple").description("multiple")
                                )
                        )
                );
    }

    private void thenRestdocsAndRamlFilesExist() {
        then(generatedRamlFragmentFile()).exists();
        then(generatedRequestJsonFile()).exists();
        then(generatedResponseJsonFile()).exists();
        then(generatedRequestSchemaFile()).exists();
        then(generatedResponseSchemaFile()).exists();

        then(generatedCurlAdocFile()).exists();
        then(generatedHttpRequestAdocFile()).exists();
        then(generatedHttpResponseAdocFile()).exists();
    }

}

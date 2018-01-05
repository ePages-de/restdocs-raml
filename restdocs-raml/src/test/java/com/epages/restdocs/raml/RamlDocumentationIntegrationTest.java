package com.epages.restdocs.raml;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
    public void should_document_using_the_passed_raml_snippet() {
        givenEndpointInvoked();

        whenDocumentedWithRamlSnippet();

        thenRestdocsAndRamlFilesExist();
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
                        //subsectionWithPath("_links").ignored()
                        fieldWithPath("_links").ignored()
                    ),
                    links(linkWithRel("self").description("some"))
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

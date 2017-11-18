package com.epages.restdocs.raml;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;

import java.util.Map;

import org.junit.Test;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.SnippetException;

public class LinkHandlerTest {

    private Operation operation;

    private LinkHandler linkHandler = new LinkHandler();

    private Map<String, Object> model;

    @Test
    public void should_do_nothing_if_no_links_are_documented() {
        givenResponseWithJsonBody();

        whenModelGeneratedWithLinkDescriptors();

        then(model).isEmpty();
    }

    @Test
    public void should_do_nothing_if_documented_links_are_valid() {
        givenResponseWithJsonBody();

        whenModelGeneratedWithLinkDescriptors(linkWithRel("self").description("self"));

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_undocumented_links() {
        givenResponseWithJsonBody();

        thenThrownBy(() -> whenModelGeneratedWithLinkDescriptors(linkWithRel("other").description("self")))
                .isInstanceOf(SnippetException.class);
    }

    @Test
    public void should_fail_on_non_existing_documented_fields() {
        givenResponseWithJsonBody();

        thenThrownBy(() -> whenModelGeneratedWithLinkDescriptors(
                linkWithRel("self").description("self"),
                linkWithRel("other").description("self")
        )).isInstanceOf(SnippetException.class);
    }

    private void whenModelGeneratedWithLinkDescriptors(LinkDescriptor... linkDescriptors) {
        model = linkHandler.generateModel(operation, RamlResourceSnippetParameters.builder()
                .links(linkDescriptors)
                .build());
    }

    private void givenResponseWithJsonBody() {
        operation = new OperationBuilder()
                .response()
                .status(200)
                .header(CONTENT_TYPE, HAL_JSON_VALUE)
                .content("{\n" +
                        "    \"comment\":\"some\",\n" +
                        "    \"_links\": {\n" +
                        "        \"self\": {\n" +
                        "            \"href\": \"http://localhost/some/id\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
                .build();
    }
}
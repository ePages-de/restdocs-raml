package com.epages.restdocs.raml;

import org.junit.Test;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.restdocs.snippet.AbstractDescriptor;

import java.util.List;

import static com.epages.restdocs.raml.DescriptorExtractor.extract;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;

public class DescriptorExtractorTest {

    @Test
    public void should_extract_request_field_descriptors() {
        // given
        RequestFieldsSnippet snippet = requestFields(
                fieldWithPath("object.field").description("Is documented!")
        );

        // when
        List<FieldDescriptor> descriptors = extract(snippet);

        then(descriptors).hasSize(1);
        then(descriptors.get(0).getPath()).isEqualTo("object.field");
        then(descriptors.get(0).getDescription()).isEqualTo("Is documented!");
    }

    @Test
    public void should_extract_response_field_descriptors() {
        // given
        ResponseFieldsSnippet snippet = responseFields(
            fieldWithPath("object.field").description("Is documented!"),
            fieldWithPath("object.anotherField").description("Is documented, too!")
        );

        // when
        List<FieldDescriptor> descriptors = extract(snippet);

        then(descriptors).hasSize(2);
        then(descriptors.stream().map(FieldDescriptor::getPath).collect(toList()))
                .containsExactly("object.field", "object.anotherField");
        then(descriptors.stream().map(AbstractDescriptor::getDescription).collect(toList()))
                .containsExactly("Is documented!", "Is documented, too!");
    }

    @Test
    public void should_extract_link_descriptors() {
        // given
        LinksSnippet snippet = links(
            linkWithRel("self").description("Is documented!")
        );

        // when
        List<LinkDescriptor> descriptors = extract(snippet);

        then(descriptors).hasSize(1);
        then(descriptors.get(0).getRel()).isEqualTo("self");
        then(descriptors.get(0).getDescription()).isEqualTo("Is documented!");
    }

    @Test
    public void should_extract_request_parameter_descriptors() {
        // given
        RequestParametersSnippet snippet = requestParameters(
            parameterWithName("page").description("Is documented!"),
            parameterWithName("elementsPerPage").description("Is documented!")
        );

        // when
        List<ParameterDescriptor> descriptors = extract(snippet);

        then(descriptors).hasSize(2);
        then(descriptors.stream().map(ParameterDescriptor::getName).collect(toList()))
                .containsExactly("page", "elementsPerPage");
        then(descriptors.stream().map(AbstractDescriptor::getDescription).collect(toList()))
                .containsExactly("Is documented!", "Is documented!");
    }
}

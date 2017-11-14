package com.epages.restdocs.raml;

import static java.util.Collections.emptyList;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RamlResourceSnippetParameters {

    private final String description;
    private final boolean privateResource;
    private final List<FieldDescriptor> requestFields;
    private final List<FieldDescriptor> responseFields;
    private final List<LinkDescriptor> links;

    protected List<FieldDescriptor> getResponseFieldsWithLinks() {
        List<FieldDescriptor> combinedDescriptors = new ArrayList<>(getResponseFields());
        combinedDescriptors.addAll(
                getLinks().stream()
                        .map(l -> fieldWithPath("_links." + l.getRel()).description(l.getDescription()).type(JsonFieldType.OBJECT))
                        .collect(Collectors.toList())
        );
        return combinedDescriptors;
    }

    public static class RamlResourceSnippetParametersBuilder {

        private List<FieldDescriptor> requestFields = emptyList();
        private List<FieldDescriptor> responseFields = emptyList();
        private List<LinkDescriptor> links = emptyList();

        public RamlResourceSnippetParametersBuilder requestFields(FieldDescriptor... requestFields) {
            this.requestFields = Arrays.asList(requestFields);
            return this;
        }

        public RamlResourceSnippetParametersBuilder requestFields(FieldDescriptors requestFields) {
            this.requestFields = requestFields.getFieldDescriptors();
            return this;
        }

        public RamlResourceSnippetParametersBuilder responseFields(FieldDescriptor... responseFields) {
            this.responseFields = Arrays.asList(responseFields);
            return this;
        }

        public RamlResourceSnippetParametersBuilder responseFields(FieldDescriptors responseFields) {
            this.responseFields = responseFields.getFieldDescriptors();
            return this;
        }

        public RamlResourceSnippetParametersBuilder links(LinkDescriptor... links) {
            this.links = Arrays.asList(links);
            return this;
        }
    }
}

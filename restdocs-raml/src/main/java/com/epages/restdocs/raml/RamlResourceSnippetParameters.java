package com.epages.restdocs.raml;

import static java.util.Collections.emptyList;

import java.util.Arrays;
import java.util.List;

import org.springframework.restdocs.payload.FieldDescriptor;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RamlResourceSnippetParameters {

    private final String description;
    private final boolean privateResource;
    private final List<FieldDescriptor> requestFields;
    private final List<FieldDescriptor> responseFields;

    public static class RamlResourceSnippetParametersBuilder {

        private List<FieldDescriptor> requestFields = emptyList();
        private List<FieldDescriptor> responseFields = emptyList();

        public RamlResourceSnippetParametersBuilder requestFields(FieldDescriptor... requestFields) {
            this.requestFields = Arrays.asList(requestFields);
            return this;
        }

        public RamlResourceSnippetParametersBuilder responseFields(FieldDescriptor... responseFields) {
            this.responseFields = Arrays.asList(responseFields);
            return this;
        }
    }
}

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
    private final List<FieldDescriptor> requestFieldDescriptors;
    private final List<FieldDescriptor> responseFieldDescriptors;

    public static class RamlResourceSnippetParametersBuilder {

        private List<FieldDescriptor> requestFieldDescriptors = emptyList();
        private List<FieldDescriptor> responseFieldDescriptors = emptyList();

        public RamlResourceSnippetParametersBuilder requestFieldDescriptors(FieldDescriptor... requestFieldDescriptors) {
            this.requestFieldDescriptors = Arrays.asList(requestFieldDescriptors);
            return this;
        }

        public RamlResourceSnippetParametersBuilder responseFieldDescriptors(FieldDescriptor... responseFieldDescriptors) {
            this.responseFieldDescriptors = Arrays.asList(responseFieldDescriptors);
            return this;
        }
    }
}

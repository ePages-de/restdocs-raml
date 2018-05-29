package com.epages.restdocs.raml;

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.Attributes.Attribute;
import org.springframework.util.ReflectionUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor(access = PRIVATE)
@Getter
public class RamlResourceSnippetParameters {

    private final String description;
    private final boolean privateResource;
    private final List<FieldDescriptor> requestFields;
    private final List<FieldDescriptor> responseFields;
    private final List<LinkDescriptor> links;
    private final List<ParameterDescriptorWithRamlType> pathParameters;
    private final List<ParameterDescriptorWithRamlType> requestParameters;
    private final List<HeaderDescriptor> requestHeaders;
    private final List<HeaderDescriptor> responseHeaders;

    List<FieldDescriptor> getResponseFieldsWithLinks() {
        List<FieldDescriptor> combinedDescriptors = new ArrayList<>(getResponseFields());
        combinedDescriptors.addAll(
                getLinks().stream()
                        .map(RamlResourceSnippetParameters::toFieldDescriptor)
                        .collect(Collectors.toList())
        );
        return combinedDescriptors;
    }

    private static FieldDescriptor toFieldDescriptor(LinkDescriptor linkDescriptor) {

        FieldDescriptor descriptor = createLinkFieldDescriptor(linkDescriptor.getRel())
                .description(linkDescriptor.getDescription())
                .type(JsonFieldType.VARIES)
                .attributes(linkDescriptor.getAttributes().entrySet().stream()
                        .map(e -> new Attribute(e.getKey(), e.getValue()))
                        .toArray(Attribute[]::new));

        if (linkDescriptor.isOptional()) {
            descriptor = descriptor.optional();
        }
        if (linkDescriptor.isIgnored()) {
            descriptor = descriptor.ignored();
        }

        return descriptor;
    }

    /**
     * Behaviour changed from restdocs 1.1 to restdocs 1.2
     * In 1.2 you need to document attributes inside the object when documenting the object with fieldWithPath - which was not the case with 1.1
     * So we need to use subsectionWithPath if we are working with 1.2 and fieldWithPath otherwise
     * @param rel
     * @return
     */
    private static FieldDescriptor createLinkFieldDescriptor(String rel) {
        String path = "_links." + rel;
        return (FieldDescriptor) Optional.ofNullable(ReflectionUtils.findMethod(PayloadDocumentation.class, "subsectionWithPath", String.class))
                .map(m -> ReflectionUtils.invokeMethod(m, null, path))
                .orElseGet(() -> fieldWithPath(path));
    }

    public static class RamlResourceSnippetParametersBuilder {

        private List<FieldDescriptor> requestFields = emptyList();
        private List<FieldDescriptor> responseFields = emptyList();
        private List<LinkDescriptor> links = emptyList();
        private List<ParameterDescriptorWithRamlType> pathParameters = emptyList();
        private List<ParameterDescriptorWithRamlType> requestParameters = emptyList();
        private List<HeaderDescriptor> requestHeaders = emptyList();
        private List<HeaderDescriptor> responseHeaders = emptyList();

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

        public RamlResourceSnippetParametersBuilder pathParameters(ParameterDescriptor... pathParameters) {
            this.pathParameters = Stream.of(pathParameters).map(ParameterDescriptorWithRamlType::from).collect(Collectors.toList());
            return this;
        }

        public RamlResourceSnippetParametersBuilder pathParameters(ParameterDescriptorWithRamlType... pathParameters) {
            this.pathParameters = Arrays.asList(pathParameters);
            return this;
        }

        public RamlResourceSnippetParametersBuilder requestParameters(ParameterDescriptor... requestParameters) {
            this.requestParameters = Stream.of(requestParameters).map(ParameterDescriptorWithRamlType::from).collect(Collectors.toList());
            return this;
        }

        public RamlResourceSnippetParametersBuilder requestParameters(ParameterDescriptorWithRamlType... requestParameters) {
            this.requestParameters = Arrays.asList(requestParameters);
            return this;
        }

        public RamlResourceSnippetParametersBuilder requestHeaders(HeaderDescriptor... requestHeaders) {
            this.requestHeaders = Arrays.asList(requestHeaders);
            return this;
        }

        public RamlResourceSnippetParametersBuilder responseHeaders(HeaderDescriptor... responseHeaders) {
            this.responseHeaders = Arrays.asList(responseHeaders);
            return this;
        }
    }
}

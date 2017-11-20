package com.epages.restdocs.raml;

import org.springframework.restdocs.payload.FieldDescriptor;

public abstract class RamlResourceDocumentation {

    public static RamlResourceSnippet ramlResource(RamlResourceSnippetParameters ramlResourceSnippetParameters) {
        return new RamlResourceSnippet(ramlResourceSnippetParameters);
    }

    public static RamlResourceSnippet ramlResource() {
        return new RamlResourceSnippet(RamlResourceSnippetParameters.builder().build());
    }

    public static FieldDescriptors fields(FieldDescriptor... fieldDescriptors) {
        return new FieldDescriptors(fieldDescriptors);
    }

    public static ParameterDescriptorWithRamlType parameterWithName(String name) {
        return new ParameterDescriptorWithRamlType(name);
    }
}

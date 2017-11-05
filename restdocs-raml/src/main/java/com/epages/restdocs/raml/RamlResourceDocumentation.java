package com.epages.restdocs.raml;

public class RamlResourceDocumentation {

    public static RamlResourceSnippet ramlResource(RamlResourceSnippetParameters ramlResourceSnippetParameters) {
        return new RamlResourceSnippet(ramlResourceSnippetParameters);
    }

    public static RamlResourceSnippet ramlResource() {
        return new RamlResourceSnippet(RamlResourceSnippetParameters.builder().build());
    }
}

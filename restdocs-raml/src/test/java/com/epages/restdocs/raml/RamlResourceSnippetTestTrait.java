package com.epages.restdocs.raml;

import java.io.File;

public interface RamlResourceSnippetTestTrait {

    String RAML_FRAGMENT_FILE = "raml-resource.adoc";
    String REQUEST_FILE_SUFFIX = "-request.json";
    String RESPONSE_FILE_SUFFIX = "-response.json";

    String getOperationName();

    File getRootOutputDirectory();

    default File generatedRamlFragmentFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + RAML_FRAGMENT_FILE);
    }

    default File generatedRequestJsonFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + getOperationName() + REQUEST_FILE_SUFFIX);
    }

    default File generatedResponseJsonFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + getOperationName() + RESPONSE_FILE_SUFFIX);
    }
}

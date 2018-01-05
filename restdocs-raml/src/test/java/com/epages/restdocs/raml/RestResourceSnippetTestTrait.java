package com.epages.restdocs.raml;

import java.io.File;

public interface RestResourceSnippetTestTrait {

    String CURL_REQUEST_FILE = "curl-request.adoc";
    String HTTP_REQUEST_FILE = "http-request.adoc";
    String HTTP_RESPONSE_FILE = "http-response.adoc";
    String REQUEST_BODY_FILE = "request-body.adoc";
    String RESPONSE_BODY_FILE = "response-body.adoc";

    String getOperationName();

    File getRootOutputDirectory();

    default File generatedCurlAdocFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + CURL_REQUEST_FILE);
    }

    default File generatedHttpRequestAdocFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + HTTP_REQUEST_FILE);
    }

    default File generatedHttpResponseAdocFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + HTTP_RESPONSE_FILE);
    }

    default File generatedRequestBodyAdocFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + REQUEST_BODY_FILE);
    }

    default File generatedResponseBodyAdocFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + RESPONSE_BODY_FILE);
    }

}

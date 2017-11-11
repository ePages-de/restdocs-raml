package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.RequestHandler.REQUEST_BODY_FILE_NAME_SUFFIX;
import static com.epages.restdocs.raml.ResponseHandler.RESPONSE_BODY_FILE_NAME_SUFFIX;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.TemplatedSnippet;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class RamlResourceSnippet extends TemplatedSnippet {

    private static final String SNIPPET_NAME = "raml-resource";

    private final RamlResourceSnippetParameters parameters;

    private final OperationHandlerChain handlerChain;

    protected RamlResourceSnippet(RamlResourceSnippetParameters parameters) {
        super(SNIPPET_NAME, null);
        this.parameters = parameters;

        handlerChain = new OperationHandlerChain(Arrays.asList(
                new JwtScopeHandler(),
                new RequestHandler(),
                new ResponseHandler(),
                new TraitExtractorChain(Arrays.asList(new PrivateResourceTraitExtractor()))));
    }

    @Override
    protected Map<String, Object> createModel(Operation operation) {
        Map<String, Object> model = new HashMap<>();
        model.put("method", operation.getRequest().getMethod().name().toLowerCase());
        model.put("description", parameters.getDescription() == null ? operation.getName() : parameters.getDescription());
        model.put("resource", getUriPath(operation));
        model.put("status", operation.getResponse().getStatus().value());

        model.putAll(handlerChain.process(operation, parameters));

        return model;
    }

    @Override
    public void document(Operation operation) throws IOException {
        super.document(operation);

        storeRequestBody(operation);

        storeResponseBody(operation);
    }

    private void storeRequestBody(Operation operation) {
        if (!StringUtils.isEmpty(operation.getRequest().getContentAsString())) {
            storeBodyJson(operation, getRequestFileName(operation), operation.getRequest().getContentAsString());
        }
    }

    private String getRequestFileName(Operation operation) {
        return operation.getName() + REQUEST_BODY_FILE_NAME_SUFFIX;
    }

    private void storeResponseBody(Operation operation) {
        if (!StringUtils.isEmpty(operation.getResponse().getContentAsString())) {
            storeBodyJson(operation, getResponseFileName(operation), operation.getResponse().getContentAsString());
        }
    }

    private String getResponseFileName(Operation operation) {
        return operation.getName() + RESPONSE_BODY_FILE_NAME_SUFFIX;
    }

    private void storeBodyJson(Operation operation, String filename, String content) {
        RestDocumentationContext context = (RestDocumentationContext) operation
                .getAttributes().get(RestDocumentationContext.class.getName());

        File output = new File(context.getOutputDirectory(), operation.getName() + "/" + filename);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(output.toPath()))) {
            writer.append(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getUriPath(Operation operation) {
        return UriComponentsBuilder.fromUriString(((String) operation.getAttributes().get(ATTRIBUTE_NAME_URL_TEMPLATE))).build().getPath();
    }
}

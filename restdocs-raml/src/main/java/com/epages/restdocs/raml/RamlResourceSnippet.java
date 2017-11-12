package com.epages.restdocs.raml;

import static java.util.Collections.singletonList;
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

import com.epages.restdocs.raml.jsonschema.JsonSchemaFromFieldDescriptorsGenerator;

public class RamlResourceSnippet extends TemplatedSnippet implements FileNameTrait {

    private static final String SNIPPET_NAME = "raml-resource";

    private final RamlResourceSnippetParameters parameters;

    private final OperationHandlerChain handlerChain;

    private final JsonSchemaFromFieldDescriptorsGenerator jsonSchemasGenerator = new JsonSchemaFromFieldDescriptorsGenerator();

    protected RamlResourceSnippet(RamlResourceSnippetParameters parameters) {
        super(SNIPPET_NAME, null);
        this.parameters = parameters;

        handlerChain = new OperationHandlerChain(Arrays.asList(
                new JwtScopeHandler(),
                new RequestHandler(),
                new ResponseHandler(),
                new TraitExtractorChain(singletonList(new PrivateResourceTraitExtractor()))));
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

        storeRequestJsonSchema(operation);

        storeResponseJsonSchema(operation);
    }

    private void storeRequestJsonSchema(Operation operation) {
        if (shouldGenerateRequestSchemaFile(operation, parameters)) {
            storeFile(operation, getRequestSchemaFileName(operation.getName()),
                    jsonSchemasGenerator.generateSchema(parameters.getRequestFields()));
        }
    }

    private void storeResponseJsonSchema(Operation operation) {
        if (shouldGenerateResponseSchemaFile(operation, parameters)) {
            storeFile(operation, getResponseSchemaFileName(operation.getName()),
                    jsonSchemasGenerator.generateSchema(parameters.getResponseFields()));
        }
    }
    private void storeRequestBody(Operation operation) {
        if (!StringUtils.isEmpty(operation.getRequest().getContentAsString())) {
            storeFile(operation, getRequestFileName(operation.getName()), operation.getRequest().getContentAsString());
        }
    }

    private void storeResponseBody(Operation operation) {
        if (!StringUtils.isEmpty(operation.getResponse().getContentAsString())) {
            storeFile(operation, getResponseFileName(operation.getName()), operation.getResponse().getContentAsString());
        }
    }

    private void storeFile(Operation operation, String filename, String content) {
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

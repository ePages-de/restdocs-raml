package com.epages.restdocs.raml;

import static com.epages.restdocs.raml.HeaderHandler.requestHeaderHandler;
import static com.epages.restdocs.raml.HeaderHandler.responseHeaderHandler;
import static java.util.Collections.singletonList;
import static org.springframework.restdocs.config.SnippetConfigurer.DEFAULT_SNIPPET_ENCODING;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.TemplatedSnippet;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.StandardTemplateResourceResolver;
import org.springframework.restdocs.templates.TemplateEngine;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.restdocs.templates.mustache.MustacheTemplateEngine;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.epages.restdocs.raml.jsonschema.JsonSchemaFromFieldDescriptorsGenerator;

public class RamlResourceSnippet extends TemplatedSnippet implements FileNameTrait {

    private static final String SNIPPET_NAME = "raml-resource";

    private final RamlResourceSnippetParameters parameters;

    private final OperationHandlerChain handlerChain;

    private final JsonSchemaFromFieldDescriptorsGenerator jsonSchemasGenerator = new JsonSchemaFromFieldDescriptorsGenerator();

    RamlResourceSnippet(RamlResourceSnippetParameters parameters) {
        super(SNIPPET_NAME, null);
        this.parameters = parameters;

        handlerChain = new OperationHandlerChain(Arrays.asList(
                new JwtScopeHandler(),
                new RequestHandler(),
                new ResponseHandler(),
                new LinkHandler(),
                new PathParameterHandler(),
                new RequestParameterHandler(),
                requestHeaderHandler(),
                responseHeaderHandler(),
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
        documentSnippet(operation);

        storeRequestBody(operation);

        storeResponseBody(operation);

        storeRequestJsonSchema(operation);

        storeResponseJsonSchema(operation);
    }

    private void documentSnippet(Operation operation) throws IOException {

        WriterResolver writerResolver = new StandardWriterResolver(new RestDocumentationContextPlaceholderResolverFactory(), DEFAULT_SNIPPET_ENCODING, new RamlTemplateFormat());
        try (Writer writer = writerResolver.resolve(operation.getName(), SNIPPET_NAME,
                (RestDocumentationContext) operation.getAttributes().get(RestDocumentationContext.class.getName()))) {
            Map<String, Object> model = createModel(operation);
            TemplateEngine templateEngine = new MustacheTemplateEngine(new StandardTemplateResourceResolver(new RamlTemplateFormat()));
            writer.append(templateEngine.compileTemplate(SNIPPET_NAME).render(model));
        }
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
                    jsonSchemasGenerator.generateSchema(parameters.getResponseFieldsWithLinks()));
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
        File output = getOutputFile(operation, filename);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(output.toPath()))) {
            writer.append(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private File getOutputFile(Operation operation, String filename) {
        Object context = operation.getAttributes().get(RestDocumentationContext.class.getName());
        try {
            //use reflection here because of binary incompatibility between spring-restdocs 1 and 2
            //RestDocumentationContext changed from a class to an interface
            //if our code should work against both versions we need to avoid compiling against a version directly
            //see https://github.com/ePages-de/restdocs-raml/issues/7
            //we can remove the use of reflection when we drop support for spring-restdocs 1
            Method getOutputDirectory = context.getClass().getDeclaredMethod("getOutputDirectory");
            getOutputDirectory.setAccessible(true);
            File outputFile = (File) getOutputDirectory.invoke(context);
            return new File(outputFile, operation.getName() + "/" + filename);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private String getUriPath(Operation operation) {
        String urlTemplate = (String) operation.getAttributes().get(ATTRIBUTE_NAME_URL_TEMPLATE);
        if (StringUtils.isEmpty(urlTemplate)) {
            throw new MissingUrlTemplateException();
        }
        return UriComponentsBuilder.fromUriString(urlTemplate).build().getPath();
    }

    static class RamlTemplateFormat implements TemplateFormat {

        @Override
        public String getId() {
            return "raml";
        }

        @Override
        public String getFileExtension() {
            return "raml";
        }
    }

    static class MissingUrlTemplateException extends RuntimeException {
        public MissingUrlTemplateException() {
            super("Missing URL template - please use RestDocumentationRequestBuilders with urlTemplate to construct the request");
        }
    }
}

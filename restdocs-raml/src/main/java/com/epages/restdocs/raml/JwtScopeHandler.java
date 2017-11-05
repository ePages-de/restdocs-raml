package com.epages.restdocs.raml;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.operation.Operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtScopeHandler implements OperationHandler {

    @Override
    public Map<String, Object> generateModel(Operation operation, RamlResourceSnippetParameters ramlResourceSnippetParameters) {
        return handleScopes(operation);
    }

    private Map<String, Object> handleScopes(Operation operation) {
        Map<String, Object> model = new HashMap<>();
        if (operation.getRequest().getHeaders().getFirst("Authorization") != null) {
            String jwt = operation.getRequest().getHeaders().getFirst("Authorization")
                    .replace("Bearer ", "");
            List<String> scopes = jwt2scopes(jwt).stream().map(s -> "\"" + s + "\"").collect(toList());
            if (!scopes.isEmpty()) {
                String renderedScopes = "[" + String.join(", ", scopes) + "]";
                model.put("scopes", renderedScopes);
            }
        }
        return model;
    }

    @SuppressWarnings("unchecked")
    private static List<String> jwt2scopes(final String jwt) {
        String[] jwtParts = jwt.split("\\.");
        if (jwtParts.length >= 2) { // JWT = header, payload, signature; at least the first two should be there
            String jwtPayload = jwtParts[1];
            String decodedPayload = new String(Base64.getDecoder().decode(jwtPayload));
            try {
                Map<String, Object> jwtMap =  new ObjectMapper().readValue(decodedPayload, new TypeReference<Map<String, Object>>(){});
                Object scope = jwtMap.get("scope");
                if (scope instanceof List) {
                    return (List<String>) scope;
                }
            } catch (IOException e) {
                //probably not JWT
            }
        }

        return emptyList();
    }
}

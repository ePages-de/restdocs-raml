package com.epages.restdocs.raml;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.payload.FieldDescriptor;

public abstract class AbstractFieldHandler implements OperationHandler {

    protected List<Map<Object, Object>> transformDescriptorsToModel(List<FieldDescriptor> fieldDescriptors) {
        String lastPath = fieldDescriptors.get(fieldDescriptors.size() - 1).getPath();
        return fieldDescriptors.stream().map(field -> {
            Map<Object, Object> fieldMap = new HashMap<>();
            fieldMap.put("path", field.getPath());
            fieldMap.put("description", field.getDescription());
            fieldMap.put("type", field.getType() == null ? "" : field.getType().toString().toLowerCase());
            fieldMap.put("attributes", field.getAttributes());
            fieldMap.put("required", !field.isOptional());
            fieldMap.put("last", field.getPath().equals(lastPath));
            return fieldMap;
        }).collect(toList());
    }
}

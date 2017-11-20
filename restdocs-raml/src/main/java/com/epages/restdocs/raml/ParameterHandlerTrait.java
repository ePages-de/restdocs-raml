package com.epages.restdocs.raml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

interface ParameterHandlerTrait {

    default List<Map<String, String>> mapParameterDescriptorsToModel(List<ParameterDescriptorWithRamlType> pathParameters) {
        return pathParameters.stream().map(parameterDescriptor -> {
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("name", parameterDescriptor.getName());
            parameterMap.put("description", (String) parameterDescriptor.getDescription());
            parameterMap.put("type", parameterDescriptor.getType().getTypeName());
            return parameterMap;
        }).collect(Collectors.toList());
    }
}

package com.epages.restdocs.raml.jsonschema;

import static com.epages.restdocs.raml.jsonschema.JsonFieldPath.isArraySegment;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.internal.JSONPrinter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSchemaFromFieldDescriptorsGenerator {


    public String generateSchema(List<FieldDescriptor> fieldDescriptors) {
        return generateSchema(fieldDescriptors, null);
    }

    public String generateSchema(List<FieldDescriptor> fieldDescriptors, String title) {
        List<JsonFieldPath> jsonFieldPaths = distinct(fieldDescriptors).stream()
                .map(JsonFieldPath::compile)
                .collect(toList());

        Schema schema = traverse(emptyList(), jsonFieldPaths, (ObjectSchema.Builder) ObjectSchema.builder().title(title));

        return toFormattedString(unWrapRootArray(jsonFieldPaths, schema));
    }

    /**
     * Make sure that the paths of the FieldDescriptors are distinct
     * If we find multiple descriptors for the same path that are completely equal we take the first one.
     * @throws MultipleNonEqualFieldDescriptors in case we find multiple descriptors for the same path that are not equal
     */
    private List<FieldDescriptor> distinct(List<FieldDescriptor> fieldDescriptors) {
        return fieldDescriptors.stream()
                .collect(Collectors.groupingBy(FieldDescriptor::getPath))
                .values().stream()
                .map(this::reduceToSingleIfAllEqual)
                .collect(toList())
        ;
    }

    private FieldDescriptor reduceToSingleIfAllEqual(List<FieldDescriptor> fieldDescriptors) {
        if (fieldDescriptors.size() == 1) {
            return fieldDescriptors.get(0);
        }
        FieldDescriptor first = fieldDescriptors.get(0);
        final boolean hasDifferentDiscriptors = fieldDescriptors.subList(1, fieldDescriptors.size()).stream()
                .anyMatch(fieldDescriptor -> !equalsOnFields(first, fieldDescriptor));
        if (hasDifferentDiscriptors) {
            throw new MultipleNonEqualFieldDescriptors(first.getPath());
        } else {
            return first;
        }
    }

    private boolean equalsOnFields(FieldDescriptor f1, FieldDescriptor f2) {
        return f1.getPath().equals(f2.getPath())
                && f1.getType().equals(f2.getType())
                && f1.isOptional() == f2.isOptional()
                && f1.isIgnored() == f2.isIgnored();
    }


    private Schema unWrapRootArray(List<JsonFieldPath> jsonFieldPaths, Schema schema) {
        if (schema instanceof ObjectSchema) {
            ObjectSchema objectSchema = (ObjectSchema) schema;
            final Map<String, List<JsonFieldPath>> groups = groupFieldsByFirstRemainingPathSegment(emptyList(), jsonFieldPaths);
            if (groups.keySet().size() ==  1 && groups.keySet().contains("[]")) {
                return ArraySchema.builder().allItemSchema(objectSchema.getPropertySchemas().get("[]")).title(objectSchema.getTitle()).build();
            }

        }
        return schema;
    }

    private String toFormattedString(Schema schema) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
                .json()
                .indentOutput(true)
                .build();
        StringWriter writer = new StringWriter();
        schema.describeTo(new JSONPrinter(writer));
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(writer.toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Schema traverse(List<String> traversedSegments, List<JsonFieldPath> jsonFieldPaths, ObjectSchema.Builder builder) {

        Map<String, List<JsonFieldPath>> groupedFields = groupFieldsByFirstRemainingPathSegment(traversedSegments, jsonFieldPaths);
        groupedFields.forEach((propertyName, fieldList) -> {

            List<String> newTraversedSegments = new ArrayList<>(traversedSegments);
            newTraversedSegments.add(propertyName);
            fieldList.stream()
                    .filter(isDirectMatch(newTraversedSegments))
                    .findFirst()
                    .map(directMatch -> {
                        if (fieldList.size() == 1) {
                            handleEndOfPath(builder, propertyName, directMatch.getFieldDescriptor());
                        } else {
                            List<JsonFieldPath> newFields = new ArrayList<>(fieldList);
                            newFields.remove(directMatch);
                            processRemainingSegments(builder, propertyName, newTraversedSegments, newFields, (String) directMatch.getFieldDescriptor().getDescription());
                        }
                        return true;
                    }).orElseGet(() -> {
                        processRemainingSegments(builder, propertyName, newTraversedSegments, fieldList, null);
                        return true;
                    });
        });
        return builder.build();
    }

    private Predicate<JsonFieldPath> isDirectMatch(List<String> traversedSegments) {
        //we have a direct match when there are no remaining segments or when the only following element is an array
        return jsonFieldPath -> {
            List<String> remainingSegments = jsonFieldPath.remainingSegments(traversedSegments);
            return remainingSegments.isEmpty() || (remainingSegments.size() == 1 && isArraySegment(remainingSegments.get(0)));
        };
    }

    private Map<String, List<JsonFieldPath>> groupFieldsByFirstRemainingPathSegment(List<String> traversedSegments, List<JsonFieldPath> jsonFieldPaths) {
        return jsonFieldPaths.stream().collect(groupingBy(j -> j.remainingSegments(traversedSegments).get(0)));
    }

    private void processRemainingSegments(ObjectSchema.Builder builder, String propertyName, List<String> traversedSegments, List<JsonFieldPath> fields, String description) {
        List<String> remainingSegments = fields.get(0).remainingSegments(traversedSegments);
        if (remainingSegments.size() > 0 && isArraySegment(remainingSegments.get(0))) {
            traversedSegments.add(remainingSegments.get(0));
            builder.addPropertySchema(propertyName, ArraySchema.builder()
                    .allItemSchema(traverse(traversedSegments, fields, ObjectSchema.builder()))
                    .description(description)
                    .build());
        } else {
            builder.addPropertySchema(propertyName, traverse(traversedSegments, fields, (ObjectSchema.Builder) ObjectSchema.builder()
                    .description(description)));
        }
    }

    private void handleEndOfPath(ObjectSchema.Builder builder, String propertyName, FieldDescriptor fieldDescriptor) {
        if (fieldDescriptor.isIgnored()) {
            // We don't need to render anything
        } else if (fieldDescriptor.getType().equals(JsonFieldType.NULL) || fieldDescriptor.getType().equals(JsonFieldType.VARIES)) {
            builder.addPropertySchema(propertyName, NullSchema.builder()
                    .description((String) fieldDescriptor.getDescription())
                    .build());
        } else if (fieldDescriptor.getType().equals(JsonFieldType.OBJECT)) {
            builder.addPropertySchema(propertyName, ObjectSchema.builder()
                    .description((String) fieldDescriptor.getDescription())
                    .build());
        } else if (fieldDescriptor.getType().equals(JsonFieldType.ARRAY)) {
            builder.addPropertySchema(propertyName, ArraySchema.builder()
                    .description((String) fieldDescriptor.getDescription())
                    .build());
        } else if (fieldDescriptor.getType().equals(JsonFieldType.BOOLEAN)) {
            builder.addPropertySchema(propertyName, BooleanSchema.builder()
                    .description((String) fieldDescriptor.getDescription())
                    .build());
        } else if (fieldDescriptor.getType().equals(JsonFieldType.NUMBER)) {
            builder.addPropertySchema(propertyName, NumberSchema.builder()
                    .description((String) fieldDescriptor.getDescription())
                    .build());
        } else if (fieldDescriptor.getType().equals(JsonFieldType.STRING)) {
            builder.addPropertySchema(propertyName, StringSchema.builder()
                    .description((String) fieldDescriptor.getDescription())
                    .build());
        } else {
            throw new IllegalArgumentException("unknown field type " + fieldDescriptor.getType());
        }
    }

    static class MultipleNonEqualFieldDescriptors extends RuntimeException {
        public MultipleNonEqualFieldDescriptors(String path) {
            super(String.format("Found multiple FieldDescriptors for '%s' with different values", path));
        }
    }


}

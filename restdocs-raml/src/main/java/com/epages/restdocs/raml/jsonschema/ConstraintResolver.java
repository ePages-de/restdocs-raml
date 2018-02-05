package com.epages.restdocs.raml.jsonschema;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.restdocs.constraints.Constraint;
import org.springframework.restdocs.payload.FieldDescriptor;

public class ConstraintResolver {


    //since validation-api 2.0 NotEmpty moved to javax.validation - we support both
    private static final Set<String> NOT_EMPTY_CONSTRAINTS = new HashSet<>(Arrays.asList(
            "org.hibernate.validator.constraints.NotEmpty",
            "javax.validation.constraints.NotEmpty"
    ));

    private static final Set<String> NOT_BLANK_CONSTRAINTS = new HashSet<>(Arrays.asList(
            "javax.validation.constraints.NotBlank",
            "org.hibernate.validator.constraints.NotBlank"
    ));

    private static final Set<String> REQUIRED_CONSTRAINTS = new HashSet<>();

    private static final String LENGTH_CONSTRAINT = "org.hibernate.validator.constraints.Length";

    static {
        REQUIRED_CONSTRAINTS.add("javax.validation.constraints.NotNull");
        REQUIRED_CONSTRAINTS.addAll(NOT_EMPTY_CONSTRAINTS);
        REQUIRED_CONSTRAINTS.addAll(NOT_BLANK_CONSTRAINTS);
    }

    static Integer minLengthString(FieldDescriptor fieldDescriptor) {
        return findConstraints(fieldDescriptor).stream().
                filter(constraint -> NOT_EMPTY_CONSTRAINTS.contains(constraint.getName())
                        || NOT_BLANK_CONSTRAINTS.contains(constraint.getName())
                        || LENGTH_CONSTRAINT.equals(constraint.getName()))
                .findFirst()
                .map(constraint -> LENGTH_CONSTRAINT.equals(constraint.getName()) ? (int) constraint.getConfiguration().get("min") : 1)
                .orElse(null);
    }

    static Integer maxLengthString(FieldDescriptor fieldDescriptor) {
        return findConstraints(fieldDescriptor).stream().
                filter(constraint -> LENGTH_CONSTRAINT.equals(constraint.getName()))
                .findFirst()
                .map(constraint -> (int) constraint.getConfiguration().get("max"))
                .orElse(null);
    }

    static boolean isRequired(FieldDescriptor fieldDescriptor) {
        return findConstraints(fieldDescriptor).stream()
                .anyMatch(constraint -> REQUIRED_CONSTRAINTS.contains(constraint.getName()));
    }

    @SuppressWarnings("unchecked")
    private static List<Constraint> findConstraints(FieldDescriptor fieldDescriptor) {
        return fieldDescriptor.getAttributes().values().stream()
                .filter(value -> value instanceof List)
                .map(value -> (List) value)
                .filter(list -> !list.isEmpty() && list.get(0) instanceof Constraint)
                .flatMap(list -> (Stream<Constraint>) list.stream())
                .collect(toList());
    }
}

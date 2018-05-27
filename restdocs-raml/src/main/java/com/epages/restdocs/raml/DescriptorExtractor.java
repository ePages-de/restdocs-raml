package com.epages.restdocs.raml;

import static java.util.Collections.emptyList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.headers.AbstractHeadersSnippet;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.payload.AbstractFieldsSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.AbstractParametersSnippet;
import org.springframework.restdocs.request.ParameterDescriptor;

public class DescriptorExtractor {

    public static List<FieldDescriptor> extract(AbstractFieldsSnippet snippet) {
        try {
            Method getFieldDescriptors = AbstractFieldsSnippet.class.getDeclaredMethod("getFieldDescriptors");
            getFieldDescriptors.setAccessible(true);
            return (List<FieldDescriptor>) getFieldDescriptors.invoke(snippet);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return emptyList();
    }

    public static List<LinkDescriptor> extract(LinksSnippet snippet) {
        try {
            Method getDescriptorsByRel = LinksSnippet.class.getDeclaredMethod("getDescriptorsByRel");
            getDescriptorsByRel.setAccessible(true);
            return new ArrayList<>(((Map<String, LinkDescriptor>) getDescriptorsByRel.invoke(snippet)).values());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return emptyList();
    }

    public static List<HeaderDescriptor> extract(AbstractHeadersSnippet snippet) {
        try {
            Method getHeaderDescriptors = AbstractHeadersSnippet.class.getDeclaredMethod("getHeaderDescriptors");
            getHeaderDescriptors.setAccessible(true);
            return (List<HeaderDescriptor>) getHeaderDescriptors.invoke(snippet);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return emptyList();
    }

    public static List<ParameterDescriptor> extract(AbstractParametersSnippet snippet) {
        try {
            Method getParameterDescriptors = AbstractParametersSnippet.class.getDeclaredMethod("getParameterDescriptors");
            getParameterDescriptors.setAccessible(true);
            return new ArrayList<>(((Map<String, ParameterDescriptor>) getParameterDescriptors.invoke(snippet)).values());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return emptyList();
    }
}

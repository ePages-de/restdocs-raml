package com.epages.restdocs.raml.jsonschema;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.restdocs.payload.FieldDescriptor;

final class JsonFieldPath {

	private static final Pattern BRACKETS_AND_ARRAY_PATTERN = Pattern
            .compile("\\[\'(.+?)\'\\]|\\[([0-9]+|\\*){0,1}\\]");

	private static final Pattern ARRAY_INDEX_PATTERN = Pattern
			.compile("\\[([0-9]+|\\*){0,1}\\]");

	private FieldDescriptor fieldDescriptor;

	private final List<String> segments;


	private JsonFieldPath(List<String> segments, FieldDescriptor descriptor) {
		this.fieldDescriptor = descriptor;
		this.segments = segments;
	}

	FieldDescriptor getFieldDescriptor() {
		return fieldDescriptor;
	}

	List<String> remainingSegments(List<String> traversedSegments) {
		List<String> result = new ArrayList<>();
		for (int i = 0; i <= segments.size(); i++) {
			if (traversedSegments.size() <= i || !traversedSegments.get(i).equals(segments.get(i))) {
				return segments.subList(i, segments.size());
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return this.fieldDescriptor.getPath();
	}

	static JsonFieldPath compile(FieldDescriptor descriptor) {
		List<String> segments = extractSegments(descriptor.getPath());
		return new JsonFieldPath(segments, descriptor);
	}

	static boolean isArraySegment(String segment) {
		return ARRAY_INDEX_PATTERN.matcher(segment).find();
	}

	private static List<String> extractSegments(String path) {
		Matcher matcher = BRACKETS_AND_ARRAY_PATTERN.matcher(path);

		int previous = 0;

		List<String> segments = new ArrayList<>();
		while (matcher.find()) {
			if (previous != matcher.start()) {
				segments.addAll(extractDotSeparatedSegments(path.substring(previous, matcher.start())));
			}
			if (matcher.group(1) != null) {
				segments.add(matcher.group(1));
			}
			else {
				segments.add(matcher.group());
			}
			previous = matcher.end(0);
		}

		if (previous < path.length()) {
			segments.addAll(extractDotSeparatedSegments(path.substring(previous)));
		}

		return segments;
	}

	private static List<String> extractDotSeparatedSegments(String path) {
		List<String> segments = new ArrayList<>();
		for (String segment : path.split("\\.")) {
			if (segment.length() > 0) {
				segments.add(segment);
			}
		}
		return segments;
	}
}
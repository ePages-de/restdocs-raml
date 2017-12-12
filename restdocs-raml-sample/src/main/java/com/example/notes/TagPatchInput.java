package com.example.notes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TagPatchInput {
	
	@NullOrNotBlank
	private final String name;

	@JsonCreator
	public TagPatchInput(@NullOrNotBlank @JsonProperty("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
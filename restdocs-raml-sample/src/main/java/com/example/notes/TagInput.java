package com.example.notes;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TagInput {

	@NotBlank
	private final String name;

	@JsonCreator
	public TagInput(@NotBlank @JsonProperty("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}

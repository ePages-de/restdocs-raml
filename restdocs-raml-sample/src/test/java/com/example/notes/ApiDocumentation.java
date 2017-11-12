/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.notes;

import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import com.epages.restdocs.raml.RamlResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiDocumentation {
	
	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");
	
	@Autowired
	private NoteRepository noteRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Before
	public void setUp() {

		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
			.apply(documentationConfiguration(this.restDocumentation))
			.build();
	}

	@Test
	public void notesListExample() throws Exception {
		this.noteRepository.deleteAll();

		createNote("REST maturity model", "http://martinfowler.com/articles/richardsonMaturityModel.html");
		createNote("Hypertext Application Language (HAL)", "http://stateless.co/hal_specification.html");
		createNote("Application-Level Profile Semantics (ALPS)", "http://alps.io/spec/");
		
		this.mockMvc
			.perform(get("/notes"))
			.andExpect(status().isOk())
			.andDo(document("notes-list",
				ramlResource(RamlResourceSnippetParameters.builder()
						.responseFields(
								fieldWithPath("_embedded.notes").description("An array of <<resources-note, Note resources>>")
						).build()))
			);
	}

	@Test
	public void notesCreateExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

		String tagLocation = this.mockMvc
			.perform(post("/tags")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tag)))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getHeader("Location");

		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "REST maturity model");
		note.put("body", "http://martinfowler.com/articles/richardsonMaturityModel.html");
		note.put("tags", singletonList(tagLocation));

		ConstrainedFields fields = new ConstrainedFields(NoteInput.class);

		this.mockMvc
				.perform(post("/notes")
						.contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(note)))
				.andExpect(
						status().isCreated())
				.andDo(document("notes-create",
						ramlResource(RamlResourceSnippetParameters.builder()
								.requestFields(
										fields.withPath("title").description("The title of the note"),
										fields.withPath("body").description("The body of the note"),
										fields.withPath("tags").description("An array of tag resource URIs"))
								.build()))
				);
	}

	@Test
	public void noteGetExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

		String tagLocation = this.mockMvc
			.perform(post("/tags")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tag)))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getHeader("Location");

		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "REST maturity model");
		note.put("body", "http://martinfowler.com/articles/richardsonMaturityModel.html");
		note.put("tags", Arrays.asList(tagLocation));

		String noteLocation = this.mockMvc
			.perform(post("/notes")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(note)))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getHeader("Location");
		
		this.mockMvc
			.perform(get(noteLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("title", is(note.get("title"))))
			.andExpect(jsonPath("body", is(note.get("body"))))
			.andExpect(jsonPath("_links.self.href", is(noteLocation)))
			.andExpect(jsonPath("_links.note-tags", is(notNullValue())))
			.andDo(document("notes-get",
				//links(
				//	linkWithRel("self").description("This <<resources-note,note>>"),
				//	linkWithRel("note-tags").description("This note's <<resources-note-tags,tags>>")),
					ramlResource(RamlResourceSnippetParameters.builder()
							.responseFields(
									fieldWithPath("title").description("The title of the note"),
									fieldWithPath("body").description("The body of the note"),
									fieldWithPath("_links").description("<<resources-note-links,Links>> to other resources"))
							.build()))
			);

	}

	@Test
	public void tagsListExample() throws Exception {
		this.noteRepository.deleteAll();
		this.tagRepository.deleteAll();

		createTag("REST");
		createTag("Hypermedia");
		createTag("HTTP");
		
		this.mockMvc
			.perform(get("/tags"))
			.andExpect(status().isOk())
			.andDo(document("tags-list",
					ramlResource(RamlResourceSnippetParameters.builder()
							.responseFields(
									fieldWithPath("_embedded.tags").description("An array of <<resources-tag,Tag resources>>"))
							.build()))
			);
	}

	@Test
	public void tagsCreateExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

		ConstrainedFields fields = new ConstrainedFields(TagInput.class);
		
		this.mockMvc
			.perform(post("/tags")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tag)))
			.andExpect(status().isCreated())
			.andDo(document("tags-create",
					ramlResource(RamlResourceSnippetParameters.builder().
							requestFields(
									fields.withPath("name").description("The name of the tag"))
							.build())));
	}

	@Test
	public void noteUpdateExample() throws Exception {
		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "REST maturity model");
		note.put("body", "http://martinfowler.com/articles/richardsonMaturityModel.html");

		String noteLocation = this.mockMvc
			.perform(post("/notes")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(note)))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getHeader("Location");

		this.mockMvc
			.perform(get(noteLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("title", is(note.get("title"))))
			.andExpect(jsonPath("body", is(note.get("body"))))
			.andExpect(jsonPath("_links.self.href", is(noteLocation)))
			.andExpect(jsonPath("_links.note-tags", is(notNullValue())));

		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

		String tagLocation = this.mockMvc
			.perform(post("/tags")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tag)))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getHeader("Location");

		Map<String, Object> noteUpdate = new HashMap<String, Object>();
		noteUpdate.put("tags", Arrays.asList(tagLocation));

		ConstrainedFields fields = new ConstrainedFields(NotePatchInput.class);

		this.mockMvc
			.perform(patch(noteLocation)
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(noteUpdate)))
			.andExpect(status().isNoContent())
				.andDo(document("tags-patch",
						ramlResource(RamlResourceSnippetParameters.builder().
								requestFields(
										fields.withPath("title")
												.description("The title of the note")
												.type(JsonFieldType.STRING)
												.optional(),
										fields.withPath("body")
												.description("The body of the note")
												.type(JsonFieldType.STRING)
												.optional(),
										fields.withPath("tags")
												.description("An array of tag resource URIs"))
								.build()))
			);
	}

	@Test
	public void tagGetExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

		String tagLocation = this.mockMvc
			.perform(post("/tags")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tag)))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getHeader("Location");

		this.mockMvc
			.perform(get(tagLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("name", is(tag.get("name"))))
			.andDo(document("tags-get",
				//links(
				//	linkWithRel("self").description("This <<resources-tag,tag>>"),
				//	linkWithRel("tagged-notes").description("The <<resources-tagged-notes,notes>> that have this tag")),
					ramlResource(RamlResourceSnippetParameters.builder()
							.responseFields(
									fieldWithPath("name").description("The name of the tag"),
									fieldWithPath("_links").description("<<resources-tag-links,Links>> to other resources"))
							.build()))
			);
	}

	@Test
	public void tagUpdateExample() throws Exception {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "REST");

		String tagLocation = this.mockMvc
			.perform(post("/tags")
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tag)))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getHeader("Location");

		Map<String, Object> tagUpdate = new HashMap<String, Object>();
		tagUpdate.put("name", "RESTful");

		ConstrainedFields fields = new ConstrainedFields(TagPatchInput.class);
		
		this.mockMvc
			.perform(patch(tagLocation)
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tagUpdate)))
			.andExpect(status().isNoContent())
				.andDo(document("tags-patch",
						ramlResource(RamlResourceSnippetParameters.builder().
								requestFields(
										fields.withPath("name").description("The name of the tag"))
								.build()))
				);
	}

	private void createNote(String title, String body) {
		Note note = new Note();
		note.setTitle(title);
		note.setBody(body);

		this.noteRepository.save(note);
	}

	private void createTag(String name) {
		Tag tag = new Tag();
		tag.setName(name);
		this.tagRepository.save(tag);
	}

	private static class ConstrainedFields {

		private final ConstraintDescriptions constraintDescriptions;

		ConstrainedFields(Class<?> input) {
			this.constraintDescriptions = new ConstraintDescriptions(input);
		}

		private FieldDescriptor withPath(String path) {
			return fieldWithPath(path).attributes(key("constraints").value(StringUtils
					.collectionToDelimitedString(this.constraintDescriptions
							.descriptionsForProperty(path), ". ")));
		}
	}

}

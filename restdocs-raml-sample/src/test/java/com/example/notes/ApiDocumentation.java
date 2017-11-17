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

import static com.epages.restdocs.raml.RamlResourceDocumentation.fields;
import static com.epages.restdocs.raml.RamlResourceDocumentation.ramlResource;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.test.web.servlet.ResultActions;
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

	private ConstrainedFields noteFields = new ConstrainedFields(NoteInput.class);

	private MockMvc mockMvc;
	private HashMap<String, String> tag;
	private String tagLocation;
	private ResultActions resultActions;
	private Map<String, Object> note;
	private String noteLocation;

	@Before
	public void setUp() {

		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
			.apply(documentationConfiguration(this.restDocumentation))
			.build();

		this.noteRepository.deleteAll();
		this.tagRepository.deleteAll();
	}

	@Test
	public void notesListExample() throws Exception {
		createNote("REST maturity model", "http://martinfowler.com/articles/richardsonMaturityModel.html");
		createNote("Hypertext Application Language (HAL)", "http://stateless.co/hal_specification.html");
		createNote("Application-Level Profile Semantics (ALPS)", "http://alps.io/spec/");

		whenNotesRetrieved();

		resultActions.andDo(document("notes-list", ramlResource()));
	}

	@Test
	public void notesCreateExample() throws Exception {
		givenTag();

		givenNoteRequest(singletonList(tagLocation));

		whenNoteCreated();

		resultActions
				.andDo(document("notes-create",
						ramlResource(RamlResourceSnippetParameters.builder()
								.requestFields(
										noteFields.withPath("title").description("The title of the note"),
										noteFields.withPath("body").description("The body of the note"),
										noteFields.withPath("tags").description("An array of tag resource URIs"))
								.build()))
				);
	}

	@Test
	public void noteGetExample() throws Exception {
		givenTag();

		givenNote();

		whenNoteIsRetrieved();

		resultActions.andDo(document("notes-get",
				ramlResource(RamlResourceSnippetParameters.builder()
						.description("Get all the notes")
						.responseFields(
								fieldWithPath("title").description("The title of the note"),
								fieldWithPath("body").description("The body of the note"),
								fieldWithPath("_links").description("Links to other resources"))
						.links(
								linkWithRel("self").description("This self reference"),
								linkWithRel("note-tags").description("The link to the tags associated with this note"))
						.build()))
		);
	}

	@Test
	public void tagsListExample() throws Exception {
		givenTags();

		whenTagsRetrieved();

		resultActions.andDo(document("tags-list",
				ramlResource(RamlResourceSnippetParameters.builder()
						.responseFields(
								fields(fieldWithPath("_embedded.tags").description("An array of tags"))
										.andWithPrefix("_embedded.tags[].", tagFields()))
						.build()))
		);
	}

	@Test
	public void tagsCreateExample() throws Exception {
		givenTagRequest();

		ConstrainedFields fields = new ConstrainedFields(TagInput.class);

		whenTagsCreated();

		resultActions.andDo(document("tags-create",
				ramlResource(RamlResourceSnippetParameters.builder().
						requestFields(
								fields.withPath("name").description("The name of the tag"))
						.build())));
	}

	@Test
	public void noteUpdateExample() throws Exception {
		givenNote();
		this.mockMvc
			.perform(get(noteLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("title", is(note.get("title"))))
			.andExpect(jsonPath("body", is(note.get("body"))))
			.andExpect(jsonPath("_links.self.href", is(noteLocation)))
			.andExpect(jsonPath("_links.note-tags", is(notNullValue())));

		givenTag();

		Map<String, Object> noteUpdate = new HashMap<>();
		noteUpdate.put("tags", singletonList(tagLocation));

		whenNoteUpdated(noteUpdate);

		resultActions.andDo(document("tags-patch",
						ramlResource(RamlResourceSnippetParameters.builder().
								requestFields(
										fieldWithPath("title")
												.description("The title of the note")
												.type(JsonFieldType.STRING)
												.optional(),
										fieldWithPath("body")
												.description("The body of the note")
												.type(JsonFieldType.STRING)
												.optional(),
										fieldWithPath("tags")
												.description("An array of tag resource URIs"))
								.build()))
			);
	}

	@Test
	public void tagGetExample() throws Exception {
		givenTag();

		whenTagRetrieved();

		resultActions.andDo(document("tags-get",
				ramlResource(RamlResourceSnippetParameters.builder()
						.description("Get all the tags")
						.responseFields(
								tagFields())
						.links(
								linkWithRel("self").description("Link to this tag"),
								linkWithRel("tagged-notes").description("The resources that have this tag"))
						.build()))
		);
	}

	private FieldDescriptor tagFields() {
		return fieldWithPath("name").description("The name of the tag");
	}

	@Test
	public void tagUpdateExample() throws Exception {
		givenTag();

		Map<String, Object> tagUpdate = new HashMap<String, Object>();
		tagUpdate.put("name", "RESTful");

		whenTagUpdated(tagUpdate);

		resultActions.andDo(document("tags-patch",
						ramlResource(RamlResourceSnippetParameters.builder().
								requestFields(
										tagFields())
								.build()))
				);
	}

	private void whenTagRetrieved() throws Exception {
		resultActions = this.mockMvc
				.perform(get("/tags/{id}", tagLocation.substring(tagLocation.lastIndexOf("/"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("name", is(tag.get("name"))));
	}

	private void whenNoteUpdated(Map<String, Object> noteUpdate) throws Exception {
		resultActions = this.mockMvc
				.perform(patch("/notes/{id}", noteLocation.substring(noteLocation.lastIndexOf("/")))
						.contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(noteUpdate)))
				.andExpect(status().isNoContent());
	}

	private void whenTagUpdated(Map<String, Object> tagUpdate) throws Exception {
		resultActions = this.mockMvc
			.perform(patch("/tags/{id}", tagLocation.substring(tagLocation.lastIndexOf("/")))
				.contentType(MediaTypes.HAL_JSON)
				.content(this.objectMapper.writeValueAsString(tagUpdate)))
			.andExpect(status().isNoContent());
	}

	private void whenTagsRetrieved() throws Exception {
		resultActions = this.mockMvc
				.perform(get("/tags"))
				.andExpect(status().isOk());
	}

	private void givenTags() {
		createTag("REST");
		createTag("Hypermedia");
		createTag("HTTP");
	}

	private void whenNoteIsRetrieved() throws Exception {
		resultActions= this.mockMvc
				.perform(get("/notes/{id}", noteLocation.substring(noteLocation.lastIndexOf("/"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("title", is(note.get("title"))))
				.andExpect(jsonPath("body", is(note.get("body"))))
				.andExpect(jsonPath("_links.self.href", is(noteLocation)))
				.andExpect(jsonPath("_links.note-tags", is(notNullValue())));
	}

	private void givenNoteRequest(List<String> tags) {
		note = new HashMap<>();
		note.put("title", "REST maturity model");
		note.put("body", "http://martinfowler.com/articles/richardsonMaturityModel.html");
		if (!tags.isEmpty()) {
			note.put("tags", tags);
		}
	}

	private void whenNoteCreated() throws Exception {
		resultActions = this.mockMvc
				.perform(post("/notes")
						.contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(note)))
				.andExpect(status().isCreated());
		noteLocation = resultActions.andReturn().getResponse().getHeader("Location");
	}

	private void whenTagsCreated() throws Exception {
		resultActions = this.mockMvc
				.perform(post("/tags")
						.contentType(MediaTypes.HAL_JSON)
						.content(this.objectMapper.writeValueAsString(tag)))
				.andExpect(status().isCreated());

		tagLocation = resultActions.andReturn().getResponse().getHeader("Location");
	}

	private void givenNote() throws Exception {
		givenNoteRequest(tagLocation == null ? emptyList() : singletonList(tagLocation));
		whenNoteCreated();
	}

	private void  whenNotesRetrieved() throws Exception {
		resultActions = this.mockMvc
				.perform(get("/notes"))
				.andExpect(status().isOk());
	}

	private void givenTag() throws Exception {
		givenTagRequest();

		whenTagsCreated();
	}

	private void givenTagRequest() {
		tag = new HashMap<String, String>();
		tag.put("name", "REST");
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

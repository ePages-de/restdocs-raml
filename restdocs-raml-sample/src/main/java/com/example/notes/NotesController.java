package com.example.notes;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriTemplate;

import com.example.notes.NoteResourceAssembler.NoteResource;

@RestController
@RequestMapping("/notes")
public class NotesController {

	private static final UriTemplate TAG_URI_TEMPLATE = new UriTemplate("/tags/{id}");

	private final NoteRepository noteRepository;

	private final TagRepository tagRepository;

	private final NoteResourceAssembler noteResourceAssembler;

	private final TagResourceAssembler tagResourceAssembler;

	private final PagedResourcesAssembler<Note> pagedResourcesAssembler;

	@Autowired
	public NotesController(NoteRepository noteRepository, TagRepository tagRepository,
						   NoteResourceAssembler noteResourceAssembler,
						   TagResourceAssembler tagResourceAssembler,
						   PagedResourcesAssembler<Note> pagedResourcesAssembler) {
		this.noteRepository = noteRepository;
		this.tagRepository = tagRepository;
		this.noteResourceAssembler = noteResourceAssembler;
		this.tagResourceAssembler = tagResourceAssembler;
		this.pagedResourcesAssembler = pagedResourcesAssembler;
	}

	@RequestMapping(method = RequestMethod.GET)
	PagedResources<NoteResource> all(Pageable pageable) {
		return pagedResourcesAssembler.toResource(noteRepository.findAll(pageable),
				noteResourceAssembler,
				ControllerLinkBuilder.linkTo(methodOn(this.getClass()).all(null)).withSelfRel());
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(method = RequestMethod.POST)
	HttpHeaders create(@RequestBody NoteInput noteInput) {
		Note note = new Note();
		note.setTitle(noteInput.getTitle());
		note.setBody(noteInput.getBody());
		note.setTags(getTags(noteInput.getTagUris()));

		this.noteRepository.save(note);

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders
				.setLocation(linkTo(NotesController.class).slash(note.getId()).toUri());

		return httpHeaders;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	void delete(@PathVariable("id") long id) {
		this.noteRepository.delete(id);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	Resource<Note> note(@PathVariable("id") long id) {
		return this.noteResourceAssembler.toResource(findNoteById(id));
	}

	@RequestMapping(value = "/{id}/tags", method = RequestMethod.GET)
	ResourceSupport noteTags(@PathVariable("id") long id) {
		return new Resources<>(
				this.tagResourceAssembler.toResources(findNoteById(id).getTags()));
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void updateNote(@PathVariable("id") long id, @RequestBody NotePatchInput noteInput) {
		Note note = findNoteById(id);
		if (noteInput.getTagUris() != null) {
			note.setTags(getTags(noteInput.getTagUris()));
		}
		if (noteInput.getTitle() != null) {
			note.setTitle(noteInput.getTitle());
		}
		if (noteInput.getBody() != null) {
			note.setBody(noteInput.getBody());
		}
		this.noteRepository.save(note);
	}

	private Note findNoteById(long id) {
		Note note = this.noteRepository.findById(id);
		if (note == null) {
			throw new ResourceDoesNotExistException();
		}
		return note;
	}

	private List<Tag> getTags(List<URI> tagLocations) {
		List<Tag> tags = new ArrayList<>(tagLocations.size());
		for (URI tagLocation: tagLocations) {
			Tag tag = this.tagRepository.findById(extractTagId(tagLocation));
			if (tag == null) {
				throw new IllegalArgumentException("The tag '" + tagLocation
										+ "' does not exist");
			}
			tags.add(tag);
		}
		return tags;
	}

	private long extractTagId(URI tagLocation) {
		try {
			String idString = TAG_URI_TEMPLATE.match(tagLocation.toASCIIString()).get(
					"id");
			return Long.valueOf(idString);
		}
		catch (RuntimeException ex) {
			throw new IllegalArgumentException("The tag '" + tagLocation + "' is invalid");
		}
	}
}

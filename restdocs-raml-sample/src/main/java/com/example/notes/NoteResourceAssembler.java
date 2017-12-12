package com.example.notes;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import com.example.notes.NoteResourceAssembler.NoteResource;

@Component
public class NoteResourceAssembler extends ResourceAssemblerSupport<Note, NoteResource> {

	public NoteResourceAssembler() {
		super(NotesController.class, NoteResource.class);
	}

	@Override
	public NoteResource toResource(Note note) {
		NoteResource resource = createResourceWithId(note.getId(), note);
		resource.add(linkTo(NotesController.class).slash(note.getId()).slash("tags")
				.withRel("note-tags"));
		return resource;
	}

	@Override
	protected NoteResource instantiateResource(Note entity) {
		return new NoteResource(entity);
	}

	static class NoteResource extends Resource<Note> {

		public NoteResource(Note content) {
			super(content);
		}
	}

}

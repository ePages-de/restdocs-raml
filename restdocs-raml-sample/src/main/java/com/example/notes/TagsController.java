package com.example.notes;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.notes.TagResourceAssembler.TagResource;

@RestController
@RequestMapping("tags")
public class TagsController {

	private final TagRepository repository;

	private final NoteResourceAssembler noteResourceAssembler;

	private final TagResourceAssembler tagResourceAssembler;

	private PagedResourcesAssembler<Tag> pagedResourcesAssembler;

	@Autowired
	public TagsController(TagRepository repository,
						  NoteResourceAssembler noteResourceAssembler,
						  TagResourceAssembler tagResourceAssembler,
						  PagedResourcesAssembler<Tag> pagedResourcesAssembler) {
		this.repository = repository;
		this.noteResourceAssembler = noteResourceAssembler;
		this.tagResourceAssembler = tagResourceAssembler;
		this.pagedResourcesAssembler = pagedResourcesAssembler;
	}

	@RequestMapping(method = RequestMethod.GET)
	PagedResources<TagResource> all(Pageable pageable) {
		return pagedResourcesAssembler.toResource(repository.findAll(pageable),
				tagResourceAssembler,
				linkTo(methodOn(this.getClass()).all(null)).withSelfRel());
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(method = RequestMethod.POST)
	HttpHeaders create(@RequestBody TagInput tagInput) {
		Tag tag = new Tag();
		tag.setName(tagInput.getName());

		this.repository.save(tag);

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(linkTo(TagsController.class).slash(tag.getId()).toUri());

		return httpHeaders;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	void delete(@PathVariable("id") long id) {
		this.repository.delete(id);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	Resource<Tag> tag(@PathVariable("id") long id) {
		Tag tag = findTagById(id);
		return this.tagResourceAssembler.toResource(tag);
	}

	@RequestMapping(value = "/{id}/notes", method = RequestMethod.GET)
	ResourceSupport tagNotes(@PathVariable("id") long id) {
		Tag tag = findTagById(id);
		return new Resources<>(
				this.noteResourceAssembler.toResources(tag.getNotes()));
	}

	private Tag findTagById(long id) {
		Tag tag = this.repository.findById(id);
		if (tag == null) {
			throw new ResourceDoesNotExistException();
		}
		return tag;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void updateTag(@PathVariable("id") long id, @RequestBody TagPatchInput tagInput) {
		Tag tag = findTagById(id);
		if (tagInput.getName() != null) {
			tag.setName(tagInput.getName());
		}
		this.repository.save(tag);
	}
}

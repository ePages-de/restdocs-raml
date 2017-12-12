package com.example.notes;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import com.example.notes.TagResourceAssembler.TagResource;

@Component
public class TagResourceAssembler extends ResourceAssemblerSupport<Tag, TagResource> {

	public TagResourceAssembler() {
		super(TagsController.class, TagResource.class);
	}

	@Override
	public TagResource toResource(Tag tag) {
		TagResource resource = createResourceWithId(tag.getId(), tag);
		resource.add(linkTo(TagsController.class).slash(tag.getId()).slash("notes")
				.withRel("tagged-notes"));
		return resource;
	}

	@Override
	protected TagResource instantiateResource(Tag entity) {
		return new TagResource(entity);
	}

	static class TagResource extends Resource<Tag> {

		public TagResource(Tag content) {
			super(content);
		}
	}

}

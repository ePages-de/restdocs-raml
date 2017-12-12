package com.example.notes;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface NoteRepository extends PagingAndSortingRepository<Note, Long> {

	Note findById(long id);

	List<Note> findByTagsIn(Collection<Tag> tags);
}

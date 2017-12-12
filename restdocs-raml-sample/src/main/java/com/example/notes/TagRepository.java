package com.example.notes;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {

	Tag findById(long id);

}

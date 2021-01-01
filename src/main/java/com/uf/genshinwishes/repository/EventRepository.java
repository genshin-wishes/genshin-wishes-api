package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.Event;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EventRepository extends PagingAndSortingRepository<Event, Long> {

    List<Event> findAllByOrderByStartDateDesc();
}

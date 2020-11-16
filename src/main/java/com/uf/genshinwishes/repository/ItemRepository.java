package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.Item;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

}

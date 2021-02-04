package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

}

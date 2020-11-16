package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface WishRepository extends PagingAndSortingRepository<Wish, Long> {

    Optional<Wish> findFirstByUserOrderByTimeDescIdDesc(User user);

    List<Wish> findFirst100ByUserAndGachaTypeOrderByIdDesc(User user, Integer gachaType);

    void deleteByUser(User user);

    Page<Wish> findAllByUserAndGachaTypeOrderByIdDesc(Pageable pageable, User user, Integer gachaType);

    Integer countAllByUserAndGachaType(User user, Integer gachaType);

    Integer countByUserAndGachaType(User user, Integer gachaType);
}

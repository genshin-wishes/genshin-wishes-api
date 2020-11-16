package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    public User findByEmail(String email);
}
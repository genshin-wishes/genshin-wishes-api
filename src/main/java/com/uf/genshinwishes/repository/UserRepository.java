package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByProfileId(String profileId);
}

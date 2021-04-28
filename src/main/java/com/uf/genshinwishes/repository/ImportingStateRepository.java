package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.ImportingState;
import com.uf.genshinwishes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface ImportingStateRepository extends JpaRepository<ImportingState, Long> {

    ImportingState findFirstByUserOrderByCreationDateAsc(User user);

    void deleteAllByUser(User user);

    void deleteAllByLastModifiedDateLessThan(Instant time);
}

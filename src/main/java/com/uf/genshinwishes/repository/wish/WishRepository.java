package com.uf.genshinwishes.repository.wish;

import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WishRepository extends PagingAndSortingRepository<Wish, Long>, JpaSpecificationExecutor<Wish> {

    Optional<Wish> findFirstByUserOrderByTimeDescIdDesc(User user);

    List<Wish> findFirst100ByUserAndGachaTypeOrderByIdDesc(User user, Integer gachaType);

    void deleteByUser(User user);

    Long countByUserAndGachaType(User user, Integer gachaType);
}

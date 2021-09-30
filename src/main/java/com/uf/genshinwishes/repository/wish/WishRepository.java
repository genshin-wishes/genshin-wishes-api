package com.uf.genshinwishes.repository.wish;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WishRepository extends PagingAndSortingRepository<Wish, Long>, JpaSpecificationExecutor {

    Optional<Wish> findFirstByUserOrderByTimeDescIdDesc(User user);

    List<Wish> findFirst100ByUserAndGachaTypeOrderByIdDesc(User user, Integer gachaType);

    void deleteByUser(User user);

    Long countByUserAndGachaType(User user, Integer gachaType);

    @Query(value = """
        select max(w.index) from wishes w
        join items i on w.item_id = i.item_id
        where w.index <= :wishIndex
        and w.user_id = :userId
        and w.gacha_type = :gachaType
        and w.time >= :sixMonths
        and i.rank_type = :rankType
        """, nativeQuery = true)
    Long findLatestNonArchivedWish(Long userId, Integer rankType, Integer gachaType, LocalDateTime sixMonths, Long wishIndex);

    List<Wish> findByUserOrderByGachaTypeAscIndexAsc(User user);
}

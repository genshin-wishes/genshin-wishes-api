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
        select w.* from wishes w
        where w.index = (
            select max(w2.index) from wishes w2
                join items i on w2.item_id = i.item_id
                where w2.index <= :wishIndex and w2.user_id = :userId and w2.gacha_type = :gachaType
                    and i.rank_type = :rankType
        ) and w.user_id = :userId and w.gacha_type = :gachaType
        """, nativeQuery = true)
    Optional<Wish> findByUserAndRankTypeAndGachaTypeAndWishIndex(Long userId, Integer rankType, Integer gachaType, Long wishIndex);

    @Query(value = """
        select w.* from wishes w
        where w.index = (
            select min(w2.index) from wishes w2
                where w2.time >= :archiveDate and w2.user_id = :userId and w2.gacha_type = :gachaType
        ) and w.user_id = :userId and w.gacha_type = :gachaType
        """, nativeQuery = true)
    Optional<Wish> findFirstNonArchived(Long userId, Integer gachaType, LocalDateTime archiveDate);

    List<Wish> findByUserOrderByGachaTypeAscIndexAsc(User user);
}

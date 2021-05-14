package com.uf.genshinwishes.service;

import com.uf.genshinwishes.dto.*;
import com.uf.genshinwishes.dto.mapper.WishMapper;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import com.uf.genshinwishes.repository.wish.WishRepository;
import com.uf.genshinwishes.repository.wish.WishSpecification;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StatsService {

    private EntityManager em;
    private BannerService bannerService;
    private WishRepository wishRepository;
    private WishMapper wishMapper;

    public StatsDTO getStatsFor(User user, BannerType bannerType, WishFilterDTO filters) {
        List<BannerDTO> banners = bannerService.findAll();
        filters.setRanks(Arrays.asList(4, 5));
        WishSpecification specification = WishSpecification.builder().user(user).bannerType(bannerType).banners(banners).filters(filters).build();

        StatsDTO stats = new StatsDTO();

        stats.setBannerType(bannerType);

        WishSpecification allRanksSpecifications = specification.toBuilder().filters(specification.getFilters().toBuilder().ranks(null).build()).build();
        stats.setCount(wishRepository.count(allRanksSpecifications));

        WishSpecification fourStarsSpecifications = specification.toBuilder()
            .filters(specification.getFilters().toBuilder().ranks(Collections.singletonList(4)).build())
            .build();
        stats.setCount4Stars(wishRepository.count(fourStarsSpecifications));

        WishSpecification fiveStarsSpecifications = specification.toBuilder()
            .filters(specification.getFilters().toBuilder().ranks(Collections.singletonList(5)).build())
            .build();
        stats.setCount5Stars(wishRepository.count(fiveStarsSpecifications));


        List<Wish> wishes = wishRepository.findAll(specification.toBuilder().fetchBanner(true).build(), Sort.by(Sort.Order.asc("gachaType"), Sort.Order.asc("index")));
        List<WishDTO> wishDTOs = wishes.stream()
            .map(wishMapper::toDto).collect(Collectors.toList());

        stats.setWishes(wishDTOs);

        stats.setCountPerDay(getCountPerDay(allRanksSpecifications, bannerType == BannerType.ALL));

        return stats;
    }

    public List<CountPerRankAndDay> getCountPerDay(WishSpecification specification, boolean perBanner) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CountPerRankAndDay> query = criteriaBuilder.createQuery(CountPerRankAndDay.class);
        Root<Wish> root = query.from(Wish.class);

        Expression<LocalDate> dateTrunc = criteriaBuilder.function("DATE_TRUNC", Date.class, criteriaBuilder.literal("WEEK"), root.get("time")).as(LocalDate.class);

        query.where(specification.toPredicate(root, query, criteriaBuilder));

        query.groupBy(dateTrunc, root.get("item").get("rankType"));

        return em.createQuery(query.multiselect(dateTrunc, root.get("item").get("rankType"), criteriaBuilder.count(root))).getResultList();
    }
}

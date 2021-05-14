package com.uf.genshinwishes.service;

import com.google.common.collect.Maps;
import com.uf.genshinwishes.dto.*;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import com.uf.genshinwishes.repository.wish.WishRepository;
import com.uf.genshinwishes.repository.wish.WishSpecification;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

// FIXME refactor (not dry at all)

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PublicStatsService {

    private EntityManager em;
    private BannerService bannerService;
    private WishRepository wishRepository;

    @Cacheable(value = "publicStats")
    public PublicStatsDTO getStatsFor(BannerType bannerType, Long event) {
        if (event != null && bannerService.findAll().stream().noneMatch(b -> b.getId() == event && b.getGachaType() == bannerType))
            throw new ApiError(ErrorType.INVALID_FILTERS);

        return this.updateStatsFor(bannerType, event);
    }

    @CachePut("publicStats")
    public PublicStatsDTO updateStatsFor(BannerType bannerType, Long event) {
        WishFilterDTO filters = WishFilterDTO.builder().events(
            event != null ? Arrays.asList(event) : Collections.emptyList()
        ).build();

        List<BannerDTO> banners = bannerService.findAll();
        filters.setRanks(Arrays.asList(4, 5));
        WishSpecification fourFiveSpecifications = WishSpecification.builder().bannerType(bannerType).banners(banners).filters(filters).build();

        PublicStatsDTO stats = new PublicStatsDTO();

        WishSpecification allRanksSpecifications = fourFiveSpecifications.toBuilder().filters(fourFiveSpecifications.getFilters().toBuilder().ranks(null).build()).build();
        stats.setCount(wishRepository.count(allRanksSpecifications));

        WishSpecification fourStarsSpecifications = fourFiveSpecifications.toBuilder()
            .filters(fourFiveSpecifications.getFilters().toBuilder().ranks(Collections.singletonList(4)).build())
            .build();
        stats.setCount4Stars(wishRepository.count(fourStarsSpecifications));

        WishSpecification fiveStarsSpecifications = fourFiveSpecifications.toBuilder()
            .filters(fourFiveSpecifications.getFilters().toBuilder().ranks(Collections.singletonList(5)).build())
            .build();
        stats.setCount5Stars(wishRepository.count(fiveStarsSpecifications));

        Map<Integer, BannerDTO> latestBannerToEventMap = this.bannerService.getLatestBannerToEventMap(null);
        Map<BannerType, LatestEventsCountsDTO> latestEventsCounts = Maps.newHashMap();

        Arrays.asList(BannerType.CHARACTER_EVENT, BannerType.WEAPON_EVENT).stream().forEach(banner -> {
            LatestEventsCountsDTO latestEventsCountsDTO = new LatestEventsCountsDTO();

            WishFilterDTO filter = WishFilterDTO.builder()
                .ranks(Arrays.asList(4, 5))
                .events(Arrays.asList(latestBannerToEventMap.get(banner.getType()).getId()))
                .build();
            WishSpecification eventSpecification = WishSpecification.builder()
                .banners(banners)
                .bannerType(banner)
                .filters(filter)
                .build();

            latestEventsCountsDTO.setCount(wishRepository.count(eventSpecification));
            latestEventsCountsDTO.setItems(this.getCountPerItemId(eventSpecification));

            latestEventsCounts.put(banner, latestEventsCountsDTO);
        });

        stats.setLatestEventsCounts(latestEventsCounts);

        stats.setUsersPerRegion(getUserPerRegion(allRanksSpecifications));

        stats.setCountPerBanner(getCountPerBanner(allRanksSpecifications));

        if (bannerType == BannerType.CHARACTER_EVENT || bannerType == BannerType.WEAPON_EVENT) {
            stats.setExclusiveRate5Stars(getExclusiveCount(bannerType, fiveStarsSpecifications));
            stats.setExclusiveRate4Stars(getExclusiveCount(bannerType, fourStarsSpecifications));
        }

        if (bannerType != BannerType.ALL) {
            stats.setCountPerPity5Stars(getCountPerPity(fiveStarsSpecifications));
            stats.setCountPerPity4Stars(getCountPerPity(fourStarsSpecifications));
        }

        stats.setCountPerRegion(getCountPerRegion(allRanksSpecifications));

        stats.setCountPerDay(getCountPerDay(allRanksSpecifications));
        stats.setCountPerItemId(getCountPerItemId(fourFiveSpecifications));

        return stats;
    }

    public List<CountPerDay> getCountPerDay(WishSpecification specification) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CountPerDay> query = criteriaBuilder.createQuery(CountPerDay.class);
        Root<Wish> root = query.from(Wish.class);

        Expression<LocalDate> dateTrunc = criteriaBuilder.function("DATE_TRUNC", Date.class, criteriaBuilder.literal("WEEK"), root.get("time")).as(LocalDate.class);

        query.where(specification.toPredicate(root, query, criteriaBuilder));

        query.groupBy(dateTrunc);

        return em.createQuery(query.multiselect(dateTrunc, criteriaBuilder.count(root))).getResultList();
    }

    public Float getExclusiveCount(BannerType bannerType, WishSpecification specification) {
        List<BannerDTO> banners = bannerService.findAll();
        List<Long> events = specification.getFilters().getEvents();

        Map<BannerDTO, List<CountPerItemId>> counterPerItemPerEvent = banners.stream()
            .filter(b -> b.getStartEndByRegion() != null
                && b.getGachaType() == bannerType
                && (events == null || events.isEmpty() || events.contains(b.getId())))
            .collect(Collectors.toMap(Function.identity(), banner -> {
                WishSpecification bannerSpecification = specification.toBuilder()
                    .bannerType(banner.getGachaType())
                    .filters(specification.getFilters().toBuilder()
                        .events(Arrays.asList(banner.getId()))
                        .build())
                    .build();

                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
                CriteriaQuery<CountPerItemId> query = criteriaBuilder.createQuery(CountPerItemId.class);
                Root<Wish> root = query.from(Wish.class);

                query.where(bannerSpecification.toPredicate(root, query, criteriaBuilder));

                query.groupBy(root.get("item").get("itemId"));

                return em.createQuery(query.multiselect(root.get("item").get("itemId"), criteriaBuilder.count(root))).getResultList();
            }));

        AtomicLong event = new AtomicLong(0l);
        AtomicLong total = new AtomicLong(0l);

        counterPerItemPerEvent.forEach((b, list) -> {
            list.stream().forEach(i -> {
                if (b.getItems().stream().anyMatch(item -> item.getItemId().equals(i.getItemId())))
                    event.addAndGet(i.getCount());

                total.addAndGet(i.getCount());
            });
        });

        return total.get() == 0l ? 0f : 1.0f * (total.get() - event.get()) / event.get();
    }

    public List<CountPerItemId> getCountPerItemId(WishSpecification specification) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CountPerItemId> query = criteriaBuilder.createQuery(CountPerItemId.class);
        Root<Wish> root = query.from(Wish.class);

        query.where(specification.toPredicate(root, query, criteriaBuilder));

        query.groupBy(root.get("item").get("itemId"));

        query.orderBy(criteriaBuilder.desc(criteriaBuilder.count(root)));

        return em.createQuery(query.multiselect(root.get("item").get("itemId"), criteriaBuilder.count(root))).getResultList();
    }

    public List<CountPerPity> getCountPerPity(WishSpecification specification) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CountPerPity> query = criteriaBuilder.createQuery(CountPerPity.class);
        Root<Wish> root = query.from(Wish.class);

        query.where(criteriaBuilder.and(root.get("pity").isNotNull(),
            specification.toPredicate(root, query, criteriaBuilder)));

        query.groupBy(root.get("pity"));

        return em.createQuery(query.multiselect(root.get("pity"), criteriaBuilder.count(root))).getResultList();
    }

    public List<CountPerBanner> getCountPerBanner(WishSpecification specification) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CountPerBanner> query = criteriaBuilder.createQuery(CountPerBanner.class);
        Root<Wish> root = query.from(Wish.class);

        query.where(specification.toPredicate(root, query, criteriaBuilder));

        query.groupBy(root.get("gachaType"));

        return em.createQuery(query.multiselect(root.get("gachaType"), criteriaBuilder.count(root))).getResultList();
    }

    public List<CountPerRegion> getCountPerRegion(WishSpecification specification) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CountPerRegion> query = criteriaBuilder.createQuery(CountPerRegion.class);
        Root<Wish> root = query.from(Wish.class);

        query.where(specification.toPredicate(root, query, criteriaBuilder));

        query.groupBy(root.get("user").get("region"));

        return em.createQuery(query.multiselect(root.get("user").get("region"), criteriaBuilder.count(root))).getResultList();
    }

    public List<CountPerRegion> getUserPerRegion(WishSpecification specification) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<CountPerRegion> query = criteriaBuilder.createQuery(CountPerRegion.class);
        Root<User> root = query.from(User.class);

        Subquery<Wish> subQuery = query.subquery(Wish.class);
        Root<Wish> subRoot = subQuery.from(Wish.class);
        subQuery.where(criteriaBuilder.and(criteriaBuilder.equal(subRoot.get("user"), root)),
            specification.toPredicate(subRoot, query, criteriaBuilder));

        Expression<String> region = root.get("region");

        query.where(criteriaBuilder.and(root.get("region").isNotNull(),
            criteriaBuilder.exists(subQuery.select(subRoot))));

        query.groupBy(region);

        return em.createQuery(query.multiselect(region, criteriaBuilder.count(root))).getResultList();
    }
}

package com.uf.genshinwishes.repository.wish;

import com.google.common.collect.Lists;
import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class WishSpecification implements Specification<Wish> {

    private User user;
    private BannerType bannerType;
    private List<BannerDTO> banners;
    private WishFilterDTO filters;
    private Boolean fetchBanner;
    private Boolean ignoreFirstPity;

    public WishFilterDTO getFilters() {
        return filters;
    }

    @Override
    public Predicate toPredicate(Root<Wish> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
        if (bannerType == null || banners == null) throw new ApiError(ErrorType.INVALID_FILTERS);

        List<Predicate> predicates = Lists.newArrayList();

        // Mandatory
        if (!BannerType.ALL.equals(bannerType))
            predicates.add(builder.equal(root.<Integer>get("gachaType"), bannerType.getType()));

        // Optional
        if (user != null) {
            predicates.add(builder.equal(root.<User>get("user"), user));
        }

        if (filters != null) {
            predicates.add(getItemPredicate(root));
            predicates.add(getRankPredicate(root));
            predicates.add(getItemTypePredicate(root, builder));
            predicates.add(getDatePredicate(root, builder));
        }

        if (ignoreFirstPity != null && ignoreFirstPity) {
            predicates.add(getIgnoreFirstPityPredicate(root, builder));
        }

        return builder.and(predicates.stream().filter(predicate -> predicate != null).toArray(Predicate[]::new));
    }

    private Predicate getItemPredicate(Root<Wish> root) {
        if (filters.getItems() != null) {
            return root.<Item>get("item").<Integer>get("itemId").in(
                filters.getItems()
            );
        }
        return null;
    }

    private Predicate getRankPredicate(Root<Wish> root) {
        if (filters.getRanks() != null) {
            return root.<Item>get("item").<Integer>get("rankType").in(
                filters.getRanks()
            );
        }
        return null;
    }

    private Predicate getItemTypePredicate(Root<Wish> root, CriteriaBuilder builder) {
        if (filters.getItemType() != null) {
            return builder.equal(
                root.<Item>get("item").<String>get("itemType"),
                filters.getItemType().name()
            );
        }
        return null;
    }

    private Predicate getDatePredicate(Root<Wish> root, CriteriaBuilder builder) {
        if (filters.getEvents() != null && !filters.getEvents().isEmpty()) {
            List<Predicate> orTime = filters.getEvents().stream().map(event -> {
                BannerDTO banner = getBanner(event);

                List<Predicate> timePredicates = Arrays.stream(Region.values()).map(region -> {
                    LocalDateTime start = banner.getStartEndByRegion().get(region)[0];
                    LocalDateTime end = banner.getStartEndByRegion().get(region)[1];

                    return builder.and(builder.equal(root.get("user").get("region"), region.getPrefix()), builder.between(
                        builder.function("DATE_TRUNC", Date.class, builder.literal("MINUTE"), root.get("time")).as(LocalDateTime.class),
                         builder.literal(start),
                         builder.literal(end)));
                }).collect(Collectors.toList());

                return builder.or(timePredicates.toArray(new Predicate[]{}));
            }).collect(Collectors.toList());

            return builder.or(orTime.toArray(new Predicate[]{}));
        }

        return null;
    }

    private Predicate getIgnoreFirstPityPredicate(Root<Wish> root, CriteriaBuilder builder) {
        return builder.or(
            root.get("pity").isNull(),
            builder.not(builder.equal(root.get("index"), root.get("pity"))),
            builder.equal(root.get("gachaType"), BannerType.NOVICE.getType())
        );
    }

    private BannerDTO getBanner(Long event) {
        return banners.stream().filter(b -> b.getId().equals(event)).findFirst().orElseThrow();
    }
}

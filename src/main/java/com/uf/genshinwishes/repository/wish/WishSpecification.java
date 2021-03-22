package com.uf.genshinwishes.repository.wish;

import com.google.common.collect.Lists;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class WishSpecification implements Specification<Wish> {

    private User user;
    private BannerType bannerType;
    private WishFilterDTO filters;

    public WishSpecification(User user, BannerType bannerType, WishFilterDTO filters) {
        this.user = user;
        this.bannerType = bannerType;
        this.filters = filters;
    }

    @Override
    public Predicate toPredicate(Root<Wish> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder) {
        List<Predicate> predicates = Lists.newArrayList();

        // Mandatory
        predicates.add(builder.equal(root.<User>get("user"), user));
        if (!BannerType.ALL.equals(bannerType))
            predicates.add(builder.equal(root.<Integer>get("gachaType"), bannerType.getType()));

        // Optional
        if (filters != null) {
            predicates.add(getFreeTextPredicate(root, builder));
            predicates.add(getRankPredicate(root, builder));
            predicates.add(getItemTypePredicate(root, builder));
            predicates.add(getDatePredicate(root, builder));
        }

        return builder.and(predicates.stream().filter(predicate -> predicate != null).toArray(Predicate[]::new));
    }

    private Predicate getFreeTextPredicate(Root<Wish> root, CriteriaBuilder builder) {
        if (filters.getFreeText() != null) {
            return builder.like(
                builder.lower(root.<Item>get("item").<String>get(Boolean.TRUE.equals(filters.getFr()) ? "nameFr" : "name")),
                "%" +
                    filters.getFreeText().toLowerCase()
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_")
                        .replace("*", "%")
                    + "%"
            );
        }
        return null;
    }

    private Predicate getRankPredicate(Root<Wish> root, CriteriaBuilder builder) {
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
        if(filters.getEvents() != null && !filters.getEvents().isEmpty()) {
            return root.get("event").get("id").in(filters.getEvents());
        }

        return null;
    }
}

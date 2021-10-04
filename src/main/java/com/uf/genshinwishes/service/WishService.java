package com.uf.genshinwishes.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.dto.mapper.BannerMapper;
import com.uf.genshinwishes.dto.mapper.WishMapper;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.*;
import com.uf.genshinwishes.repository.ItemRepository;
import com.uf.genshinwishes.repository.wish.WishRepository;
import com.uf.genshinwishes.repository.wish.WishSpecification;
import com.uf.genshinwishes.service.mihoyo.MihoyoImRestClient;
import com.uf.genshinwishes.service.mihoyo.MihoyoRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class WishService {
    Logger logger = LoggerFactory.getLogger(WishService.class);

    @Autowired
    private WishRepository wishRepository;
    @Autowired
    private BannerService bannerService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private MihoyoRestClient mihoyoRestClient;
    @Autowired
    private MihoyoImRestClient mihoyoImRestClient;
    @Autowired
    private WishMapper wishMapper;
    @Autowired
    private ImportingStateService importingStateService;

    private List<Item> items;

    @Transactional
    public void importWishes(User user, String authkey, String gameBiz) {
        if (Strings.isNullOrEmpty(user.getMihoyoUid())) {
            throw new ApiError(ErrorType.NO_MIHOYO_LINKED);
        }

        Map<Integer, ImportingBannerState> stateByBanner = importingStateService.initializeImport(user);

        if (stateByBanner == null) {
            throw new ApiError(ErrorType.ALREADY_IMPORTING);
        }

        try {
            MihoyoUserDTO mihoyoUser = mihoyoImRestClient.getUserInfo(Optional.of(user), authkey, gameBiz);

            if (!user.getMihoyoUid().equals(mihoyoUser.getUser_id()))
                throw new ApiError(ErrorType.MIHOYO_UID_DIFFERENT);

            CompletableFuture.runAsync(() -> runImportFor(stateByBanner, user, authkey, gameBiz));
        } catch (Exception e) {
            importingStateService.forceRemove(user);

            throw e;
        }
    }


    public Map<BannerType, Collection<WishDTO>> getBanners(User user) {
        Multimap<BannerType, WishDTO> wishesByBanner = MultimapBuilder.hashKeys(BannerType.values().length).arrayListValues().build();

        Arrays.stream(BannerType.values())
            .forEach(type -> {
                List<Wish> wishes = wishRepository.findFirst100ByUserAndGachaTypeOrderByIdDesc(user, type.getType());

                wishesByBanner.putAll(type, wishes.stream().map(wishMapper::toDto).collect(Collectors.toList()));
            });

        return wishesByBanner.asMap();
    }

    @Transactional
    public void deleteAllUserWishes(User user) {
        wishRepository.deleteByUser(user);
    }

    public List<WishDTO> findByUserAndBannerType(User user, BannerType bannerType, Integer page, WishFilterDTO filters) {
        List<BannerDTO> banners = bannerService.findAllForUser(user);
        List<Wish> wishes = this.wishRepository.findAll(
            WishSpecification.builder().user(user).bannerType(bannerType).banners(banners).filters(filters).build(),
            PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "time", "id"))
        ).getContent();

        return wishes.stream().map(wishMapper::toDto).collect(Collectors.toList());
    }

    public Long countAllByUserAndGachaType(User user, BannerType bannerType, WishFilterDTO filters) {
        List<BannerDTO> banners = bannerService.findAllForUser(user);
        return this.wishRepository.count(WishSpecification.builder().user(user).bannerType(bannerType).banners(banners).filters(filters).build());
    }

    public Map<BannerType, Long> countAllByUser(User user) {
        return Arrays.stream(BannerType.values())
            .collect(Collectors.toMap(
                (banner) -> banner,
                (banner) -> this.wishRepository.countByUserAndGachaType(user, banner.getType())
            ));
    }

    public List<Wish> findByUser(User user) {
        return this.wishRepository.findByUserOrderByGachaTypeAscIndexAsc(user);
    }

    @Cacheable("wishesCount")
    public Long getWishesCount() {
        return null;
    }

    @CachePut("wishesCount")
    public Long updateWishesCount() {
        return wishRepository.count();
    }

    private void runImportFor(Map<Integer, ImportingBannerState> stateByGachaType, User user, String authkey, String gameBiz) {
        // Refresh item list
        items = itemRepository.findAll();

        Optional<Wish> ifLastWish = wishRepository.findFirstByUserOrderByTimeDescIdDesc(user);
        Optional<LocalDateTime> ifLastWishDate = ifLastWish.map(Wish::getTime);
        Map<BannerType, Long> oldCounts = countAllByUser(user);
        Instant now = Instant.now();

        List<Wish> cumulatedWishes = Lists.newArrayList();

        List<CompletableFuture<Void>> futures = BannerType.getBannersExceptAll().map(bannerType -> CompletableFuture.runAsync(() -> {
            ImportingBannerState bannerState = stateByGachaType.get(bannerType.getType());

            try {
                List<Wish> wishes = paginateWishesOlderThanDate(bannerState, authkey, gameBiz, bannerType, ifLastWishDate);

                importingStateService.finish(bannerState);

                AtomicReference<Long> last5StarIndex = new AtomicReference(0L);
                AtomicReference<Long> last4StarIndex = new AtomicReference(0L);

                calculateLatestIndexForFiveAndFourStars(user, oldCounts.getOrDefault(bannerType, 0L), bannerType, last5StarIndex, last4StarIndex);

                // Most recent = highest ID
                Collections.reverse(wishes);

                wishes = wishes.stream().map((wish) -> {
                    long index = oldCounts.getOrDefault(bannerType, 0L) + 1;

                    wish.setUser(user);
                    switch (wish.getItem().getRankType()) {
                        case 5:
                            wish.setPity(index - last5StarIndex.get());
                            last5StarIndex.set(index);
                            break;
                        case 4:
                            wish.setPity(index - last4StarIndex.get());
                            last4StarIndex.set(index);
                            break;
                    }
                    wish.setImportDate(now);
                    wish.setIndex(index);

                    oldCounts.put(bannerType, index);

                    return wish;
                }).collect(Collectors.toList());

                cumulatedWishes.addAll(wishes);
            } catch (ApiError e) {
                importingStateService.markError(bannerState, e);
                throw new CompletionException(e);
            } catch (Exception e) {
                importingStateService.markError(bannerState, new ApiError(ErrorType.IMPORT_ERROR));
                throw new CompletionException(e);
            }
        })).collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}))
            .thenApply(ignored -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()))
            .thenRun(() -> {
                wishRepository.saveAll(cumulatedWishes);

                BannerType.getBannersExceptAll().forEach(b ->
                    importingStateService.markSaved(stateByGachaType.get(b.getType())));
            })
            .exceptionally((err) -> {
                logger.error("Could not import for #{}", user.getId(), err.getCause());

                BannerType.getBannersExceptAll().forEach(b ->
                    importingStateService.markError(stateByGachaType.get(b.getType()), new ApiError(ErrorType.ERROR)));

                return null;
            });
    }

    private void calculateLatestIndexForFiveAndFourStars(User user, Long lastIndex, BannerType bannerType, AtomicReference<Long> last5StarIndex, AtomicReference<Long> last4StarIndex) {
        Optional<Wish> last5Star = wishRepository.findByUserAndRankTypeAndGachaTypeAndWishIndex(user.getId(), 5, bannerType.getType(), lastIndex);
        Optional<Wish> last4Star = wishRepository.findByUserAndRankTypeAndGachaTypeAndWishIndex(user.getId(), 4, bannerType.getType(), lastIndex);
        Optional<Wish> firstNonArchived = wishRepository.findFirstNonArchived(user.getId(), bannerType.getType(), BannerMapper.computeImportArchiveDate(), BannerMapper.computeArchiveDate(Region.getFromUser(user)));
        Long firstNonArchivedIndex = firstNonArchived.map(Wish::getIndex).orElse(lastIndex);

        if (last5Star.isPresent()) {
            if(last5Star.get().isBeforeArchive())
                last5StarIndex.set(firstNonArchivedIndex);
            else
                last5StarIndex.set(last5Star.get().getIndex());
        }

        if (last4Star.isPresent()) {
            if(last4Star.get().isBeforeArchive())
                last4StarIndex.set(firstNonArchivedIndex);
            else
                last4StarIndex.set(last4Star.get().getIndex());
        }
    }

    private List<MihoyoWishLogDTO> getWishesForPage(String authkey, String gameBiz, BannerType bannerType, String lastWishId, Integer page) {
        return mihoyoRestClient.getWishes(authkey, gameBiz, bannerType, lastWishId, page);
    }

    private List<Wish> paginateWishesOlderThanDate(ImportingBannerState bannerState, String authkey, String gameBiz, BannerType bannerType, Optional<LocalDateTime> ifLastWishDate) {
        List<Wish> wishes = Lists.newLinkedList();
        List<MihoyoWishLogDTO> pageWishes;
        String lastWishId = null;
        int currentPage = 1;

        while (!(pageWishes = getWishesForPage(authkey, gameBiz, bannerType, lastWishId, currentPage++)).isEmpty()) {
            List<Wish> internalWishes = pageWishes.stream()
                .map(wish -> wishMapper.fromMihoyo(wish, items))
                .collect(Collectors.toList());

            // We got a wish that's older than the last import
            if (ifLastWishDate.isPresent()) {
                if (internalWishes.get(internalWishes.size() - 1).getTime().compareTo(ifLastWishDate.get()) <= 0) {

                    List<Wish> filteredWishes = pageWishes.stream()
                        .map(wish -> wishMapper.fromMihoyo(wish, items))
                        .filter(wish -> wish.getTime().isAfter(ifLastWishDate.get()))
                        .collect(Collectors.toList());
                    wishes.addAll(filteredWishes);

                    importingStateService.increment(bannerState, filteredWishes.size());

                    break;
                }
            }

            lastWishId = pageWishes.get(pageWishes.size() - 1).getId();
            wishes.addAll(internalWishes);

            importingStateService.increment(bannerState, internalWishes.size());
        }

        if (!wishes.isEmpty()) {
            Wish firstWish = wishMapper.fromMihoyo(getWishesForPage(authkey, gameBiz, bannerType, null, 1).get(0), items);

            if (!wishes.get(0).getTime().equals(firstWish.getTime())) {
                throw new ApiError(ErrorType.NEW_WISHES_DURING_IMPORT);
            }
        }

        return wishes;
    }
}

package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.Banner;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BannerRepository extends PagingAndSortingRepository<Banner, Long> {

    List<Banner> findAllByGachaTypeOrderByStartDesc(Integer gachaType);

    Banner findFirstByGachaTypeOrderByEndDesc(Integer gachaType);
}

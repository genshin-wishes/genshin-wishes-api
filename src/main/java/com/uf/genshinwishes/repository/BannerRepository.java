package com.uf.genshinwishes.repository;

import com.uf.genshinwishes.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Override
    List<Banner> findAll();

    List<Banner> findAllByGachaTypeOrderByStartDesc(Integer gachaType);

    List<Banner> findAllByOrderByStartDesc();

    Banner findFirstByGachaTypeOrderByEndDesc(Integer gachaType);
}

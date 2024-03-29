package com.uf.genshinwishes.model;

import com.uf.genshinwishes.dto.mapper.BannerMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "WISHES")
public class Wish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long index;

    @Column
    private Long pity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer gachaType;

    @Column
    private Boolean second;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private Item item;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column
    private Instant importDate;

    public boolean isBeforeArchive() {
        return this.getImportDate() != null
            ? this.getImportDate().isBefore(BannerMapper.computeImportArchiveDate())
            : this.getTime().isBefore(BannerMapper.computeArchiveDate(Region.getFromUser(user)));
    }
}

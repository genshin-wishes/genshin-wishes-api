package com.uf.genshinwishes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity(name = "importing_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportingState implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant creationDate;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "importing_state_id", referencedColumnName = "id", nullable = false)
    List<ImportingBannerState> bannerStates;

    private Boolean deleted;
}

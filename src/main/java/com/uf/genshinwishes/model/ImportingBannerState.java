package com.uf.genshinwishes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "importing_banner_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportingBannerState implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer gachaType;

    @Column(nullable = false)
    private Integer count;

    @Column(nullable = false)
    private Boolean finished;

    @Column(nullable = false)
    private Boolean saved;

    @Column
    private String error;

    @ManyToOne
    @JoinColumn(name = "importing_state_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    ImportingState importingState;
}

package com.uf.genshinwishes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "items")
@Where(clause="published_at is not null")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item implements Serializable {

    @Id
    private Long id;

    @NaturalId
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String itemType;

    @Column(nullable = false)
    private Integer rankType;

    @ManyToOne
    @JoinTable(
        name = "upload_file_morph",
        joinColumns = @JoinColumn(name = "related_id"),
        inverseJoinColumns = @JoinColumn(name = "upload_file_id"))
    private Image image;
}

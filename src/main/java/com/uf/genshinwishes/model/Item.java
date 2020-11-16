package com.uf.genshinwishes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "ITEMS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    private Long itemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nameFr;

    @Column(nullable = false)
    private String itemType;

    @Column(nullable = false)
    private Integer rankType;
}

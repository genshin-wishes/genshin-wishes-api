package com.uf.genshinwishes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.FetchMode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.context.annotation.Lazy;

import javax.persistence.*;
import java.util.Date;
import java.util.Optional;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String uid;

    @Column(nullable = false)
    private Integer gachaType;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Item item;

    @Column
    private String itemName;

    @Column(nullable = false)
    private Date time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula(value = "(SELECT event.id FROM events event WHERE gacha_type = event.gacha_type AND time BETWEEN event.start_date AND event.end_date)")
    @NotFound(action = NotFoundAction.IGNORE)
    private Event event;
}

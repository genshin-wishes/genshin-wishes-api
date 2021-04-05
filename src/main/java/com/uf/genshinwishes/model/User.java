package com.uf.genshinwishes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -51419785561130265L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column
    private String key;

    @Column()
    private String lang;

    @Column()
    private Boolean wholeClock;

    @Column(nullable = false)
    private Date creationDate;

    @Column()
    private Date lastLoggingDate;

    @Column()
    private String mihoyoUsername;

    @Column()
    private String mihoyoUid;

    @Column()
    private String profileId;

    @Column()
    private Boolean sharing;
}

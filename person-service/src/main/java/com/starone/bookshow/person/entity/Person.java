package com.starone.bookshow.person.entity;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.starone.common.enums.Profession;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String email;

    @Column(name = "permanent_address")
    private String pAddress;

    @Column(name = "current_address")
    private String cAddress;

    @Column(name = "profile_img_url")
    private String profileImg;

    @Column(name = "is_active")
    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "person_professions", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "profession")
    @Enumerated(EnumType.STRING)
    private Set<Profession> professions;

}

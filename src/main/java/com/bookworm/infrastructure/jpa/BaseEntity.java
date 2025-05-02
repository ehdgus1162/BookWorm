package com.bookworm.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;


@Getter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
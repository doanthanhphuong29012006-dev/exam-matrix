package com.exammatrix.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", length = 10, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
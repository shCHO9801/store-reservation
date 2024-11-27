package com.zerobase.storereservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;             // 매장 이름

    @Column(nullable = false)
    private String location;         // 매장 위치

    @Column(columnDefinition = "TEXT")
    private String description;     //매장 설명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;             // 매장 소유자 (User와 연관 관계)
}

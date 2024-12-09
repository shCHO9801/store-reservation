package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * StoreRepository
 * - 매장 데이터를 처리하기 위한 JPA Repository
 * - 매장 관련 기본 및 확장 가능한 쿼리를 정의
 */
public interface StoreRepository extends JpaRepository<Store, Long> {
}

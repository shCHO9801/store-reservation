package com.zerobase.storereservation.repository;

import com.zerobase.storereservation.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}

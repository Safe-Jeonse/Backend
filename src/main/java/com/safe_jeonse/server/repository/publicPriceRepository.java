package com.safe_jeonse.server.repository;

import com.safe_jeonse.server.domain.publicPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface publicPriceRepository extends JpaRepository<publicPrice, String> {
}

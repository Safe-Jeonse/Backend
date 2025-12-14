package com.safe_jeonse.server.repository;

import com.safe_jeonse.server.domain.PublicPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicPriceRepository extends JpaRepository<PublicPrice, String> {
}

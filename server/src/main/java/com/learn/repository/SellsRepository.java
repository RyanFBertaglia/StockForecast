package com.learn.repository;

import com.learn.model.Sells;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SellsRepository extends JpaRepository<Sells, Long> {
    Integer countByProductId(Long productId);

    @Query(value = """
    SELECT COALESCE(SUM(quantity), 0)
    FROM Sells
    WHERE id_product = :productId
      AND date BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Long countSellsInRange(
            @Param("productId") Long productId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}

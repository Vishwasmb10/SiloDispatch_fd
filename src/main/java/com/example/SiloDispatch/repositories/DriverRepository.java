package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Driver;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DriverRepository extends CrudRepository<Driver, Long> {
    List<Driver> findAll();

    @Modifying
    @Query("UPDATE drivers SET cash_in_hand = cash_in_hand + :amount WHERE id = :driverId")
    void incrementCashInHand(@Param("driverId") Long driverId, @Param("amount") BigDecimal amount);


}


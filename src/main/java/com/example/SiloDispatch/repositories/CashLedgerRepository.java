package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.CashLedger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashLedgerRepository extends CrudRepository<CashLedger, Long> {
    List<CashLedger> findByDriverId(Long driverId);
}

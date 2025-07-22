package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Batch;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRepository extends CrudRepository<Batch, Long> {
    List<Batch> findByStatus(Batch.BatchStatus status);
    // Check if a driver already has an active batch (ASSIGNED or IN_PROGRESS)
    @Query("SELECT COUNT(*) FROM batches WHERE driver_id = :driverId AND status IN ('ASSIGNED', 'IN_PROGRESS')")
    int countActiveBatchesForDriver(@Param("driverId") Long driverId);


    @Query("SELECT * FROM Batches b WHERE b.driver_id = :driverId AND b.status = :status")
    Batch findByDriverIdAndStatus(@Param("driverId") Long driverId, @Param("status") Batch.BatchStatus status);



}

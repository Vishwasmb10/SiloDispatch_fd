package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Batch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRepository extends CrudRepository<Batch, Long> {
    List<Batch> findByStatus(Batch.BatchStatus status);
}

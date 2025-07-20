package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Driver;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends CrudRepository<Driver, Long> {
    List<Driver> findAll();

}


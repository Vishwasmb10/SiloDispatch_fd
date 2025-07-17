package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.Customer;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    @Query("SELECT * FROM customers WHERE phone = :phone")
    Customer findByPhone(String phone);

    @Modifying
    @Query("INSERT INTO customers(id, name, phone, address, pincode, lat, lon) VALUES (:#{#c.id}, :#{#c.name}, :#{#c.phone}, :#{#c.address}, :#{#c.pincode}, :#{#c.lat}, :#{#c.lon})")
    void insert(@Param("c") Customer customer);


}

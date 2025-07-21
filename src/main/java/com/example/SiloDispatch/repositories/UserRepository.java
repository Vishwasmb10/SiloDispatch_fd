package com.example.SiloDispatch.repositories;

import com.example.SiloDispatch.models.AppUser;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<AppUser, Long> {
    @Query("SELECT * FROM app_user WHERE username = :username")
    Optional<AppUser> findByUsername(String username);
}

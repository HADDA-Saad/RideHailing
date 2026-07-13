package com.ridehailing.rider.repository;

import com.ridehailing.rider.model.Rider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderRepository extends JpaRepository<Rider,Long> {
    Optional<Rider> findById(Long id);
    List<Rider> findByName(String name);
    Optional<Rider> findByEmail(String email);

}

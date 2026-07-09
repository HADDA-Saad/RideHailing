    package com.ridehailing.ride.repository;

    import com.ridehailing.ride.model.Ride;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;
    import java.util.Optional;

    public interface RideRepository extends JpaRepository<Ride,Long> {
        List<Ride> findByRiderId(Long id);
        List<Ride> findByDriverId(Long id);
        List<Ride> findByStatus(String status);

    }

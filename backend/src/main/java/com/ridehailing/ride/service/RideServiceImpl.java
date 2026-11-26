package com.ridehailing.ride.service;

import com.ridehailing.auth.model.Role;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.driver.repository.DriverRepository;
import com.ridehailing.ride.model.Ride;
import com.ridehailing.ride.model.RideStatus;
import com.ridehailing.ride.repository.RideRepository;
import com.ridehailing.rider.model.Rider;
import com.ridehailing.rider.repository.RiderRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class RideServiceImpl implements RideService{
    private final RideRepository rideRepo;
    private final DriverRepository driverRepo;
    private final RiderRepository riderRepo;

    public RideServiceImpl(RideRepository rideRepo, DriverRepository driverRepo,
                           RiderRepository riderRepo) {
        this.rideRepo = rideRepo;
        this.driverRepo = driverRepo;
        this.riderRepo = riderRepo;
    }

    @Override
    public Ride requestRide(Long driverId, Long authenticatedUserId) {
        Rider rider = riderRepo.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Rider profile required"));

        Ride ride = new Ride();
        ride.setRiderId(rider.getId());
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACTIVE);
        return rideRepo.save(ride);
    }


    private Long profileId(Long userId, Role role) {
        if (role == Role.RIDER) {
            return riderRepo.findByUserId(userId).map(Rider::getId)
                    .orElseThrow(() -> new AccessDeniedException("Rider profile required"));
        }
        return driverRepo.findByUserId(userId).map(Driver::getId)
                .orElseThrow(() -> new AccessDeniedException("Driver profile required"));
    }

    private void requireMatch(Long profileId, Long requestedId) {
        if (!profileId.equals(requestedId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    // A ride belongs to both participants' histories, even after it ends.
    public Ride findById(Long id, Long userId, Role role) {
        Ride ride = rideRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        requireMatch(profileId(userId, role), role == Role.RIDER ? ride.getRiderId() : ride.getDriverId());
        return ride;
    }

    public List<Ride> findByDriverId(Long id, Long userId) {
        Long driverId = profileId(userId, Role.DRIVER);
        requireMatch(driverId, id);
        return rideRepo.findByDriverId(driverId);
    }

    public List<Ride> findByRiderId(Long id, Long userId) {
        Long riderId = profileId(userId, Role.RIDER);
        requireMatch(riderId, id);
        return rideRepo.findByRiderId(riderId);
    }

    @Override
    @Transactional
    public Ride completeRide(Long id, Long authenticatedUserId) {
        Driver driver = driverRepo.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new AccessDeniedException("Driver profile required"));
        Ride ride = rideRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
        if (!driver.getId().equals(ride.getDriverId())) {
            throw new AccessDeniedException("Ride is assigned to another driver");
        }
        ride.setStatus(RideStatus.COMPLETED);
        return rideRepo.save(ride);
    }
    public List<Ride> findByStatus(RideStatus status, Long userId, Role role) {
        Long id = profileId(userId, role);
        return role == Role.RIDER ? rideRepo.findByStatusAndRiderId(status, id)
                : rideRepo.findByStatusAndDriverId(status, id);
    }
}

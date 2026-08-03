package com.ridehailing.ride.model;

import com.ridehailing.driver.model.Driver;
import com.ridehailing.rider.model.Rider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rides")
@Getter @Setter @NoArgsConstructor
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private Long riderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    @Column(nullable = false)
    private double price;


}

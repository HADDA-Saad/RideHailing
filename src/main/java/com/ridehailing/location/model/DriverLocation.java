package com.ridehailing.location.model;

import com.ridehailing.driver.model.Driver;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;;

@Entity
@Table(name="driver_locations")
@Getter @Setter@NoArgsConstructor

public class DriverLocation {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private LocalDate timestamp;

}

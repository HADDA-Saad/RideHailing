package com.ridehailing.PriceEstimate.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "price_estimate")
@Getter @Setter @NoArgsConstructor
public class PriceEstimate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priceId;

    @Column(nullable = false)
    private Long rideId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare =BigDecimal.valueOf(5.0);

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distance;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal perKmRate = BigDecimal.valueOf(2.5);

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal surgeMultiplier = BigDecimal.valueOf(1.0);

}

package com.ridehailing.rider.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "riders")
@Getter @Setter @NoArgsConstructor
public class Rider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;


}

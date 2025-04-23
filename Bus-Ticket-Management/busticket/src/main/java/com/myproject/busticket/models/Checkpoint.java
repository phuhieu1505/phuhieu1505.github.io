package com.myproject.busticket.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "checkpoint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Checkpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkpoint_id")
    private int checkpointId;

    @Column(name = "place_name", nullable = false, length = 70)
    private String placeName;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(name = "province", nullable = false, length = 255)
    private String province;

    @Column(name = "city", nullable = false, length = 255)
    private String city;

    @Column(name = "phone", nullable = false, length = 12)
    private String phone;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

}

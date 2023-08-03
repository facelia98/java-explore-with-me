package ru.practicum.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "locations")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double lat;
    @Column(nullable = false)
    private Double lon;
}

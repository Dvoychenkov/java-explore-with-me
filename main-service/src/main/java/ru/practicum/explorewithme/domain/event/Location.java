package ru.practicum.explorewithme.domain.event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Location {

    @Column(name = "location_lat", nullable = false)
    private Double lat;

    @Column(name = "location_lon", nullable = false)
    private Double lon;
}

package io.github.trquinn76.entitydemo.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@jakarta.persistence.Entity
@Table(name = "entity")
public class Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "latitude", nullable = false)
    private Double latitudeDegrees;

    @Column(name = "longitude", nullable = false)
    private Double longitudeDegrees;

    @Column(name = "description")
    private String description;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitudeDegrees() {
        return latitudeDegrees;
    }

    public void setLatitudeDegrees(Double latitude) {
        this.latitudeDegrees = latitude;
    }

    public Double getLongitudeDegrees() {
        return longitudeDegrees;
    }

    public void setLongitudeDegrees(Double longitude) {
        this.longitudeDegrees = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // hashing on the Id only, as that field is not editable.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Entity other = (Entity) obj;
        return Objects.equals(description, other.description) && Objects.equals(id, other.id)
                && Objects.equals(latitudeDegrees, other.latitudeDegrees)
                && Objects.equals(longitudeDegrees, other.longitudeDegrees) && Objects.equals(name, other.name);
    }

}

package com.myproject.busticket.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import com.myproject.busticket.models.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    @Query(value = "SELECT r FROM Route r WHERE r.distance BETWEEN :min AND :max", nativeQuery = true)
    List<Route> findRouteByDistance(@Param("min") double min, @Param("max") double max);

    Optional<Route> findByCode(String code);

    Page<Route> findByCodeContainingOrNameContainingAllIgnoreCase(String code, String name, Pageable pageable);

}

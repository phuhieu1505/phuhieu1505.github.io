package com.myproject.busticket.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Checkpoint;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Integer> {
    List<Checkpoint> findByPlaceName(String placeName);

    @Query("Select distinct c.province from Checkpoint c")
    List<String> getAllProvinces();

    @Query("Select distinct c.city from Checkpoint c")
    List<String> getAllCities();

    @Query("""
                SELECT c.province
                FROM Checkpoint c
                JOIN Route_Checkpoint rc ON c.checkpointId = rc.checkpoint.checkpointId
                WHERE rc.route.code = :route_code AND rc.type = 'departure'
            """)
    String findDepartureName(@Param("route_code") String routeCode);

    @Query("""
                SELECT c.province
                FROM Checkpoint c
                JOIN Route_Checkpoint rc ON c.checkpointId = rc.checkpoint.checkpointId
                WHERE rc.route.code = :route_code AND rc.type = 'drop_off'
            """)
    String findDropOffName(@Param("route_code") String routeCode);

    Page<Checkpoint> findByPlaceNameContainingOrAddressContainingOrProvinceContainingOrCityContainingOrPhoneContainingOrRegionContainingAllIgnoreCase(
            String searchValue, String searchValue2, String searchValue3, String searchValue4, String searchValue5,
            String searchValue6, Pageable pageable);
}

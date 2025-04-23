package com.myproject.busticket.repositories;

import com.myproject.busticket.dto.RouteTripCountDTO;
import com.myproject.busticket.models.Bus;
import com.myproject.busticket.models.Controller;
import com.myproject.busticket.models.Driver;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.models.Staff;
import com.myproject.busticket.models.Trip;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {
    @Query(value = "SELECT t.*\n" +
            "FROM `trip` t\n" +
            "LEFT JOIN bus AS bus ON t.bus_id = bus.bus_id\n" +
            "LEFT JOIN booking AS b ON t.trip_id = b.trip_id\n" +
            "WHERE t.route_code = (\n" +
            "    SELECT rc1.route_code\n" +
            "    FROM `route_checkpoint` rc1\n" +
            "    JOIN checkpoint c1 ON rc1.checkpoint_id = c1.checkpoint_id\n" +
            "    JOIN route_checkpoint rc2 ON rc1.route_code = rc2.route_code\n" +
            "    JOIN checkpoint c2 ON rc2.checkpoint_id = c2.checkpoint_id\n" +
            "    WHERE rc1.type = \"departure\" AND (c1.city =:departure OR c1.province =:departure)\n" +
            "    AND  rc2.type = \"drop_off\" AND (c2.city =:destination OR c2.province =:destination)\n" +
            ")\n" +
            "AND status = \"waiting\"\n" +
            "AND DATE(t.departure_time) =:departureDate\n" +
            "GROUP BY t.trip_id, bus.number_of_seat\n" +
            "HAVING (bus.number_of_seat - IFNULL(SUM(b.number_of_seat), 0)) >=:numberOfTickets", nativeQuery = true)
    List<Trip> findTrip(@Param("departure") String departure,
            @Param("destination") String destination,
            @Param("departureDate") LocalDateTime departureDate,
            @Param("numberOfTickets") int numberOfTickets);

    @Query("SELECT t FROM Trip t WHERE t.status = 'waiting'")
    List<Trip> findAllWaitingTrips();

    @Query()
    int findNumberOfSeatAvailableByTripId(int tripId);

    List<Trip> findByRoute(Route route);

    List<Trip> findByBus(Bus bus);

    @Query("SELECT t FROM Trip t WHERE t.status = 'waiting' AND t.departureTime > :currentTime")
    List<Trip> findUpcomingTrips(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT t FROM Trip t WHERE t.status = 'arriving' OR t.status = 'waiting' AND t.departureTime <= :currentTime AND t.arrivalTime >= :currentTime")
    List<Trip> findOnGoingTrips(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT t FROM Trip t WHERE t.departureTime >= :startDate AND t.arrivalTime <= :endDate AND " +
            "(t.route.code LIKE %:query% OR t.bus.plate LIKE %:query% OR t.driver.account.fullName LIKE %:query%)")
    Page<Trip> findBySearchValueAndDepartureTimeBetween(@Param("query") String query,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE t.arrivalTime <= :endDate AND " +
            "(t.route.code LIKE %:query% OR t.bus.plate LIKE %:query% OR t.driver.account.fullName LIKE %:query%)")
    Page<Trip> findBySearchValueAndArrivalTimeBefore(@Param("query") String query,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE t.departureTime >= :startDate AND " +
            "(t.route.code LIKE %:query% OR t.bus.plate LIKE %:query% OR t.driver.account.fullName LIKE %:query%)")
    Page<Trip> findBySearchValueAndDepartureTimeAfter(@Param("query") String query,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE t.route.code LIKE %:query% OR t.bus.plate LIKE %:query% OR t.driver.account.fullName LIKE %:query%")
    Page<Trip> findBySearchValue(@Param("query") String query, Pageable pageable);

    @Query("SELECT new com.myproject.busticket.dto.RouteTripCountDTO(t.route.code, COUNT(t)) " +
            "FROM Trip t " +
            "WHERE (:routeCode IS NULL OR t.route.code = :routeCode) " +
            "GROUP BY t.route.code " +
            "ORDER BY COUNT(t) DESC")
    List<RouteTripCountDTO> findTripCountByRouteCode(@Param("routeCode") String routeCode, Pageable pageable);

    List<Trip> findByDriver(Driver driver);

    List<Trip> findByController(Controller controller);

    List<Trip> findByStaff(Staff staff);
}

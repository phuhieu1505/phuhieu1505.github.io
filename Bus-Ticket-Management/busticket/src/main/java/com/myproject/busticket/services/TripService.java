package com.myproject.busticket.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.myproject.busticket.dto.RouteTripCountDTO;
import com.myproject.busticket.dto.TripDTO;
import com.myproject.busticket.mapper.TripMapper;
import com.myproject.busticket.models.Bus;
import com.myproject.busticket.models.Controller;
import com.myproject.busticket.models.Driver;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.models.Staff;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.repositories.TripRepository;

@Service
public class TripService {
    @Autowired
    private TripRepository tripRepository;
    TripMapper tripMapper = Mappers.getMapper(TripMapper.class);

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public TripDTO findById(int tripId) {
        Optional<Trip> trip = tripRepository.findById(tripId);
        return tripMapper.entityToDTO(trip.get());
    }

    public List<Trip> findAll() {
        return tripRepository.findAll();
    }

    public List<Trip> findByBus(Bus bus) {
        return tripRepository.findByBus(bus);
    }

    public List<TripDTO> searchTrip(String departure, String destination, LocalDateTime departureDate,
            int numberOfTickets) {
        List<Trip> trip = tripRepository.findTrip(departure, destination, departureDate, numberOfTickets);
        return tripMapper.map(trip);
    }

    public List<Trip> findTripByRouteCode(Route routeCode) {
        return tripRepository.findByRoute(routeCode);
    }

    public Trip findTripById(int tripId) {
        Optional<Trip> trip = tripRepository.findById(tripId);
        return trip.get();
    }

    public Trip save(Trip trip) {
        return tripRepository.save(trip);
    }

    public Page<Trip> getAll(Pageable pageable) {
        return tripRepository.findAll(pageable);
    }

    public List<Trip> findConflictingTripsByBus(String busPlate, LocalDateTime departureTime,
            LocalDateTime arrivalTime) {
        List<Trip> waitingTrips = tripRepository.findAllWaitingTrips();

        List<Trip> conflictingTrips = waitingTrips.stream()
                .filter(trip -> trip.getBus().getPlate().equals(busPlate))
                .filter(trip -> !(departureTime.isBefore(trip.getDepartureTime())
                        && arrivalTime.isBefore(trip.getDepartureTime())) &&
                        !(departureTime.isAfter(trip.getArrivalTime()) && arrivalTime.isAfter(trip.getArrivalTime())))
                .collect(Collectors.toList());

        return conflictingTrips;
    }

    public List<Trip> findConflictingTripsByController(int controllerId, LocalDateTime departureTime,
            LocalDateTime arrivalTime) {
        List<Trip> waitingTrips = tripRepository.findAllWaitingTrips();

        List<Trip> conflictingTrips = waitingTrips.stream()
                .filter(trip -> trip.getController().getId() == controllerId)
                .filter(trip -> !(departureTime.isBefore(trip.getDepartureTime())
                        && arrivalTime.isBefore(trip.getDepartureTime())) &&
                        !(departureTime.isAfter(trip.getArrivalTime()) && arrivalTime.isAfter(trip.getArrivalTime())))
                .collect(Collectors.toList());

        return conflictingTrips;
    }

    public List<Trip> findConflictingTripsByDriver(int driverId, LocalDateTime departureTime,
            LocalDateTime arrivalTime) {
        List<Trip> waitingTrips = tripRepository.findAllWaitingTrips();

        List<Trip> conflictingTrips = waitingTrips.stream()
                .filter(trip -> trip.getDriver().getDriverId() == driverId)
                .filter(trip -> !(departureTime.isBefore(trip.getDepartureTime())
                        && arrivalTime.isBefore(trip.getDepartureTime())) &&
                        !(departureTime.isAfter(trip.getArrivalTime()) && arrivalTime.isAfter(trip.getArrivalTime())))
                .collect(Collectors.toList());

        return conflictingTrips;
    }

    public List<Trip> findUpcomingTrips(LocalDateTime currentTime) {
        return tripRepository.findUpcomingTrips(currentTime);
    }

    public List<Trip> findOnGoingTrips(LocalDateTime currentTime) {
        for (Trip trip : tripRepository.findOnGoingTrips(currentTime)) {
            System.out.println("TripService findOnGoingTrips tripId:" + trip.getTripId());
        }
        return tripRepository.findOnGoingTrips(currentTime);
    }

    public void deleteTripById(int tripId) {
        tripRepository.deleteById(tripId);
    }

    public Page<Trip> searchTrips(Pageable pageable, String query, LocalDateTime startDate, LocalDateTime endDate) {
        if (query == null) {
            query = "";
        }
        if (startDate == null && endDate == null) {
            return tripRepository.findBySearchValue(query, pageable);
        } else if (startDate == null) {
            return tripRepository.findBySearchValueAndArrivalTimeBefore(query, endDate, pageable);
        } else if (endDate == null) {
            return tripRepository.findBySearchValueAndDepartureTimeAfter(query, startDate, pageable);
        } else {
            return tripRepository.findBySearchValueAndDepartureTimeBetween(query, startDate, endDate, pageable);
        }
    }

    public List<RouteTripCountDTO> getTripCountByRouteCode(String routeCode) {
        Pageable pageable = PageRequest.of(0, 5);
        return tripRepository.findTripCountByRouteCode(routeCode, pageable);
    }

    public List<Trip> findTripsByController(Controller controller) {
        return tripRepository.findByController(controller);
    }

    public List<Trip> findTripsByStaff(Staff staff) {
        return tripRepository.findByStaff(staff);
    }

    public List<Trip> findTripsByDriver(Driver driver) {
        return tripRepository.findByDriver(driver);
    }
}

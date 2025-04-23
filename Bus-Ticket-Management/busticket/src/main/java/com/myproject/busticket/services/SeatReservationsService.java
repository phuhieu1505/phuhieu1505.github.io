package com.myproject.busticket.services;

import com.myproject.busticket.dto.SeatReservationsDTO;
import com.myproject.busticket.mapper.SeatReservationsMapper;
import com.myproject.busticket.models.Booking;
import com.myproject.busticket.models.Bus_Seats;
import com.myproject.busticket.models.SeatReservations;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.repositories.SeatReservationsRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatReservationsService {
    @Autowired
    private SeatReservationsRepository seatReservationsRepository;

    SeatReservationsMapper seatReservationsMapper = Mappers.getMapper(SeatReservationsMapper.class);

    public SeatReservationsService(SeatReservationsRepository seatReservationsRepository) {
        this.seatReservationsRepository = seatReservationsRepository;
    }

    public List<SeatReservationsDTO> getListStatusSeat(int TripId) {
        List<SeatReservations> seatReservations = seatReservationsRepository.findByTrip_TripId(TripId);
        try {
            return seatReservationsMapper.map(seatReservations);
        } catch (Exception e){
            System.out.println("Seat reservation" + e);
        }
        return null;
    }

    public List<SeatReservations> getBySeat(Bus_Seats bus_Seats) {
        return seatReservationsRepository.findBySeat(bus_Seats);
    }

    public SeatReservations save(SeatReservations seatReservations) {
        return seatReservationsRepository.save(seatReservations);
    }

    public void delete(SeatReservations seatReservations) {
        seatReservationsRepository.delete(seatReservations);
    }

    public List<SeatReservations> getByTrip(Trip trip) {
        return seatReservationsRepository.findByTrip(trip);
    }

    public SeatReservations getReservationBySeatAndTrip(Bus_Seats seat, Trip trip) {
        List<SeatReservations> seatReservations = seatReservationsRepository.findBySeatIdAndTripId(seat.getId(),
                trip.getTripId());
        if (seatReservations.size() > 0) {
            return seatReservations.get(0);
        }
        return null;
    }

    public void deleteAll(List<SeatReservations> reservations) {
        seatReservationsRepository.deleteAll(reservations);
    }

    public List<SeatReservations> findByBooking(Booking booking) {
        return seatReservationsRepository.findByBooking(booking);
    }

    public void updateStatusReserved(List<Integer> seatId, int tripId){
        seatReservationsRepository.updateStatusReserved(seatId, tripId);
    }
    public void updateStatusWithBooking(List<Integer> seatId, int bookingId) {
        seatReservationsRepository.updateStatusWithBooking(seatId, bookingId);
    }
}

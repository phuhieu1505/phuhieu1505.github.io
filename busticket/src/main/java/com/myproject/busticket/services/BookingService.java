package com.myproject.busticket.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.myproject.busticket.dto.BookingInfoDTO;
import com.myproject.busticket.enums.PaymentMethod;
import com.myproject.busticket.enums.TicketType;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Bill;
import com.myproject.busticket.models.Bill_Detail;
import com.myproject.busticket.models.Booking;
import com.myproject.busticket.models.Customer;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.repositories.BookingRepository;
import com.myproject.busticket.utilities.SecurityUtil;

import jakarta.mail.MessagingException;

@Service
public class BookingService {
    private BookingRepository bookingRepository;
    private TripService tripService;
    private AccountService accountService;
    private CustomerService customerService;
    private SeatReservationsService seatReservationsService;
    private BillService billService;
    private BillDetailService billDetailService;

    private EmailService emailService;
    private Bus_SeatsService busSeatsService;

    public BookingService(BookingRepository bookingRepository,
            TripService tripService,
            AccountService accountService,
            CustomerService customerService,
            SeatReservationsService seatReservationsService,
            BillService billService,
            BillDetailService billDetailService,
            EmailService emailService,
            Bus_SeatsService busSeatsService) {
        this.bookingRepository = bookingRepository;
        this.tripService = tripService;
        this.accountService = accountService;
        this.customerService = customerService;
        this.seatReservationsService = seatReservationsService;
        this.billService = billService;
        this.billDetailService = billDetailService;
        this.emailService = emailService;
        this.busSeatsService = busSeatsService;
    }

    public Booking getBookingById(int bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    public String checkLogin() {
        Account account = SecurityUtil.getCurrentAccount();
        return account != null ? account.getEmail() : null;
    }

    public void createTicketOneWay(BookingInfoDTO booking, LocalDateTime paymentDate) {
        Booking newBooking = new Booking();
        Customer newCustomer = new Customer();

        // xử lý thông tin khách hàng
        String email = null;
        // Có login
        if (checkLogin() != null) {
            // đã từng đặt xe chưa, rồi thì lấy cus đó
            if (customerService.existsByEmail(checkLogin())) {
                newBooking.setCustomer(customerService.getCustomerByEmail(checkLogin()));
                email = checkLogin();
            } else { // chưa thì thêm cus
                newCustomer.setEmail(checkLogin());
                newCustomer.setName(booking.getCustomer().getName());
                newCustomer.setPhone(booking.getCustomer().getPhone());
                customerService.create(newCustomer);
                newBooking.setCustomer(newCustomer);

                email = checkLogin();
            }

        } else { // ko login
            email = booking.getCustomer().getEmail();
            // email có tồn tại account thì dùng account đó
            if (accountService.getUserByEmail(email).isPresent()) {
                newBooking.setCustomer(customerService.getCustomerByEmail(email));
            } else if (customerService.existsByEmail(email)) { // Nếu đã là kh mà chưa có account thì lấy cus
                newBooking.setCustomer(customerService.getCustomerByEmail(email));
            } else { // Nếu ko thì là kh lần đầu => tạo account mới
                newCustomer.setEmail(email);
                newCustomer.setName(booking.getCustomer().getName());
                newCustomer.setPhone(booking.getCustomer().getPhone());
                customerService.create(newCustomer);
                newBooking.setCustomer(newCustomer);
            }
        }

        // lưu vé
        int tripId = booking.getTicketInfoDTO().getTripId();
        newBooking.setTrip(tripService.findTripById(tripId));

        byte numberOfSeat = booking.getTicketInfoDTO().getNumberOfSeat();
        newBooking.setNumberOfSeat(numberOfSeat);
        newBooking.setRoundTrip(false);
        newBooking.setRoundTripId(null);
        bookingRepository.save(newBooking);

        // đổi trạng thái số ghế
        List<Integer> listSeatId = booking.getTicketInfoDTO().getSeatNumbers().stream()
                .map(Integer::valueOf)
                .toList();
        int bookingID = newBooking.getBookingId();
        seatReservationsService.updateStatusWithBooking(listSeatId, bookingID);

        // lấy seat name tương ứng với từng seat id
        List<String> seatName = new ArrayList<>();
        for (Integer seatId : listSeatId) {
            seatName.add((busSeatsService.getSeatNameById(seatId)));
        }

        // lưu hóa đơn
        Bill bill = new Bill();
        bill.setCustomer(newBooking.getCustomer());
        bill.setPaymentMethod(PaymentMethod.vnpay);
        bill.setPaymentDate(paymentDate);
        billService.save(bill);

        // lưu chi tiết hóa đơn
        Bill_Detail billDetail = new Bill_Detail();
        billDetail.setBill(bill);
        billDetail.setTrip(newBooking.getTrip());
        billDetail.setNumberOfTicket(numberOfSeat);
        billDetail.setFee(newBooking.getTrip().getPrice() * numberOfSeat);
        billDetail.setTicketType(TicketType.one_way_ticket);
        billDetailService.save(billDetail);

        // send Email
        Context context = new Context(Locale.forLanguageTag("vi"));
        String subject = "[EASYBUS] HÓA ĐƠN ĐIỆN TỬ CỦA VÉ SỐ " + newBooking.getBookingId();
        context.setVariable("routeName", newBooking.getTrip().getRoute().getName());
        context.setVariable("departureTime", newBooking.getTrip().getDepartureTime());
        context.setVariable("numberOfSeat", numberOfSeat);
        context.setVariable("listSeat", seatName);
        context.setVariable("totalFee", billDetail.getFee());
        try {
            emailService.sendBookingEmail(email, subject, "email-template", context);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public void createTicketRoundTrip(List<BookingInfoDTO> booking, LocalDateTime paymentDate) {
        Booking newBookingTrip = new Booking();
        Booking newBookingRoundTrip = new Booking();
        Customer newCustomer = new Customer();

        // xử lý thông tin khách hàng
        String email = null;
        // Có login
        if (checkLogin() != null) {
            // đã từng đặt xe chưa, rồi thì lấy cus đó
            if (customerService.existsByEmail(checkLogin())) {
                newBookingTrip.setCustomer(customerService.getCustomerByEmail(checkLogin()));
                newBookingRoundTrip.setCustomer(customerService.getCustomerByEmail(checkLogin()));
                email = checkLogin();
            } else { // chưa thì thêm cus
                newCustomer.setEmail(checkLogin());
                customerService.create(newCustomer);

                newBookingTrip.setCustomer(newCustomer);
                newBookingRoundTrip.setCustomer(newCustomer);

                email = checkLogin();
            }

        } else { // ko login
            email = booking.get(0).getCustomer().getEmail();
            // email có tồn tại account thì dùng account đó
            if (accountService.getUserByEmail(email).isPresent()) {
                newBookingTrip.setCustomer(customerService.getCustomerByEmail(email));
                newBookingRoundTrip.setCustomer(customerService.getCustomerByEmail(email));
            } else if (customerService.existsByEmail(email)) { // Nếu đã là kh mà chưa có account thì lấy cus
                newBookingTrip.setCustomer(customerService.getCustomerByEmail(email));
                newBookingRoundTrip.setCustomer(customerService.getCustomerByEmail(email));
            } else { // Nếu ko thì là kh lần đầu => tạo account mới
                newCustomer.setEmail(email);
                newCustomer.setName(newBookingTrip.getCustomer().getName());
                newCustomer.setPhone(newBookingTrip.getCustomer().getPhone());
                customerService.create(newCustomer);
                newBookingTrip.setCustomer(newCustomer);
                newBookingTrip.setCustomer(newCustomer);
            }
        }

        // lưu vé
        int tripId = booking.get(0).getTicketInfoDTO().getTripId();
        int returnTripId = booking.get(1).getTicketInfoDTO().getTripId();
        newBookingTrip.setTrip(tripService.findTripById(tripId));
        newBookingRoundTrip.setTrip(tripService.findTripById(returnTripId));

        byte numberOfSeatTrip = booking.get(0).getTicketInfoDTO().getNumberOfSeat();
        byte numberOfSeatRoundTrip = booking.get(1).getTicketInfoDTO().getNumberOfSeat();
        newBookingTrip.setNumberOfSeat(numberOfSeatTrip);
        newBookingRoundTrip.setNumberOfSeat(numberOfSeatRoundTrip);
        newBookingTrip.setRoundTrip(true);
        newBookingRoundTrip.setRoundTrip(true);
        newBookingTrip.setRoundTripId(
                "RT" + newBookingRoundTrip.getBookingId() + newBookingTrip.getCustomer().getCustomerId());
        newBookingRoundTrip.setRoundTripId(
                "RT" + newBookingRoundTrip.getBookingId() + newBookingTrip.getCustomer().getCustomerId());
        bookingRepository.save(newBookingTrip);
        bookingRepository.save(newBookingRoundTrip);

        // đổi trạng thái số ghế
        List<Integer> listTripSeatId = booking.get(0).getTicketInfoDTO().getSeatNumbers().stream()
                .map(Integer::valueOf)
                .toList();
        int bookingTripId = newBookingTrip.getBookingId();
        seatReservationsService.updateStatusWithBooking(listTripSeatId,
                bookingTripId);

        List<Integer> listRoundTripSeatId = booking.get(1).getTicketInfoDTO().getSeatNumbers().stream()
                .map(Integer::valueOf)
                .toList();
        int bookingRoundTripId = newBookingTrip.getBookingId();
        seatReservationsService.updateStatusWithBooking(listRoundTripSeatId,
                bookingRoundTripId);

        // lấy seat name tương ứng với từng seat id
        List<String> seatNameTrip = new ArrayList<>();
        for (Integer seatId : listTripSeatId) {
            seatNameTrip.add((busSeatsService.getSeatNameById(seatId)));
        }

        List<String> seatNameRoundTrip = new ArrayList<>();
        for (Integer seatId : listRoundTripSeatId) {
            seatNameRoundTrip.add((busSeatsService.getSeatNameById(seatId)));
        }

        // lưu hóa đơn
        Bill bill = new Bill();
        bill.setCustomer(newBookingTrip.getCustomer());
        bill.setPaymentMethod(PaymentMethod.vnpay);
        bill.setPaymentDate(paymentDate);
        billService.save(bill);

        // lưu chi tiết hóa đơn
        Bill_Detail billDetailTrip = new Bill_Detail();
        Bill_Detail billDetailRoundTrip = new Bill_Detail();
        billDetailTrip.setBill(bill);
        billDetailTrip.setTrip(newBookingTrip.getTrip());
        billDetailTrip.setNumberOfTicket(numberOfSeatTrip);
        billDetailTrip.setFee(newBookingTrip.getTrip().getPrice() * numberOfSeatTrip);
        billDetailTrip.setTicketType(TicketType.round_trip_ticket);

        billDetailRoundTrip.setBill(bill);
        billDetailRoundTrip.setTrip(newBookingRoundTrip.getTrip());
        billDetailRoundTrip.setNumberOfTicket(numberOfSeatRoundTrip);
        billDetailRoundTrip.setFee(newBookingRoundTrip.getTrip().getPrice() * numberOfSeatRoundTrip);
        billDetailRoundTrip.setTicketType(TicketType.round_trip_ticket);

        billDetailService.save(billDetailTrip);
        billDetailService.save(billDetailRoundTrip);

        // send Email
        Context context = new Context(Locale.forLanguageTag("vi"));
        String subject = "[EASYBUS] HÓA ĐƠN ĐIỆN TỬ CỦA VÉ KHỨ HỒI SỐ" + newBookingTrip.getBookingId() + "VÀ"
                + newBookingRoundTrip.getBookingId();
        context.setVariable("routeNameTrip", newBookingTrip.getTrip().getRoute().getName());
        context.setVariable("departureTimeTrip", newBookingTrip.getTrip().getDepartureTime());
        context.setVariable("numberOfSeatTrip", numberOfSeatTrip);
        context.setVariable("seatNameTrip", seatNameTrip);
        context.setVariable("totalFeeTrip", billDetailTrip.getFee());

        context.setVariable("routeNameRoundTrip", newBookingRoundTrip.getTrip().getRoute().getName());
        context.setVariable("departureTimeRoundTrip", newBookingRoundTrip.getTrip().getDepartureTime());
        context.setVariable("numberOfSeatRoundTrip", numberOfSeatRoundTrip);
        context.setVariable("seatNameRoundTrip", seatNameRoundTrip);
        context.setVariable("totalFeeRoundTrip", billDetailRoundTrip.getFee());
        try {
            emailService.sendBookingEmail(email, subject, "roundTripEmail-template", context);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public List<Booking> findByTrip(Trip trip) {
        return bookingRepository.findByTrip(trip);
    }

    public void delete(Booking booking) {
        bookingRepository.delete(booking);
    }

    public void deleteAll(List<Booking> bookings) {
        bookingRepository.deleteAll(bookings);
    }

    public Page<Booking> getAll(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public List<Booking> findByRoundTripId(String roundTripId) {
        return bookingRepository.findByRoundTripId(roundTripId);
    }

    public List<Booking> findBookingDetailsByCustomerId(int customerId) {
        return bookingRepository.findBookingDetailsByCustomerId(customerId);
    }

    public List<Booking> findByCustomer(Customer customer) {
        return bookingRepository.findByCustomer(customer);
    }

    public Page<Booking> searchBookings(String searchValue, Pageable pageable) {
        return bookingRepository.searchBookings(searchValue, pageable);
    }

}

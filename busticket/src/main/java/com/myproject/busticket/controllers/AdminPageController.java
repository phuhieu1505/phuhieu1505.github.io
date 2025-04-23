package com.myproject.busticket.controllers;

import com.myproject.busticket.dto.AccountDTO;
import com.myproject.busticket.dto.BookingDTO;
import com.myproject.busticket.dto.BusDTO;
import com.myproject.busticket.dto.ControllerDTO;
import com.myproject.busticket.dto.CustomerDTO;
import com.myproject.busticket.dto.DriverDTO;
import com.myproject.busticket.dto.RouteDTO;
import com.myproject.busticket.dto.StaffDTO;
import com.myproject.busticket.dto.TripDTO;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Bill;
import com.myproject.busticket.models.Booking;
import com.myproject.busticket.models.Bus;
import com.myproject.busticket.models.Checkpoint;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.services.AccountService;
import com.myproject.busticket.services.BillService;
import com.myproject.busticket.services.BookingService;
import com.myproject.busticket.services.BusService;
import com.myproject.busticket.services.CheckpointService;
import com.myproject.busticket.services.RoleService;
import com.myproject.busticket.services.RouteService;
import com.myproject.busticket.services.TripService;
import com.myproject.busticket.utilities.SecurityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequestMapping("/easy-bus")
@Controller
public class AdminPageController {
	@Autowired
	private TripService tripService;

	@Autowired
	private BusService busService;

	@Autowired
	private RouteService routeService;

	@Autowired
	private CheckpointService checkpointService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private BillService billService;

	@GetMapping("/dashboard")
	public String dashBoardPage() {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		if (SecurityUtil.hasRole("ADMIN") || SecurityUtil.hasRole("STAFF")) {
			return "admin";
		}
		return "redirect:/auth/login";
	}

	@GetMapping("/trip-management")
	public String adminTripPage(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Trip> tripsPage = tripService.getAll(pageable);

		int totalPages = tripsPage.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);

		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("trips", tripsPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", tripsPage.getTotalPages());
		model.addAttribute("pageNumbers", pageNumbers);

		return "trip-management";
	}

	@GetMapping("/bus-management")
	public String adminBusPage(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Bus> busesPage = busService.getAll(pageable);

		int totalPages = busesPage.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);

		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("buses", busesPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", busesPage.getTotalPages());
		model.addAttribute("pageNumbers", pageNumbers);

		return "bus-management";
	}

	@GetMapping("/route-management")
	public String adminRoutePage(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Route> routesPage = routeService.getAll(pageable);

		int totalPages = routesPage.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);

		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("routes", routesPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", routesPage.getTotalPages());
		model.addAttribute("pageNumbers", pageNumbers);

		return "route-management";
	}

	@GetMapping("/route-management/search")
	public String adminSearchByCodeAndName(@RequestParam String searchValue, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Route> routesSearchPage = routeService.searchRouteByCodeAndName(pageable, searchValue);

		int totalPages = routesSearchPage.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);

		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("routesSearch", routesSearchPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalSearchPages", routesSearchPage.getTotalPages());
		model.addAttribute("pageNumbers", pageNumbers);
		return "search-route";
	}

	@GetMapping("/checkpoint-management")
	public String adminCheckpointPage(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Checkpoint> checkpointPages = checkpointService.getAll(pageable);

		int totalPages = checkpointPages.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);

		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("checkpoints", checkpointPages.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", checkpointPages.getTotalPages());
		model.addAttribute("pageNumbers", pageNumbers);

		return "checkpoint-management";
	}

	@GetMapping("/account-management")
	public String adminUserPage(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account currentAccount = SecurityUtil.getCurrentAccount();
		if (currentAccount == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Account> accountPages = accountService.getAll(pageable);

		List<AccountDTO> accountDTOs = accountPages.getContent().stream()
				.map(account -> new AccountDTO(account.getId(), account.getEmail(),
						account.getPassword(),
						account.getFullName(), account.getPhone(),
						roleService.getRoleById(account.getRole().getRoleId()),
						account.getStatus(),
						account.getVerificationCode(), account.getVerificationExpiration(),
						account.getLoginToken(),
						account.getPasswordResetToken(), account.getPasswordResetExpiration(),
						account.isEnabled()))
				.collect(Collectors.toList());

		int totalPages = accountPages.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);

		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("accounts", accountDTOs);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("pageNumbers", pageNumbers);

		return "account-management";
	}

	@GetMapping("/booking-management")
	public String bookingPage(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Booking> bookingPages = bookingService.getAll(pageable);

		int totalPages = bookingPages.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);
		List<BookingDTO> bookings = bookingPages.getContent().stream()
				.map(booking -> new BookingDTO(
						booking.getBookingId(),
						new CustomerDTO(
								booking.getCustomer().getCustomerId(),
								booking.getCustomer().getEmail(),
								booking.getCustomer().getName(),
								booking.getCustomer().getPhone()),
						new TripDTO(
								booking.getTrip().getTripId(),
								booking.getTrip().getDepartureTime(),
								booking.getTrip().getArrivalTime(),
								booking.getTrip().getPrice(),
								booking.getTrip().getStatus(),
								new BusDTO(
										booking.getTrip().getBus().getPlate(),
										booking.getTrip().getBus().getNumberOfSeat(),
										booking.getTrip().getBus().getSeatType()),
								new DriverDTO(
										booking.getTrip().getDriver().getDriverId(),
										new AccountDTO(
												booking.getTrip().getDriver().getAccount().getId(),
												booking.getTrip().getDriver().getAccount().getEmail(),
												booking.getTrip().getDriver().getAccount().getPassword(),
												booking.getTrip().getDriver().getAccount().getFullName(),
												booking.getTrip().getDriver().getAccount().getPhone(),
												booking.getTrip().getDriver().getAccount().getRole(),
												booking.getTrip().getDriver().getAccount().getStatus(),
												booking.getTrip().getDriver().getAccount().getVerificationCode(),
												booking.getTrip().getDriver().getAccount().getVerificationExpiration(),
												booking.getTrip().getDriver().getAccount().getLoginToken(),
												booking.getTrip().getDriver().getAccount().getPasswordResetToken(),
												booking.getTrip().getDriver().getAccount().getPasswordResetExpiration(),
												booking.getTrip().getDriver().getAccount().isEnabled())),
								new ControllerDTO(
										booking.getTrip().getController().getId(),
										new AccountDTO(
												booking.getTrip().getController().getAccount().getId(),
												booking.getTrip().getController().getAccount().getEmail(),
												booking.getTrip().getController().getAccount().getPassword(),
												booking.getTrip().getController().getAccount().getFullName(),
												booking.getTrip().getController().getAccount().getPhone(),
												booking.getTrip().getController().getAccount().getRole(),
												booking.getTrip().getController().getAccount().getStatus(),
												booking.getTrip().getController().getAccount().getVerificationCode(),
												booking.getTrip().getController().getAccount()
														.getVerificationExpiration(),
												booking.getTrip().getController().getAccount().getLoginToken(),
												booking.getTrip().getController().getAccount().getPasswordResetToken(),
												booking.getTrip().getController().getAccount()
														.getPasswordResetExpiration(),
												booking.getTrip().getController().getAccount().isEnabled())),
								new StaffDTO(
										booking.getTrip().getStaff().getStaff_id(),
										new AccountDTO(
												booking.getTrip().getStaff().getAccount().getId(),
												booking.getTrip().getStaff().getAccount().getEmail(),
												booking.getTrip().getStaff().getAccount().getPassword(),
												booking.getTrip().getStaff().getAccount().getFullName(),
												booking.getTrip().getStaff().getAccount().getPhone(),
												booking.getTrip().getStaff().getAccount().getRole(),
												booking.getTrip().getStaff().getAccount().getStatus(),
												booking.getTrip().getStaff().getAccount().getVerificationCode(),
												booking.getTrip().getStaff().getAccount().getVerificationExpiration(),
												booking.getTrip().getStaff().getAccount().getLoginToken(),
												booking.getTrip().getStaff().getAccount().getPasswordResetToken(),
												booking.getTrip().getStaff().getAccount().getPasswordResetExpiration(),
												booking.getTrip().getStaff().getAccount().isEnabled())),
								new RouteDTO(
										booking.getTrip().getRoute().getRouteId(),
										booking.getTrip().getRoute().getCode(),
										booking.getTrip().getRoute().getName(),
										booking.getTrip().getRoute().getTime(),
										booking.getTrip().getRoute().getDistance())),
						booking.getNumberOfSeat(),
						booking.isRoundTrip(),
						booking.getRoundTripId()))
				.collect(Collectors.toList());
		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("bookings", bookings);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("pageNumbers", pageNumbers);

		return "booking-management";
	}

	@GetMapping("/bill-management")
	public String billPage(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int size, Model model) {
		Account account = SecurityUtil.getCurrentAccount();
		if (account == null) {
			return "redirect:/auth/login";
		}
		Pageable pageable = PageRequest.of(page, size);
		Page<Bill> billPages = billService.getAll(pageable);

		int totalPages = billPages.getTotalPages();
		int startPage = Math.max(0, page - 2);
		int endPage = Math.min(totalPages - 1, page + 2);

		List<Integer> pageNumbers = IntStream.rangeClosed(startPage, endPage)
				.boxed()
				.collect(Collectors.toList());

		model.addAttribute("bills", billPages.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", billPages.getTotalPages());
		model.addAttribute("pageNumbers", pageNumbers);

		return "bill-management";
	}

}

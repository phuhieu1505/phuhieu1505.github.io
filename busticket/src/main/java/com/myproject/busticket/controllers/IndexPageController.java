package com.myproject.busticket.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.myproject.busticket.dto.ScheduleDTO;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.services.CheckpointService;
import com.myproject.busticket.services.RouteCheckpointService;
import com.myproject.busticket.services.RouteService;

@RequestMapping("/home")
@Controller
public class IndexPageController {
    CheckpointService checkpointService;
    RouteCheckpointService routeCheckpointService;
    RouteService routeService;

    public IndexPageController(CheckpointService checkpointService, RouteCheckpointService routeCheckpointService,
            RouteService routeService) {
        this.checkpointService = checkpointService;
        this.routeService = routeService;
        this.routeCheckpointService = routeCheckpointService;
    }

    @GetMapping("/index")
    public String indexPage(Model model) {
        List<String> provinces = checkpointService.getAllProvinces();
        List<String> cities = checkpointService.getAllCities();
        model.addAttribute("tripType", "one-way");
        model.addAttribute("provinces", provinces);
        model.addAttribute("cities", cities);
        return "index";
    }

    @GetMapping("/schedule")
    public String schedulePage(Model model) {
        List<ScheduleDTO> schedules = new ArrayList<>();
        List<Route> routes = routeService.getAll();
        for (Route route : routes) {
            ScheduleDTO schedule = new ScheduleDTO();
            schedule.setCode(route.getCode());
            schedule.setDepartureName(checkpointService.findDepartureName(route.getCode()));
            schedule.setDropOffName(checkpointService.findDropOffName(route.getCode()));
            schedule.setTime(route.getTime());
            schedule.setDistance(route.getDistance());
            schedules.add(schedule);
        }
        model.addAttribute("schedules", schedules);
        return "schedule";
    }

    @GetMapping("/about-us")
    public String aboutUsPage() {
        return "about_us";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }

    @GetMapping("/search-ticket-info")
    public String searchTicketInfoPage() {
        return "search-ticket-info";
    }

    @GetMapping("/search-billing-info")
    public String searchBillingInfoPage() {
        return "search-billing-info";
    }

    @GetMapping("/403")
    public String error403PageTest() {
        return "403";
    }

    @GetMapping("/404")
    public String error404PageTest() {
        return "404";
    }

    @GetMapping("/vnpay")
    public String finnishBookingTest() {
        return "vnpay";
    }
    
    @GetMapping("/user-info")
    public String UserInfo() {
        return "user-info";
    }
}

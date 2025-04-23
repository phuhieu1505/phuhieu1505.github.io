package com.myproject.busticket.controllers;

import com.myproject.busticket.services.CustomerService;
import org.springframework.stereotype.Controller;

@Controller
public class CustomerController {
    CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

}

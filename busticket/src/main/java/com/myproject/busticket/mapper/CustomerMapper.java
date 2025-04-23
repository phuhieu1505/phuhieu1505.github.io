package com.myproject.busticket.mapper;

import org.mapstruct.Mapper;

import com.myproject.busticket.dto.CustomerDTO;
import com.myproject.busticket.models.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDTO entityToDTO(Customer customer);

    Customer dtoToEntity(CustomerDTO customerDTO);
}

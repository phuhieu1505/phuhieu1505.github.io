package com.myproject.busticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.myproject.busticket.dto.DriverDTO;
import com.myproject.busticket.models.Driver;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    DriverDTO entityToDTO(DriverDTO driverDTO);

    DriverDTO dtoToEntity(DriverDTO driverDTO);

    List<DriverDTO> map(List<Driver> drivers);
}

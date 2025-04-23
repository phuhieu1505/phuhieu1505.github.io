package com.myproject.busticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.myproject.busticket.dto.BusDTO;
import com.myproject.busticket.models.Bus;

@Mapper(componentModel = "spring")
public interface BusMapper {

    @Mapping(source = "plate", target = "plate")
    @Mapping(source = "numberOfSeat", target = "numberOfSeat")
    @Mapping(source = "seatType", target = "seatType")
    BusDTO entityToDTO(Bus bus);

    @Mapping(source = "plate", target = "plate")
    @Mapping(source = "numberOfSeat", target = "numberOfSeat")
    @Mapping(source = "seatType", target = "seatType")
    Bus dtoToEntity(BusDTO busDTO);

    List<BusDTO> map(List<Bus> buses);

}
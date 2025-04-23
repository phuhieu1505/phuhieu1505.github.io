package com.myproject.busticket.mapper;

import com.myproject.busticket.dto.BillDetailDTO;
import com.myproject.busticket.models.Bill_Detail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BillDetailMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "trip", target = "trip")
    @Mapping(source = "numberOfTicket", target = "numberOfTicket")
    @Mapping(source = "fee", target = "fee")
    @Mapping(source = "ticketType", target = "ticketType")
    @Mapping(source = "trip.staff.staff_id", target = "trip.staff.staff_id")
    @Mapping(source = "trip.controller.account.phone", target = "trip.controller.account.phone")
    @Mapping(source = "trip.driver.account.phone", target = "trip.driver.account.phone")
    @Mapping(source = "trip.staff.account.phone", target = "trip.staff.account.phone")
    BillDetailDTO entityToDTO(Bill_Detail billDetail);

    Bill_Detail dtoToEntity(BillDetailDTO billDetailDTO);

    List<BillDetailDTO> map(List<Bill_Detail> billDetails);
}
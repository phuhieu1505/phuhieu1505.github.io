package com.myproject.busticket.mapper;

import com.myproject.busticket.dto.BillDTO;
import com.myproject.busticket.models.Bill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillMapper {

    @Mapping(source = "billId", target = "billId")
    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "paymentMethod", target = "paymentMethod")
    @Mapping(source = "paymentDate", target = "paymentDate")
    BillDTO entityToDTO(Bill bill);

    Bill dtoToEntity(BillDTO billDTO);

    List<BillDTO> map(List<Bill> bills);
}
package com.myproject.busticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.myproject.busticket.dto.StaffDTO;
import com.myproject.busticket.models.Staff;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    StaffDTO entityToDTO(Staff staff);

    Staff dtoToEntity(StaffDTO staffDTO);

    List<StaffDTO> map(List<Staff> staffs);
}

package com.myproject.busticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.myproject.busticket.dto.ControllerDTO;
import com.myproject.busticket.models.Controller;

@Mapper(componentModel = "spring")
public interface ControllerMapper {

    ControllerDTO entityToDTO(ControllerDTO controllerDTO);

    ControllerDTO dtoToEntity(ControllerDTO controllerDTO);

    List<ControllerDTO> map(List<Controller> controllers);
}

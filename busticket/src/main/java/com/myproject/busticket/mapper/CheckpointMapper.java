package com.myproject.busticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.myproject.busticket.dto.CheckpointDTO;
import com.myproject.busticket.models.Checkpoint;

@Mapper(componentModel = "spring")
public interface CheckpointMapper {
    CheckpointDTO entityToDTO(Checkpoint checkpoint);

    Checkpoint dtoToEntity(CheckpointDTO checkpointDTO);

    List<CheckpointDTO> map(List<Checkpoint> checkpoints);
}

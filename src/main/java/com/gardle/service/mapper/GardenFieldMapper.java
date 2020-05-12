package com.gardle.service.mapper;

import com.gardle.domain.GardenField;
import com.gardle.service.dto.GardenFieldDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SimpleUserMapper.class})
public interface GardenFieldMapper extends EntityMapper<GardenFieldDTO, GardenField> {

    @Mapping(target = "coverImage", ignore = true)
    GardenField toEntity(GardenFieldDTO gardenFieldDTO);

    GardenFieldDTO toDto(GardenField entity);
}

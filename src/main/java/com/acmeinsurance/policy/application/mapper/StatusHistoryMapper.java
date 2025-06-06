package com.acmeinsurance.policy.application.mapper;

import com.acmeinsurance.policy.application.dto.model.StatusHistoryDTO;
import com.acmeinsurance.policy.domain.model.StatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StatusHistoryMapper {

    StatusHistoryMapper INSTANCE = Mappers.getMapper(StatusHistoryMapper.class);

    StatusHistoryDTO toDTO(final StatusHistory statusHistory);

    StatusHistory toDomain(final StatusHistoryDTO statusHistoryEntryDTO);
}
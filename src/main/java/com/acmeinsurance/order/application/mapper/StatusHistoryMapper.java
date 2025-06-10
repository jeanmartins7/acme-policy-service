package com.acmeinsurance.order.application.mapper;

import com.acmeinsurance.order.application.dto.policy.model.StatusHistoryDTO;
import com.acmeinsurance.order.domain.model.StatusHistoryEntry;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StatusHistoryMapper {

    StatusHistoryMapper INSTANCE = Mappers.getMapper(StatusHistoryMapper.class);

    StatusHistoryDTO toDTO(final StatusHistoryEntry statusHistoryEntry);

    StatusHistoryEntry toDomain(final StatusHistoryDTO statusHistoryEntryDTO);
}
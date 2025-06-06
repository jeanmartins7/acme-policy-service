package com.acmeinsurance.policy.application.mapper;

import com.acmeinsurance.policy.application.dto.request.PolicyRequestDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyRequestResponseDTO;
import com.acmeinsurance.policy.application.dto.response.PolicyResponseDTO;
import com.acmeinsurance.policy.domain.model.PolicyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StatusHistoryMapper.class})
public interface PolicyRequestMapper {

    PolicyRequestMapper INSTANCE = Mappers.getMapper(PolicyRequestMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "history", ignore = true)
    PolicyRequest toDomain(PolicyRequestDTO policyRequestDTO);


    PolicyRequestResponseDTO toCreationResponseDTO(final PolicyRequest policyRequest);

    PolicyResponseDTO toResponseDTO(final PolicyRequest policyRequest);

}
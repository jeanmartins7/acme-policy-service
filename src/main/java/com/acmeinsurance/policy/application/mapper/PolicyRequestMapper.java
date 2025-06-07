package com.acmeinsurance.policy.application.mapper;

import com.acmeinsurance.policy.application.dto.policy.request.PolicyRequestDTO;
import com.acmeinsurance.policy.application.dto.policy.response.PolicyRequestResponseDTO;
import com.acmeinsurance.policy.application.dto.policy.response.PolicyResponseDTO;
import com.acmeinsurance.policy.domain.model.PolicyRequest;
import com.acmeinsurance.policy.domain.usecase.command.CreatePolicyRequestCommand;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StatusHistoryMapper.class})
public interface PolicyRequestMapper {

    PolicyRequestMapper INSTANCE = Mappers.getMapper(PolicyRequestMapper.class);

    CreatePolicyRequestCommand toCreateCommand(final PolicyRequestDTO dto);

    PolicyRequestResponseDTO toCreationResponseDTO(final PolicyRequest policyRequest);

    PolicyResponseDTO toResponseDTO(final PolicyRequest policyRequest);

}
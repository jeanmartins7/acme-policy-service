package com.acmeinsurance.order.application.mapper;

import com.acmeinsurance.order.application.dto.policy.request.PolicyRequestDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyRequestResponseDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyResponseDTO;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.usecase.command.CreatePolicyRequestCommand;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {StatusHistoryMapper.class})
public interface PolicyRequestMapper {

    PolicyRequestMapper INSTANCE = Mappers.getMapper(PolicyRequestMapper.class);

    CreatePolicyRequestCommand toCreateCommand(final PolicyRequestDTO dto);

    PolicyRequestResponseDTO toCreationResponseDTO(final PolicyRequest policyRequest);

    PolicyResponseDTO toResponseDTO(final PolicyRequest policyRequest);

}
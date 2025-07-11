package com.acmeinsurance.order.infrastructure.integration.fraud.mapper;

import com.acmeinsurance.order.infrastructure.integration.fraud.dto.model.FraudAnalysisResult;
import com.acmeinsurance.order.infrastructure.integration.fraud.dto.response.FraudApiResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FraudApiMapper {

    FraudApiMapper INSTANCE = Mappers.getMapper(FraudApiMapper.class);

    FraudAnalysisResult toDomain(final FraudApiResponseDTO fraudApiResponseDTO);

}
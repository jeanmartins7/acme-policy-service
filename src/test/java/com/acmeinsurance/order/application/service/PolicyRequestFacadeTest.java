package com.acmeinsurance.order.application.service;

import com.acmeinsurance.order.application.dto.policy.request.PolicyRequestDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyRequestResponseDTO;
import com.acmeinsurance.order.application.dto.policy.response.PolicyResponseDTO;
import com.acmeinsurance.order.application.mapper.PolicyRequestMapper;
import com.acmeinsurance.order.domain.model.PolicyRequest;
import com.acmeinsurance.order.domain.usecase.CancelPolicyRequestUseCase;
import com.acmeinsurance.order.domain.usecase.CreatePolicyRequestUseCase;
import com.acmeinsurance.order.domain.usecase.GetPolicyRequestUseCase;
import com.acmeinsurance.order.domain.usecase.command.CreatePolicyRequestCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyRequestFacadeTest {

    private PolicyRequestMapper mapper;
    private CreatePolicyRequestUseCase createUseCase;
    private GetPolicyRequestUseCase getUseCase;
    private CancelPolicyRequestUseCase cancelUseCase;
    private PolicyRequestFacade facade;

    @BeforeEach
    void setUp() {
        mapper = mock(PolicyRequestMapper.class);
        createUseCase = mock(CreatePolicyRequestUseCase.class);
        getUseCase = mock(GetPolicyRequestUseCase.class);
        cancelUseCase = mock(CancelPolicyRequestUseCase.class);
        facade = new PolicyRequestFacade(mapper, createUseCase, getUseCase, cancelUseCase);
    }

    @Test
    void createPolicyRequest_shouldReturnResponseDTO() {
        PolicyRequestDTO dto = mock(PolicyRequestDTO.class);
        CreatePolicyRequestCommand command = mock(CreatePolicyRequestCommand.class);
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        PolicyRequestResponseDTO responseDTO = mock(PolicyRequestResponseDTO.class);

        when(mapper.toCreateCommand(dto)).thenReturn(command);
        when(createUseCase.execute(command)).thenReturn(Mono.just(policyRequest));
        when(mapper.toCreationResponseDTO(policyRequest)).thenReturn(responseDTO);

        StepVerifier.create(facade.createPolicyRequest(dto))
                .expectNext(responseDTO)
                .verifyComplete();

        verify(mapper).toCreateCommand(dto);
        verify(createUseCase).execute(command);
        verify(mapper).toCreationResponseDTO(policyRequest);
    }

    @Test
    void getPolicyRequestById_shouldReturnResponseDTO() {
        String id = "id";
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        PolicyResponseDTO responseDTO = mock(PolicyResponseDTO.class);

        when(getUseCase.executeById(id)).thenReturn(Mono.just(policyRequest));
        when(mapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        StepVerifier.create(facade.getPolicyRequestById(id))
                .expectNext(responseDTO)
                .verifyComplete();

        verify(getUseCase).executeById(id);
        verify(mapper).toResponseDTO(policyRequest);
    }

    @Test
    void getPolicyRequestsByCustomerId_shouldReturnFluxOfResponseDTO() {
        String customerId = "customer";
        PolicyRequest pr1 = mock(PolicyRequest.class);
        PolicyRequest pr2 = mock(PolicyRequest.class);
        PolicyResponseDTO dto1 = mock(PolicyResponseDTO.class);
        PolicyResponseDTO dto2 = mock(PolicyResponseDTO.class);

        when(getUseCase.executeByCustomerId(customerId)).thenReturn(Flux.just(pr1, pr2));
        when(mapper.toResponseDTO(pr1)).thenReturn(dto1);
        when(mapper.toResponseDTO(pr2)).thenReturn(dto2);

        StepVerifier.create(facade.getPolicyRequestsByCustomerId(customerId))
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();

        verify(getUseCase).executeByCustomerId(customerId);
        verify(mapper).toResponseDTO(pr1);
        verify(mapper).toResponseDTO(pr2);
    }

    @Test
    void deletePolicyRequestById_shouldReturnResponseDTO() {
        String id = "id";
        PolicyRequest policyRequest = mock(PolicyRequest.class);
        PolicyResponseDTO responseDTO = mock(PolicyResponseDTO.class);

        when(cancelUseCase.executeById(id)).thenReturn(Mono.just(policyRequest));
        when(mapper.toResponseDTO(policyRequest)).thenReturn(responseDTO);

        StepVerifier.create(facade.deletePolicyRequestById(id))
                .expectNext(responseDTO)
                .verifyComplete();

        verify(cancelUseCase).executeById(id);
        verify(mapper).toResponseDTO(policyRequest);
    }
}
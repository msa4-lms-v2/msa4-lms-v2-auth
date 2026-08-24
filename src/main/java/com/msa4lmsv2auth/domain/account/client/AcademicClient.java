package com.msa4lmsv2auth.domain.account.client;

import com.msa4lmsv2auth.domain.account.request.ProfessorProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.response.ProfessorProvisioningResponseDTO;
import com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AcademicClient {

    private final RestClient restClient;

    public AcademicClient(
            @Value("${services.academic.url}") String academicUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(academicUrl)
                .build();
    }

    public StudentProvisioningResponseDTO createStudent(
            StudentProvisioningRequestDTO request
    ) {
        GlobalResponseDTO<StudentProvisioningResponseDTO> response =
                restClient.post()
                        .uri("/api/academic/account-provisionings/students")
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.data() == null) {
            throw new IllegalStateException(
                    "Academic 응답이 비어 있습니다."
            );
        }

        return response.data();
    }

    public ProfessorProvisioningResponseDTO createProfessor(
            ProfessorProvisioningRequestDTO request
    ) {
        GlobalResponseDTO<ProfessorProvisioningResponseDTO> response =
                restClient.post()
                        .uri("/api/academic/account-provisionings/professors")
                        .body(request)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.data() == null) {
            throw new IllegalStateException(
                    "Academic 응답이 비어 있습니다."
            );
        }

        return response.data();
    }
}
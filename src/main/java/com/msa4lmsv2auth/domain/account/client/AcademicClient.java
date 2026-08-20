package com.msa4lmsv2auth.domain.account.client;

import com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO;
import org.springframework.beans.factory.annotation.Value;
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
        return restClient.post()
                .uri("/api/academic/account-provisionings")
                .body(request)
                .retrieve()
                .body(StudentProvisioningResponseDTO.class);
    }
}
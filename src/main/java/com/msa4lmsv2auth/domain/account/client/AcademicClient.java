package com.msa4lmsv2auth.domain.account.client;

import com.msa4lmsv2auth.domain.account.request.ProfessorProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO;
import com.msa4lmsv2auth.domain.account.response.ProfessorProvisioningResponseDTO;
import com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AcademicClient {

    private final RestClient restClient;

    public AcademicClient(
            @Value("${services.academic.url}") String academicUrl
    ) {
        // Outbox Worker가 동기 호출하므로 무한 대기를 막기 위해 connect/read timeout을 명시한다.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .baseUrl(academicUrl)
                .requestFactory((ClientHttpRequestFactory) requestFactory)
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
                        .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                            throw new AcademicProvisioningRejectedException(
                                    "Academic 학생 프로비저닝 요청이 거부됐습니다(상태 " + res.getStatusCode().value() + ").");
                        })
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
                        .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                            throw new AcademicProvisioningRejectedException(
                                    "Academic 교수 프로비저닝 요청이 거부됐습니다(상태 " + res.getStatusCode().value() + ").");
                        })
                        .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.data() == null) {
            throw new IllegalStateException(
                    "Academic 응답이 비어 있습니다."
            );
        }

        return response.data();
    }
}

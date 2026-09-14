package com.msa4lmsv2auth.domain.account.client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import com.msa4lmsv2auth.global.response.GlobalResponseDTO;
import java.util.Map;
@Component
public class AdmissionClient {
    private final RestClient client; private final String token;
    public AdmissionClient(@Value("${ADMISSION_ACADEMIC_BASE_URL:http://localhost:8082}") String url,@Value("${services.admission.auth-token:${ADMISSION_AUTH_TOKEN:}}") String token) {
        this.token=token; var factory=new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(2000); factory.setReadTimeout(5000);
        client=RestClient.builder().baseUrl(url).requestFactory(factory).defaultHeader("X-Admission-Token",token).build();
    }
    public void requirePaid(Long id) {
        if(token.isBlank()) throw new IllegalStateException("입학 서비스 인증 설정이 필요합니다.");
        GlobalResponseDTO<Map<String,Object>> response=client.get().uri("/api/academic/internal/admissions/{id}",id).retrieve().body(new ParameterizedTypeReference<>(){});
        if(response==null || response.data()==null || !Boolean.TRUE.equals(response.data().get("tuitionPaid")) || "CANCELLED".equals(response.data().get("status")))
            throw new IllegalStateException("등록금 완납이 확인되지 않았습니다.");
    }
    public com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO provision(com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO request) {
        if(token.isBlank())throw new IllegalStateException("입학 서비스 인증 설정이 필요합니다.");
        GlobalResponseDTO<com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO> r=client.post()
                .uri("/api/academic/internal/admissions/{id}/student",request.admissionCandidateId()).body(request).retrieve().body(new ParameterizedTypeReference<>(){});
        if(r==null || r.data()==null)throw new IllegalStateException("학번 응답 없음");return r.data();
    }
    public void activated(Long id,Long accountId) {
        if(token.isBlank()) throw new IllegalStateException("입학 서비스 인증 설정이 필요합니다.");
        client.post().uri("/api/academic/internal/admissions/{id}/activated",id).body(Map.of("accountId",accountId)).retrieve().toBodilessEntity();
    }
}

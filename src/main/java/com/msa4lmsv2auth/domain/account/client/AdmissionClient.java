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
    private final RestClient client;
    public AdmissionClient(@Value("${ADMISSION_ACADEMIC_BASE_URL:http://localhost:8082}") String url) {
        var factory=new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(2000); factory.setReadTimeout(5000);
        client=RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    public void requirePaid(Long id) {
        GlobalResponseDTO<Map<String,Object>> response=client.get().uri("/api/academic/internal/admissions/{id}",id).retrieve().body(new ParameterizedTypeReference<>(){});
        if(response==null || response.data()==null || !Boolean.TRUE.equals(response.data().get("tuitionPaid")) || "CANCELLED".equals(response.data().get("status")))
            throw new IllegalStateException("등록금 완납이 확인되지 않았습니다.");
    }
    public com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO provision(com.msa4lmsv2auth.domain.account.request.StudentProvisioningRequestDTO request) {
        GlobalResponseDTO<com.msa4lmsv2auth.domain.account.response.StudentProvisioningResponseDTO> r=client.post()
                .uri("/api/academic/internal/admissions/{id}/student",request.admissionCandidateId()).body(request).retrieve().body(new ParameterizedTypeReference<>(){});
        if(r==null || r.data()==null)throw new IllegalStateException("학번 응답 없음");return r.data();
    }
    public void activated(Long id,Long accountId) {
        client.post().uri("/api/academic/internal/admissions/{id}/activated",id).body(Map.of("accountId",accountId)).retrieve().toBodilessEntity();
    }
}

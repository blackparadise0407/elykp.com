package com.elykp.api.shared;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

@Service
public class SharedService {
    @Value("${web.authority}")
    private String authority;

    @Value("${web.client-id}")
    private String clientId;

    @Value("${web.client-secret}")
    private String clientSecret;

    @Value("${web.authority-api-url}")
    private String authorityApiUrl;

    private final RestTemplate restTemplate;

    public SharedService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<String> requestAccessTokenFromIS() throws Unauthorized {
        String url = authority + "/protocol/openid-connect/token";

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(map, httpHeaders);
        ResponseEntity<AccessTokenExchangeResponse> response = restTemplate.exchange(url,
                HttpMethod.POST,
                body,
                AccessTokenExchangeResponse.class);

        return Optional.ofNullable(response.getBody()).map(AccessTokenExchangeResponse::getAccess_token);
    }

    @Nullable
    public KeycloakUserResponse getKcUserById(String accessToken, String userId) throws Unauthorized {
        String url = authorityApiUrl + "/users/" + userId;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<KeycloakUserResponse> response = restTemplate.exchange(url, HttpMethod.GET, body,
                KeycloakUserResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        return null;
    }

}

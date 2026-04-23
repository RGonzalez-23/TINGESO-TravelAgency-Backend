package com.example.TINGESO.Services;

import com.example.TINGESO.DTOs.IamProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KeycloakAdminService {

    private final RestClient restClient;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    public KeycloakAdminService() {
        this.restClient = RestClient.builder().build();
    }

    public Map<String, Object> getCurrentUserProfile(String userId) {
        return getUserById(userId);
    }

    public Map<String, Object> updateOwnProfile(String userId, IamProfileUpdateRequest req) {
        Map<String, Object> current = getUserById(userId);
        return updateUser(current, req, false);
    }

    public Map<String, Object> adminUpdateUser(String userId, IamProfileUpdateRequest req) {
        Map<String, Object> current = getUserById(userId);
        return updateUser(current, req, true);
    }

    public void deactivateUser(String userId) {
        Map<String, Object> current = getUserById(userId);

        current.put("enabled", false);

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) current.get("attributes");
        if (attributes == null) {
            attributes = new HashMap<>();
            current.put("attributes", attributes);
        }
        attributes.put("accountActive", List.of("0"));

        saveUserRepresentation(userId, current);
    }

    private Map<String, Object> updateUser(Map<String, Object> current, IamProfileUpdateRequest req, boolean isAdminUpdate) {
        String userId = Objects.toString(current.get("id"), "");

        if (req.getNombres() != null && !req.getNombres().isBlank()) {
            current.put("firstName", req.getNombres().trim());
        }

        String apellidoPaterno = req.getApellidoPaterno();
        String apellidoMaterno = req.getApellidoMaterno();
        String mergedLastName = mergeLastName(current.get("lastName"), apellidoPaterno, apellidoMaterno);
        current.put("lastName", mergedLastName);

        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            current.put("email", req.getEmail().trim());
            current.put("username", req.getEmail().trim());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) current.get("attributes");
        if (attributes == null) {
            attributes = new HashMap<>();
            current.put("attributes", attributes);
        }

        if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
            attributes.put("apellidoPaterno", List.of(apellidoPaterno.trim()));
        }
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            attributes.put("apellidoMaterno", List.of(apellidoMaterno.trim()));
        }

        if (req.getPhone() != null) {
            attributes.put("phone", List.of(req.getPhone().trim()));
        }
        if (req.getRut() != null) {
            attributes.put("rut", List.of(req.getRut().trim()));
        }
        if (req.getNacionalidad() != null) {
            attributes.put("nacionalidad", List.of(req.getNacionalidad().trim()));
        }

        if (isAdminUpdate && req.getActive() != null) {
            current.put("enabled", req.getActive());
            attributes.put("accountActive", List.of(req.getActive() ? "1" : "0"));
        }

        saveUserRepresentation(userId, current);
        return getUserById(userId);
    }

    private String mergeLastName(Object currentLastName, String apellidoPaterno, String apellidoMaterno) {
        String existing = currentLastName == null ? "" : currentLastName.toString();
        String[] pieces = existing.trim().split("\\s+");
        String currentPaterno = pieces.length > 0 ? pieces[0] : "";
        String currentMaterno = pieces.length > 1 ? pieces[1] : "";

        String nextPaterno = (apellidoPaterno != null && !apellidoPaterno.isBlank()) ? apellidoPaterno.trim() : currentPaterno;
        String nextMaterno = (apellidoMaterno != null && !apellidoMaterno.isBlank()) ? apellidoMaterno.trim() : currentMaterno;

        return (nextPaterno + " " + nextMaterno).trim();
    }


    private Map<String, Object> getUserById(String userId) {
        String adminToken = getAdminAccessToken();
        Map<String, Object> data = restClient.get()
                .uri(adminBaseUrl() + "/users/" + userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(Map.class);

        if (data == null) {
            throw new IllegalStateException("Usuario no encontrado en Keycloak");
        }

        return data;
    }

    private void saveUserRepresentation(String userId, Map<String, Object> representation) {
        String adminToken = getAdminAccessToken();
        restClient.put()
                .uri(adminBaseUrl() + "/users/" + userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(representation)
                .retrieve()
                .toBodilessEntity();
    }

    private String getAdminAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", adminClientId);
        form.add("client_secret", adminClientSecret);

        @SuppressWarnings("unchecked")
        Map<String, Object> tokenResponse = restClient.post()
                .uri(tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new IllegalStateException("No fue posible obtener token de admin para Keycloak");
        }

        return tokenResponse.get("access_token").toString();
    }

    private String tokenUrl() {
        return UriComponentsBuilder
                .fromUriString(authServerUrl)
                .pathSegment("realms", realm, "protocol", "openid-connect", "token")
                .build()
                .toUriString();
    }

    private String adminBaseUrl() {
        return UriComponentsBuilder
                .fromUriString(authServerUrl)
                .pathSegment("admin", "realms", realm)
                .build()
                .toUriString();
    }

}

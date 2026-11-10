package com.hato.api.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierService.class);
    private final GoogleIdTokenVerifier verifier;
    private final String clientId;

    public GoogleTokenVerifierService(@Value("${hato.google.client-id}") String clientId) {
        this.clientId = clientId;
        GoogleIdTokenVerifier v = null;
        try {
            v = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            log.warn("No se pudo inicializar GoogleIdTokenVerifier en línea: {}", e.getMessage());
        }
        this.verifier = v;
    }

    public GoogleUserProfile verify(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new BadCredentialsException("Token de Google vacío");
        }

        // Si es un token demo o de desarrollo
        if (idTokenString.startsWith("mock-jwt-google") || verifier == null) {
            return new GoogleUserProfile(
                    "google-usr-dev",
                    "ganadero.google@hato.com",
                    "Ganadero Google Verificado",
                    null
            );
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                // Si la audiencia no coincide pero el token viene en dev/test
                throw new BadCredentialsException("Token de Google inválido o expirado");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            return new GoogleUserProfile(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (GeneralSecurityException | IOException e) {
            log.error("Fallo al verificar token de Google", e);
            throw new BadCredentialsException("Fallo en la validación criptográfica de Google: " + e.getMessage());
        }
    }

    public record GoogleUserProfile(String sub, String email, String name, String pictureUrl) {}
}

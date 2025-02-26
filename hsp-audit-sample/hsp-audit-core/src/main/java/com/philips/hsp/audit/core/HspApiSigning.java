package com.philips.hsp.audit.core;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HspApiSigning {
    private static final String API_SIGNATURE_FORMAT = "HmacSHA256;Credential:%s;SignedHeaders:SignedDate;Signature:%s";
    private static final String SECRET_KEY_PREFIX = "DHPWS";
    AuditProperties properties;

    @Inject
    public HspApiSigning(AuditProperties auditProperties) {
        this.properties = auditProperties;
    }
    public String getSignature(String signedDate) {
        return createApiSignature(signedDate,
                properties.getSharedKey(),
                properties.getSecretKey(),
                properties.getAlgorithm());
    }

    private String createApiSignature(final String signedDate, String sharedKey, String secretKey, String algorithm) {
        return String.format(API_SIGNATURE_FORMAT, sharedKey,
                createSignature(signedDate, algorithm, secretKey));
    }

    private String createSignature(final String signedDateHeader, String algorithm, String secretKey) {
        try {
            final Mac sha256HMAC = Mac.getInstance(algorithm);
            final SecretKeySpec keySpec = getSecretKeySpec(algorithm, secretKey);
            sha256HMAC.init(keySpec);
            final byte[] signature = sha256HMAC
                    .doFinal(Base64.getEncoder().encode(signedDateHeader.getBytes(StandardCharsets.UTF_8)));
            return new String(Base64.getEncoder().encode(signature), StandardCharsets.UTF_8);
        } catch (final Exception ex) {
            throw new HspAuditException("Failed to create API signature", ex);//NOSONAR
        }
    }

    private SecretKeySpec getSecretKeySpec(String algorithm, String secretKey) {
        String keyPrefix = SECRET_KEY_PREFIX + secretKey;
        final byte[] key = keyPrefix.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(key, algorithm);
    }
}

package org.server;
import com.google.gson.JsonObject;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.util.HashMap;

public class SecretManager {
    private final JsonObject secrets;
    public SecretManager() {
        String secretName = "apartment-hunter-secrets";
        Region region = Region.of("eu-west-1");

        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create("apartment-hunter"))
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (SecretsManagerException e) {

            throw e;
        }

        String secret = getSecretValueResponse.secretString();
        secrets = com.google.gson.JsonParser.parseString(secret).getAsJsonObject();

    }
    public String getSecret(String key) {
        if (secrets.has(key)) {
            return secrets.get(key).getAsString();
        } else {
            throw new IllegalArgumentException("Secret key not found: " + key);
        }
    }
}

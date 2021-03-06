package com.sequenceiq.cloudbreak.cloud.azure.task.interactivelogin;

import static com.sequenceiq.cloudbreak.cloud.azure.task.interactivelogin.AzureInteractiveLoginStatusCheckerTask.AZURE_MANAGEMENT;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.SubscriptionState;
import com.sequenceiq.cloudbreak.cloud.azure.AzureSubscription;

@Service
public class SubscriptionChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionChecker.class);

    public void checkSubscription(String subscriptionId, String accessToken) throws InteractiveLoginException {
        if (subscriptionId == null) {
            throw new InteractiveLoginException("Parameter subscriptionId is required and cannot be null.");
        }
        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(AZURE_MANAGEMENT);
        Invocation.Builder request = resource.path("/subscriptions/" + subscriptionId)
                .queryParam("api-version", "2016-06-01")
                .request();
        request.accept(MediaType.APPLICATION_JSON);

        request.header("Authorization", "Bearer " + accessToken);
        Response response = request.get();

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            AzureSubscription subscription = response.readEntity(AzureSubscription.class);
            if (!subscription.getState().equals(SubscriptionState.ENABLED)) {
                throw new InteractiveLoginException("Subscription specified in Profile is in incorrect state:" + "" + subscription.getState().toString());
            }
            LOGGER.debug("Subscription definitions successfully retrieved:" + subscription.getDisplayName());
        } else {
            String errorResponse = response.readEntity(String.class);
            try {
                String errorMessage = new ObjectMapper().readTree(errorResponse).get("error").get("message").asText();
                LOGGER.error("Subscription retrieve error:" + errorMessage);
                throw new InteractiveLoginException("Error with the subscription specified in Profile id: " + subscriptionId + " message: " + errorMessage);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}

package bytes.sync.jobs.restutil;

import bytes.sync.entity.ActivationExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.net.URISyntaxException;

public class APICallHelper {

    private final Logger logger = LoggerFactory.getLogger(APICallHelper.class);
    private ActivationExpression activationExpression;

    public APICallHelper(ActivationExpression activationExpression) {
        this.activationExpression = activationExpression;
    }

    public RestOutputWrapper executeRestCall() {
        RestOutputWrapper outputWrapper = new RestOutputWrapper();
        ResponseEntity responseEntity = null;
        long startTime = System.currentTimeMillis();
        switch (activationExpression.getMethod().toUpperCase()) {
            case "GET": responseEntity = getCall(activationExpression); break;
            case "POST": responseEntity = postCall(activationExpression); break;
            case "PUT": responseEntity = putCall(activationExpression); break;
            case "DELETE": responseEntity = deleteCall(activationExpression); break;
            default: logger.debug("Not a matching HTTP method: {}", activationExpression.getMethod());
        }

        long timeToExecuteInMillis = System.currentTimeMillis() - startTime;

        if(responseEntity != null) {
            String resBody = responseEntity.getBody() != null ? responseEntity.getBody().toString() : "";
            outputWrapper.setResponseMessage(resBody);
            outputWrapper.setTimeToExecuteInMillis(timeToExecuteInMillis);
            outputWrapper.setHttpStatus(responseEntity.getStatusCodeValue());
            logger.debug("Response from APICall, Status: {}, Body: {}", responseEntity.getStatusCode(), resBody);
        } else {
            logger.debug("Null ResponseEntity returned");
        }

        return outputWrapper;
    }

    private ResponseEntity getCall(ActivationExpression activationExpression) {
        try {
            RequestEntity requestEntity = RequestEntity
                    .get(new URI(activationExpression.getUrl()))
                    .accept(MediaType.APPLICATION_JSON)
                    .build();
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.exchange(requestEntity, String.class);
        } catch (URISyntaxException e){
            logger.error("error parsing url: {}", e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            logger.error("error accessing resource: {}", e.getMessage());
            return null;
        }
    }

    private ResponseEntity postCall(ActivationExpression activationExpression) {
        try {
            RequestEntity requestEntity = RequestEntity
                    .post(new URI(activationExpression.getUrl()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(activationExpression.getPayload());
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.exchange(requestEntity, String.class);
        } catch (URISyntaxException e){
            logger.error("error parsing url: {}", e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            logger.error("error accessing resource: {}", e.getMessage());
            return null;
        }
    }

    private ResponseEntity putCall(ActivationExpression activationExpression) {
        try {
            RequestEntity requestEntity = RequestEntity
                    .put(new URI(activationExpression.getUrl()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(activationExpression.getPayload());
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.exchange(requestEntity, String.class);
        } catch (URISyntaxException e){
            logger.error("error parsing url: {}", e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            logger.error("error accessing resource: {}", e.getMessage());
            return null;
        }
    }

    private ResponseEntity deleteCall(ActivationExpression activationExpression) {
        try {
            RequestEntity requestEntity = RequestEntity
                    .delete(new URI(activationExpression.getUrl()))
                    .build();
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.exchange(requestEntity, String.class);
        } catch (URISyntaxException e){
            logger.error("error parsing url: {}", e.getMessage());
            return null;
        } catch (ResourceAccessException e) {
            logger.error("error accessing resource: {}", e.getMessage());
            return null;
        }
    }

}

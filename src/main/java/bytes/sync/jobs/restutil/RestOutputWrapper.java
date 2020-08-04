package bytes.sync.jobs.restutil;

import org.springframework.stereotype.Component;

@Component
public class RestOutputWrapper {

    private String responseMessage;
    private Integer httpStatus;
    private Long timeToExecuteInMillis;

    public Long getTimeToExecuteInMillis() {
        return timeToExecuteInMillis;
    }

    public void setTimeToExecuteInMillis(Long timeToExecuteInMillis) {
        this.timeToExecuteInMillis = timeToExecuteInMillis;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String toString() {
        return "Status: " + httpStatus + ", TimeTakenToExecute: " + timeToExecuteInMillis + "ms, ResponseMessage: " + responseMessage;
    }
}

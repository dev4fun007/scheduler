package bytes.sync.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;


@Entity
@Table(name = "ScheduledExecutionLogs")
public class ScheduledExecutionInfo {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String executionId;

    private String scheduleWrapperId;
    private Timestamp executionTimestamp;
    private Long timeToExecuteInMillis;
    private Integer responseHttpStatus;

    @Column(columnDefinition="text")
    private String responseMessage;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getScheduleWrapperId() {
        return scheduleWrapperId;
    }

    public void setScheduleWrapperId(String scheduleWrapperId) {
        this.scheduleWrapperId = scheduleWrapperId;
    }

    public Timestamp getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(Timestamp executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public Long getTimeToExecuteInMillis() {
        return timeToExecuteInMillis;
    }

    public void setTimeToExecuteInMillis(Long timeToExecuteInMillis) {
        this.timeToExecuteInMillis = timeToExecuteInMillis;
    }

    public Integer getResponseHttpStatus() {
        return responseHttpStatus;
    }

    public void setResponseHttpStatus(Integer responseHttpStatus) {
        this.responseHttpStatus = responseHttpStatus;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}

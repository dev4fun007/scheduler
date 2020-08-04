package bytes.sync.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;


@Entity
@Table(name = "ScheduledObject")
public class SchedulerWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String jobClass;
    private String jobGroup;
    private String jobName;
    private String triggerExpression;
    private String cronExpression;
    private Date startTime;
    private String addedBy;
    private Date addedOn;
    private Boolean active;
    @Embedded
    private ActivationExpression activationExpression;
    private String rowSecurity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTriggerExpression() {
        return triggerExpression;
    }

    public void setTriggerExpression(String triggerExpression) {
        this.triggerExpression = triggerExpression;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Date getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Date addedOn) {
        this.addedOn = addedOn;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ActivationExpression getActivationExpression() {
        return activationExpression;
    }

    public void setActivationExpression(ActivationExpression activationExpression) {
        this.activationExpression = activationExpression;
    }

    public String getRowSecurity() {
        return rowSecurity;
    }

    public void setRowSecurity(String rowSecurity) {
        this.rowSecurity = rowSecurity;
    }
}

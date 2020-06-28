package bytes.sync.model;

import java.util.Date;

public class SchedulerWrapper {

    String id;
    String jobClass;
    String jobGroup;
    String jobName;
    String triggerExpression;
    String cronExpression;
    String startTime;
    String addedBy;
    String addedOn;
    Boolean active;
    String activationExpression;
    String rowSecurity;

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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(String addedOn) {
        this.addedOn = addedOn;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getActivationExpression() {
        return activationExpression;
    }

    public void setActivationExpression(String activationExpression) {
        this.activationExpression = activationExpression;
    }

    public String getRowSecurity() {
        return rowSecurity;
    }

    public void setRowSecurity(String rowSecurity) {
        this.rowSecurity = rowSecurity;
    }
}

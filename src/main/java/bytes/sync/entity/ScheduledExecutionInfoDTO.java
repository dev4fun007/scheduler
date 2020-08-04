package bytes.sync.entity;

import java.util.List;

public class ScheduledExecutionInfoDTO {

    private long count;
    private List<ScheduledExecutionInfo> scheduledExecutionInfoList;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<ScheduledExecutionInfo> getScheduledExecutionInfoList() {
        return scheduledExecutionInfoList;
    }

    public void setScheduledExecutionInfoList(List<ScheduledExecutionInfo> scheduledExecutionInfoList) {
        this.scheduledExecutionInfoList = scheduledExecutionInfoList;
    }
}

package ch.ubp.pms.entities.aaamaindb;

import java.util.stream.Stream;

public enum SubscriptionEventStatus {

    UNTREATED(0L, "Untreated"),
    SUCCEED(1L, "Succeed"),
    FAILED(2L, "Failed"),
    RUNNING(3L, "Running"),
    DEFERRED(4L, "Deferred"),
    DISCARDED(5L, "Discarded");
 
    private long id;
    private String code;
 
    SubscriptionEventStatus(long id, String code) {
        this.id = id;
        this.code = code;
    }
    
    public long getId() { return id; }

    public String getCode() { return code; }

    public static SubscriptionEventStatus of(long status) {
        return Stream.of(SubscriptionEventStatus.values())
                .filter(s -> s.getId() == status)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
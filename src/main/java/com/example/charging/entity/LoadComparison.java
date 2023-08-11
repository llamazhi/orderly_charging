package com.example.charging.entity;

import java.math.BigDecimal;

// 此类存有一天内优化前后用电负荷对比
public class LoadComparison {
    private String uid;
    private BigDecimal time;
    private BigDecimal oldLoad;
    private BigDecimal newLoad;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public BigDecimal getTime() {
        return time;
    }

    public void setTime(BigDecimal time) {
        this.time = time;
    }

    public BigDecimal getOldLoad() {
        return oldLoad;
    }

    public void setOldLoad(BigDecimal oldLoad) {
        this.oldLoad = oldLoad;
    }

    public BigDecimal getNewLoad() {
        return newLoad;
    }

    public void setNewLoad(BigDecimal newLoad) {
        this.newLoad = newLoad;
    }
}

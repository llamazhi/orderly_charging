package com.example.charging.entity;

import java.math.BigDecimal;

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

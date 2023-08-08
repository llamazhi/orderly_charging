package com.example.charging.entity;

import java.math.BigDecimal;
import java.util.List;

public class LoadComparison {
    private String id;
    private BigDecimal time;
    private BigDecimal oldLoad;
    private BigDecimal newLoad;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

package com.taiji.opcuabackend.entity;

import java.util.Date;

public class DailyValue {
    private static final long serialVersionUID = 1L;
    private Date dateKey;

    private Long statusCount;

    public Date getDateKey() {
        return dateKey;
    }

    public void setDateKey(Date dateKey) {
        this.dateKey = dateKey;
    }

    public Long getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(Long statusCount) {
        this.statusCount = statusCount;
    }
}
package com.example.library.model;

import java.util.Date;

public class Blacklist {
    private Long id;
    private Long userId;
    private String reason;
    private Integer overdueBooksCount;
    private Integer totalOverdueDays;
    private Date createTime;
    private Date updateTime;
    private Integer isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getOverdueBooksCount() {
        return overdueBooksCount;
    }

    public void setOverdueBooksCount(Integer overdueBooksCount) {
        this.overdueBooksCount = overdueBooksCount;
    }

    public Integer getTotalOverdueDays() {
        return totalOverdueDays;
    }

    public void setTotalOverdueDays(Integer totalOverdueDays) {
        this.totalOverdueDays = totalOverdueDays;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "Blacklist{" +
                "id=" + id +
                ", userId=" + userId +
                ", reason='" + reason + '\'' +
                ", overdueBooksCount=" + overdueBooksCount +
                ", totalOverdueDays=" + totalOverdueDays +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isActive=" + isActive +
                '}';
    }
}

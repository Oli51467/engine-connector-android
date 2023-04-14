package com.irlab.view.entity;

import java.io.Serializable;

public class GameInfo implements Serializable {

    Long id;

    Long blackUserid;

    Long whiteUserid;

    String blackUsername;

    String whiteUsername;

    String result;

    String createTime;

    public GameInfo(Long id, Long blackUserid, Long whiteUserid, String blackUsername, String whiteUsername, String result, String createTime) {
        this.id = id;
        this.blackUserid = blackUserid;
        this.whiteUserid = whiteUserid;
        this.blackUsername = blackUsername;
        this.whiteUsername = whiteUsername;
        this.result = result;
        this.createTime = createTime;
    }

    public String getRecordDetail() {
        return "黑方: " + this.blackUsername + "  " + "白方: " + this.whiteUsername;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBlackUserid() {
        return blackUserid;
    }

    public void setBlackUserid(Long blackUserid) {
        this.blackUserid = blackUserid;
    }

    public Long getWhiteUserid() {
        return whiteUserid;
    }

    public void setWhiteUserid(Long whiteUserid) {
        this.whiteUserid = whiteUserid;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}

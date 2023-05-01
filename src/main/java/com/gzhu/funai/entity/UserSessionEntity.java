package com.gzhu.funai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user_session")
public class UserSessionEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonProperty("session_id")
    private Integer sessionId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_name")
    private String sessionName;

    @JsonProperty("create_time")
    private Date createTime;

    @JsonProperty("update_time")
    private Date updateTime;

    // 聊天的类型，区分普通聊天、pdf聊天、冒险游戏聊天
    @JsonProperty("type")
    private Integer type;

    private static final long serialVersionUID = 1L;

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName == null ? null : sessionName.trim();
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public UserSessionEntity(String userId, String sessionName, Integer type){
        this.userId = userId;
        this.sessionName = sessionName;
        this.type = type;
    }

    public UserSessionEntity(Integer sessionId, String sessionName){
        this.sessionId = sessionId;
        this.sessionName = sessionName;
    }
}
package com.gzhu.funai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "session_chat_record")
@ToString
public class SessionChatRecordEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonProperty("session_chat_id")
    private Integer sessionChatId;

    @JsonProperty("session_id")
    private Integer sessionId;

    private String role;

    @JsonProperty("token_num")
    private Integer tokenNum;

    @JsonProperty("create_time")
    private Date createTime;

    @JsonProperty("update_time")
    private Date updateTime;

    private String content;

    private static final long serialVersionUID = 1L;

    public Integer getSessionChatId() {
        return sessionChatId;
    }

    public void setSessionChatId(Integer sessionChatId) {
        this.sessionChatId = sessionChatId;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role == null ? null : role.trim();
    }

    public Integer getTokenNum() {
        return tokenNum;
    }

    public void setTokenNum(Integer tokenNum) {
        this.tokenNum = tokenNum;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public SessionChatRecordEntity(Integer sessionId, String role, String content, Integer tokenNum){
        this.sessionId =sessionId;
        this.role = role;
        this.content = content;
        this.tokenNum = tokenNum;
    }

    public SessionChatRecordEntity(String role, String content){
        this.role = role;
        this.content = content;
    }
}
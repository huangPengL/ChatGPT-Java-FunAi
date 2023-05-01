package com.gzhu.funai.entity;

import java.util.List;

/**
 * @author zxw
 * @Desriiption: 向量库实体
 */
public class DataSqlEntity {

    /**
     * 分段后的每一段的向量
     */
    private List<List<Float>> ll;

    /**
     *  每一段的内容
     */
    private List<String> content;

    /**
     *  总共token数量
     */
    private Integer total_token;

    public DataSqlEntity(){
    }

    public List<List<Float>> getLl(){
        return this.ll;
    }

    public void setLl(List<List<Float>> ll){
        this.ll = ll;
    }

    public Integer getTotal_token() {
        return this.total_token;
    }

    public void setTotal_token(Integer total_token) {
        this.total_token = total_token;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}

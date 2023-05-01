package com.gzhu.funai.api.openai.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/28 8:55
 */
@Data
public class Grants {
    private String object;
    @JsonProperty("data")
    private List<Datum> data;

    public Datum getActualData(){
        if(CollectionUtils.isEmpty(data)){
           return null;
        }
        return data.get(0);
    }
}

package com.gzhu.funai.api.openai.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @Author: huangpenglong
 * @Date: 2023/3/28 8:55
 */

@Data
@ToString
public class CreditGrantsResp {
    private String object;
    /**
     * 总金额：美元
     */
    @JsonProperty("total_granted")
    private BigDecimal totalGranted;
    /**
     * 总使用金额：美元
     */
    @JsonProperty("total_used")
    private BigDecimal totalUsed;
    /**
     * 总剩余金额：美元
     */
    @JsonProperty("total_available")
    private BigDecimal totalAvailable;
    /**
     * 余额明细
     */
    private Grants grants;
}

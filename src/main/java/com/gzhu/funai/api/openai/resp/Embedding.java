package com.gzhu.funai.api.openai.resp;

import lombok.Data;

import java.util.List;

/**
 * @author zxw
 * @Desriiption:
 */
@Data
public class Embedding {

    String object;

    List<Float> embedding;

    Integer index;
}

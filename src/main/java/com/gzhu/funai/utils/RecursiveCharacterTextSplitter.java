package com.gzhu.funai.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zxw
 * @Desriiption: 递归分词器
 */
public class RecursiveCharacterTextSplitter {
    private List<String> separators;
    private int chunkSize = 500;
    private int chunkOverlap = 50;

    // 构造函数，接受分隔符列表、块大小和块重叠作为参数
    public RecursiveCharacterTextSplitter(List<String> separators, int chunkSize, int chunkOverlap) {
        // 如果分隔符列表为null，则使用默认值
        if (separators == null) {
            this.separators = Arrays.asList("\n\n", "\n", " ", "");
        } else {
            this.separators = separators;
        }
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    // 将文本分割成块的方法
    public List<String> splitText(String text) {
        // 声明一个空的字符串列表，用于存储最终的文本块
        List<String> finalChunks = new ArrayList<>();
        String separator = separators.get(separators.size() - 1);

        // 循环遍历分隔符列表，找到可以在文本中找到的最合适的分隔符
        for (String s : separators) {
            if (text.contains(s) || s.isEmpty()) {
                separator = s;
                break;
            }
        }

        List<String> splits = Arrays.asList(text.split(separator));

        // 声明一个空的字符串列表，用于存储长度小于块大小的子字符串
        List<String> goodSplits = new ArrayList<>();
        // 循环遍历子字符串列表，将较短的子字符串添加到goodSplits列表中，将较长的子字符串递归地传递给splitText方法
        for (String s : splits) {
            if (s.length() < chunkSize) {
                goodSplits.add(s);
            } else {
                if (!goodSplits.isEmpty()) {
                    // 将goodSplits列表中的子字符串合并为一个文本块，并将其添加到最终的文本块列表中
                    List<String> mergedText = mergeSplits(goodSplits, separator);
                    finalChunks.addAll(mergedText);
                    goodSplits.clear();
                }
                // 递归地将较长的子字符串传递给splitText方法
                List<String> otherInfo = splitText(s);
                finalChunks.addAll(otherInfo);
            }
        }

        if (!goodSplits.isEmpty()) {
            List<String> mergedText = mergeSplits(goodSplits, separator);
            finalChunks.addAll(mergedText);
        }

        return finalChunks;
    }

    private List<String> mergeSplits(List<String> splits, String separator) {
        int separatorLen = separator.length();

        List<String> docs = new ArrayList<>();
        List<String> currentDoc = new ArrayList<>();
        int total = 0;

        for (String d : splits) {
            int len = d.length();
            if (total + len + (separatorLen > 0 && !currentDoc.isEmpty() ? separatorLen : 0) > chunkSize) {
                if (total > chunkSize) {
                    System.out.println("Warning: Created a chunk of size " + total + ", which is longer than the specified " + chunkSize);
                }
                if (!currentDoc.isEmpty()) {
                    String doc = joinDocs(currentDoc, separator);
                    if (doc != null) {
                        docs.add(doc);
                    }
                    // 通过移除currentDoc中的文档，将currentDoc的长度减小到指定的文档重叠长度chunkOverlap或更小, 结果存到下一个chunk的开始位置
                    while (total > chunkOverlap || (total + len + (separatorLen > 0 && !currentDoc.isEmpty() ? separatorLen : 0) > chunkSize && total > 0)) {
                        total -= currentDoc.get(0).length() + (separatorLen > 0 && currentDoc.size() > 1 ? separatorLen : 0);
                        currentDoc.remove(0);
                    }
                }
            }
            currentDoc.add(d);
            total += len + (separatorLen > 0 && currentDoc.size() > 1 ? separatorLen : 0);
        }

        String doc = joinDocs(currentDoc, separator);
        if (doc != null) {
            docs.add(doc);
        }

        return docs;
    }

    private String joinDocs(List<String> docs, String separator) {
        if (docs.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            sb.append(docs.get(i));
            if (i < docs.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}
package com.deepseek.chat.api;

import com.deepseek.chat.core.model.ChatMessage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * DeepSeek API 客户端
 * 负责调用 DeepSeek API 进行对话
 */
public class DeepSeekApiClient {
    
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";
    
    private final String apiKey;
    
    public DeepSeekApiClient(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * 调用 DeepSeek API 发送消息并获取回复
     * @param systemPrompt 系统提示词
     * @param history 历史消息列表
     * @return AI 的回复内容
     * @throws Exception 如果请求失败
     */
    public String chat(String systemPrompt, List<ChatMessage> history) throws Exception {
        // 构建请求体
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{\"model\":\"").append(MODEL).append("\",\"messages\":[");
        
        // 添加 system prompt
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            requestBody.append("{\"role\":\"system\",\"content\":\"")
                    .append(escapeJson(systemPrompt))
                    .append("\"},");
        }
        
        // 添加历史消息
        for (int i = 0; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            requestBody.append("{\"role\":\"")
                    .append(msg.getRole())
                    .append("\",\"content\":\"")
                    .append(escapeJson(msg.getContent()))
                    .append("\"}");
            if (i < history.size() - 1) {
                requestBody.append(",");
            }
        }
        
        requestBody.append("]}");
        
        // 发送 HTTP 请求
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("API 请求失败，状态码：" + responseCode);
        }
        
        // 读取响应
        StringBuilder responseJson = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseJson.append(line);
            }
        }
        
        // 解析 JSON 响应
        return extractContentFromJson(responseJson.toString());
    }
    
    /**
     * 从 JSON 响应中提取 content 字段
     */
    private String extractContentFromJson(String json) {
        // 简单提取 content 字段，不使用外部 JSON 库
        int contentIndex = json.indexOf("\"content\":");
        if (contentIndex == -1) {
            return "";
        }
        
        int startIndex = json.indexOf("\"", contentIndex + 10);
        if (startIndex == -1) {
            return "";
        }
        startIndex++;
        
        int endIndex = startIndex;
        while (endIndex < json.length()) {
            char c = json.charAt(endIndex);
            if (c == '"' && json.charAt(endIndex - 1) != '\\') {
                break;
            }
            endIndex++;
        }
        
        String content = json.substring(startIndex, endIndex);
        return unescapeJson(content);
    }
    
    /**
     * 转义 JSON 特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * 反转义 JSON 特殊字符
     */
    private String unescapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}

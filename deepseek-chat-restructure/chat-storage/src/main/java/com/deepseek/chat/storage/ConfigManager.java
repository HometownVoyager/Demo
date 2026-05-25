package com.deepseek.chat.storage;

import com.deepseek.chat.core.model.ChatMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 配置和历史记录管理器
 * 负责 API Key、System Prompt 和聊天历史的持久化
 */
public class ConfigManager {
    
    private static final String CONFIG_FILE = "deepseek_config.properties";
    private static final String HISTORY_FILE = "chat_history.txt";
    
    /**
     * 加载配置
     * @return 配置属性
     */
    public Properties loadConfig() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("加载配置文件失败：" + e.getMessage());
            }
        }
        return props;
    }
    
    /**
     * 保存配置
     * @param apiKey API Key
     * @param systemPrompt 系统提示词
     */
    public void saveConfig(String apiKey, String systemPrompt) {
        Properties props = new Properties();
        props.setProperty("api.key", apiKey);
        props.setProperty("system.prompt", systemPrompt);
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "DeepSeek Chat Config");
        } catch (IOException e) {
            System.err.println("保存配置文件失败：" + e.getMessage());
        }
    }
    
    /**
     * 加载聊天历史
     * @return 历史消息列表
     */
    public List<ChatMessage> loadHistory() {
        List<ChatMessage> history = new ArrayList<>();
        File historyFile = new File(HISTORY_FILE);
        if (!historyFile.exists()) {
            return history;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            StringBuilder currentMessage = new StringBuilder();
            String role = null;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ROLE:")) {
                    if (role != null && currentMessage.length() > 0) {
                        history.add(new ChatMessage(role, currentMessage.toString().trim()));
                    }
                    role = line.substring(5).trim();
                    currentMessage = new StringBuilder();
                } else if (line.startsWith("CONTENT:")) {
                    currentMessage.append(line.substring(8)).append("\n");
                }
            }
            
            // 添加最后一条消息
            if (role != null && currentMessage.length() > 0) {
                history.add(new ChatMessage(role, currentMessage.toString().trim()));
            }
        } catch (IOException e) {
            System.err.println("加载历史记录失败：" + e.getMessage());
        }
        
        return history;
    }
    
    /**
     * 保存聊天历史
     * @param history 历史消息列表
     */
    public void saveHistory(List<ChatMessage> history) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            for (ChatMessage msg : history) {
                writer.println("ROLE:" + msg.getRole());
                writer.println("CONTENT:" + msg.getContent());
                writer.println();
            }
        } catch (IOException e) {
            System.err.println("保存历史记录失败：" + e.getMessage());
        }
    }
}

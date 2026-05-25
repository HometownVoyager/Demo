package com.deepseek.chat.storage;

import com.deepseek.chat.core.model.CharacterCard;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色卡管理器
 * 负责角色卡的持久化存储和加载
 */
public class CharacterStorage {
    
    private static final String CHARACTER_FILE = "characters.json";
    
    /**
     * 加载所有角色卡
     * @return 角色卡列表
     */
    public List<CharacterCard> loadCharacters() {
        List<CharacterCard> characters = new ArrayList<>();
        File file = new File(CHARACTER_FILE);
        if (!file.exists()) {
            return characters;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            characters = parseCharacters(content.toString());
        } catch (IOException e) {
            System.err.println("加载角色卡失败：" + e.getMessage());
        }
        
        return characters;
    }
    
    /**
     * 保存所有角色卡
     * @param characters 角色卡列表
     */
    public void saveCharacters(List<CharacterCard> characters) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CHARACTER_FILE))) {
            writer.println("[");
            for (int i = 0; i < characters.size(); i++) {
                CharacterCard cc = characters.get(i);
                writer.println("  {");
                writer.println("    \"name\": \"" + escapeJson(cc.getName()) + "\",");
                writer.println("    \"description\": \"" + escapeJson(cc.getDescription()) + "\",");
                writer.println("    \"personality\": \"" + escapeJson(cc.getPersonality()) + "\",");
                writer.println("    \"greeting\": \"" + escapeJson(cc.getGreeting()) + "\"");
                writer.print("  }");
                if (i < characters.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
        } catch (IOException e) {
            System.err.println("保存角色卡失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析角色卡 JSON
     */
    private List<CharacterCard> parseCharacters(String jsonLike) {
        List<CharacterCard> result = new ArrayList<>();
        
        // 简单解析，不使用外部 JSON 库
        String[] entries = jsonLike.split("\\},\\s*\\{");
        for (String entry : entries) {
            String name = extractField(entry, "\"name\":");
            String description = extractField(entry, "\"description\":");
            String personality = extractField(entry, "\"personality\":");
            String greeting = extractField(entry, "\"greeting\":");
            
            if (name != null && !name.isEmpty()) {
                result.add(new CharacterCard(name, description, personality, greeting));
            }
        }
        
        return result;
    }
    
    /**
     * 提取字段值
     */
    private String extractField(String text, String prefix) {
        int index = text.indexOf(prefix);
        if (index == -1) {
            return null;
        }
        
        int startIndex = text.indexOf("\"", index + prefix.length());
        if (startIndex == -1) {
            return null;
        }
        startIndex++;
        
        int endIndex = startIndex;
        while (endIndex < text.length()) {
            char c = text.charAt(endIndex);
            if (c == '"' && text.charAt(endIndex - 1) != '\\') {
                break;
            }
            endIndex++;
        }
        
        return text.substring(startIndex, endIndex);
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
}

package com.deepseek.chat.storage;

import com.deepseek.chat.core.model.WorldBook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 世界书管理器
 * 负责世界书的持久化存储和加载
 */
public class WorldBookStorage {
    
    private static final String WORLD_BOOK_FILE = "world_books.json";
    
    /**
     * 加载所有世界书
     * @return 世界书列表
     */
    public List<WorldBook> loadWorldBooks() {
        List<WorldBook> worldBooks = new ArrayList<>();
        File file = new File(WORLD_BOOK_FILE);
        if (!file.exists()) {
            return worldBooks;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            worldBooks = parseWorldBooks(content.toString());
        } catch (IOException e) {
            System.err.println("加载世界书失败：" + e.getMessage());
        }
        
        return worldBooks;
    }
    
    /**
     * 保存所有世界书
     * @param worldBooks 世界书列表
     */
    public void saveWorldBooks(List<WorldBook> worldBooks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(WORLD_BOOK_FILE))) {
            writer.println("[");
            for (int i = 0; i < worldBooks.size(); i++) {
                WorldBook wb = worldBooks.get(i);
                writer.println("  {");
                writer.println("    \"name\": \"" + escapeJson(wb.getName()) + "\",");
                writer.println("    \"content\": \"" + escapeJson(wb.getContent()) + "\"");
                writer.print("  }");
                if (i < worldBooks.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
        } catch (IOException e) {
            System.err.println("保存世界书失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析世界书 JSON
     */
    private List<WorldBook> parseWorldBooks(String jsonLike) {
        List<WorldBook> result = new ArrayList<>();
        
        // 简单解析，不使用外部 JSON 库
        String[] entries = jsonLike.split("\\},\\s*\\{");
        for (String entry : entries) {
            String name = extractField(entry, "\"name\":");
            String content = extractField(entry, "\"content\":");
            
            if (name != null && !name.isEmpty()) {
                result.add(new WorldBook(name, content));
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

package com.deepseek.chat.core.model;

/**
 * 世界书模型
 * 存储世界观、背景设定、特殊规则等内容
 */
public class WorldBook {
    private String name;
    private String content;

    public WorldBook(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "WorldBook{" +
                "name='" + name + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                '}';
    }
}

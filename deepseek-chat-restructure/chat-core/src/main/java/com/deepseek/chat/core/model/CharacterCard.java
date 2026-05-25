package com.deepseek.chat.core.model;

/**
 * 角色卡模型
 * 存储角色的名称、描述、性格、问候语和 API Key
 */
public class CharacterCard {
    private String name;
    private String description;
    private String personality;
    private String greeting;
    private String apiKey;
    private boolean isBackground; // 是否为背景角色（旁白/宏观调控者）

    public CharacterCard(String name, String description, String personality, String greeting) {
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.greeting = greeting;
        this.apiKey = "";
        this.isBackground = false;
    }

    public CharacterCard(String name, String description, String personality, String greeting, String apiKey) {
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.greeting = greeting;
        this.apiKey = apiKey != null ? apiKey : "";
        this.isBackground = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isBackground() {
        return isBackground;
    }

    public void setBackground(boolean background) {
        isBackground = background;
    }

    @Override
    public String toString() {
        return "CharacterCard{" +
                "name='" + name + '\'' +
                ", description='" + (description != null ? description.length() : 0) + " chars" +
                ", personality='" + (personality != null ? personality.length() : 0) + " chars" +
                ", apiKey='" + (apiKey != null && !apiKey.isEmpty() ? "***" : "empty") + '\'' +
                ", isBackground=" + isBackground +
                '}';
    }
}

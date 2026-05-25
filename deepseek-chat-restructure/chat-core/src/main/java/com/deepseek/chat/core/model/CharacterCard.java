package com.deepseek.chat.core.model;

/**
 * 角色卡模型
 * 存储角色的名称、描述、性格和问候语
 */
public class CharacterCard {
    private String name;
    private String description;
    private String personality;
    private String greeting;

    public CharacterCard(String name, String description, String personality, String greeting) {
        this.name = name;
        this.description = description;
        this.personality = personality;
        this.greeting = greeting;
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

    @Override
    public String toString() {
        return "CharacterCard{" +
                "name='" + name + '\'' +
                ", description='" + (description != null ? description.length() : 0) + " chars" +
                ", personality='" + (personality != null ? personality.length() : 0) + " chars" +
                '}';
    }
}

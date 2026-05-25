package com.deepseek.chat.ui.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 设置面板组件
 * 包含 API Key、System Prompt、世界书和角色卡选择等设置项
 */
public class SettingsPanel extends JPanel {
    
    private final JPasswordField apiKeyField;
    private final JTextArea systemPromptArea;
    private final JComboBox<String> activeWorldBookCombo;
    private final JComboBox<String> activeCharacterCombo;
    private final JButton saveSettingsButton;
    private final JButton clearButton;
    private final JButton worldBookButton;
    private final JButton characterButton;
    
    public SettingsPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("设置"));
        setBackground(new Color(230, 230, 230));
        
        apiKeyField = new JPasswordField(30);
        systemPromptArea = new JTextArea(3, 30);
        systemPromptArea.setLineWrap(true);
        systemPromptArea.setWrapStyleWord(true);
        systemPromptArea.setText("你是一个有帮助的 AI 助手。请用中文回答用户的问题。");
        
        activeWorldBookCombo = new JComboBox<>();
        activeWorldBookCombo.addItem("-- 无 --");
        
        activeCharacterCombo = new JComboBox<>();
        activeCharacterCombo.addItem("-- 无 --");
        
        saveSettingsButton = new JButton("保存设置");
        clearButton = new JButton("清空对话");
        worldBookButton = new JButton("管理世界书");
        characterButton = new JButton("管理角色卡");
        
        initUI();
    }
    
    private void initUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // API Key
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        add(new JLabel("API Key:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        add(apiKeyField, gbc);
        
        // System Prompt
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        add(new JLabel("System Prompt:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JScrollPane systemPromptScroll = new JScrollPane(systemPromptArea);
        systemPromptScroll.setPreferredSize(new Dimension(400, 60));
        add(systemPromptScroll, gbc);
        
        // 世界书选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        add(new JLabel("世界书:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        add(activeWorldBookCombo, gbc);
        
        // 角色卡选择
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        add(new JLabel("角色卡:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        add(activeCharacterCombo, gbc);
        
        // 按钮
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        add(saveSettingsButton, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(clearButton);
        buttonPanel.add(worldBookButton);
        buttonPanel.add(characterButton);
        add(buttonPanel, gbc);
    }
    
    // Getters
    public JPasswordField getApiKeyField() { return apiKeyField; }
    public JTextArea getSystemPromptArea() { return systemPromptArea; }
    public JComboBox<String> getActiveWorldBookCombo() { return activeWorldBookCombo; }
    public JComboBox<String> getActiveCharacterCombo() { return activeCharacterCombo; }
    public JButton getSaveSettingsButton() { return saveSettingsButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getWorldBookButton() { return worldBookButton; }
    public JButton getCharacterButton() { return characterButton; }
    
    // Setters for combo box items
    public void setWorldBookItems(String[] items) {
        activeWorldBookCombo.removeAllItems();
        activeWorldBookCombo.addItem("-- 无 --");
        for (String item : items) {
            activeWorldBookCombo.addItem(item);
        }
    }
    
    public void setCharacterItems(String[] items) {
        activeCharacterCombo.removeAllItems();
        activeCharacterCombo.addItem("-- 无 --");
        for (String item : items) {
            activeCharacterCombo.addItem(item);
        }
    }
    
    public String getSelectedWorldBook() {
        Object selected = activeWorldBookCombo.getSelectedItem();
        return selected != null ? selected.toString() : "";
    }
    
    public String getSelectedCharacter() {
        Object selected = activeCharacterCombo.getSelectedItem();
        return selected != null ? selected.toString() : "";
    }
}

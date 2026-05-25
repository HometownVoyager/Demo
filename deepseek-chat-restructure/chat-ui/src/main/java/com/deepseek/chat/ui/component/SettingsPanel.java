package com.deepseek.chat.ui.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 设置面板组件
 * 包含 API Key、System Prompt、世界书和角色卡选择等设置项
 */
public class SettingsPanel extends JPanel {
    
    private final JPasswordField apiKeyField;
    private final JTextArea systemPromptArea;
    private final JComboBox<String> activeWorldBookCombo;
    private final JPanel characterSelectionPanel;
    private final List<JCheckBox> characterCheckBoxes;
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
        
        characterCheckBoxes = new ArrayList<>();
        characterSelectionPanel = new JPanel();
        characterSelectionPanel.setLayout(new BoxLayout(characterSelectionPanel, BoxLayout.Y_AXIS));
        characterSelectionPanel.setBackground(new Color(230, 230, 230));
        
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
        
        // 角色卡选择（多选面板）
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        add(new JLabel("角色卡:"), gbc);
        
        gbc.gridx = 1;
        gbc.weighty = 1;
        JScrollPane characterScroll = new JScrollPane(characterSelectionPanel);
        characterScroll.setPreferredSize(new Dimension(400, 100));
        characterScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(characterScroll, gbc);
        
        // 按钮
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 0;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
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
    public JButton getSaveSettingsButton() { return saveSettingsButton; }
    public JButton getClearButton() { return clearButton; }
    public JButton getWorldBookButton() { return worldBookButton; }
    public JButton getCharacterButton() { return characterButton; }
    
    /**
     * 获取选中的角色卡名称列表
     */
    public List<String> getSelectedCharacters() {
        List<String> selected = new ArrayList<>();
        for (JCheckBox cb : characterCheckBoxes) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        return selected;
    }
    
    /**
     * 设置世界书下拉框选项
     */
    public void setWorldBookItems(String[] items) {
        activeWorldBookCombo.removeAllItems();
        activeWorldBookCombo.addItem("-- 无 --");
        for (String item : items) {
            activeWorldBookCombo.addItem(item);
        }
    }
    
    /**
     * 设置角色卡复选框列表
     */
    public void setCharacterItems(String[] items) {
        characterCheckBoxes.clear();
        characterSelectionPanel.removeAll();
        
        for (String item : items) {
            JCheckBox cb = new JCheckBox(item);
            characterCheckBoxes.add(cb);
            characterSelectionPanel.add(cb);
        }
        
        characterSelectionPanel.revalidate();
        characterSelectionPanel.repaint();
    }
    
    /**
     * 获取选中的世界书名称
     */
    public String getSelectedWorldBook() {
        Object selected = activeWorldBookCombo.getSelectedItem();
        return selected != null ? selected.toString() : "";
    }
}

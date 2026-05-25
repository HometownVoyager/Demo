package com.deepseek.chat.ui.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 输入面板组件
 * 包含消息输入框、发送按钮以及 Retry、编辑等功能按钮
 */
public class InputPanel extends JPanel {
    
    private final JTextField messageInputField;
    private final JButton sendButton;
    private final JButton retryButton;
    private final JButton editLastUserButton;
    private final JButton editLastAiButton;
    
    public InputPanel() {
        super(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(230, 230, 230));
        
        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        
        retryButton = new JButton("🔄 Retry");
        retryButton.setToolTipText("重新生成最后一条 AI 回复");
        retryButton.setEnabled(false);
        
        editLastUserButton = new JButton("✏️ 编辑最后输入");
        editLastUserButton.setToolTipText("编辑最后一条用户消息并重新发送");
        editLastUserButton.setEnabled(false);
        
        editLastAiButton = new JButton("✏️ 编辑最后回复");
        editLastAiButton.setToolTipText("编辑最后一条 AI 回复");
        editLastAiButton.setEnabled(false);
        
        initUI();
    }
    
    private void initUI() {
        // 底部按钮面板（Retry、编辑等）
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bottomButtonPanel.add(retryButton);
        bottomButtonPanel.add(editLastUserButton);
        bottomButtonPanel.add(editLastAiButton);
        
        // 输入面板
        JPanel inputSubPanel = new JPanel(new BorderLayout(10, 0));
        inputSubPanel.add(messageInputField, BorderLayout.CENTER);
        inputSubPanel.add(sendButton, BorderLayout.EAST);
        
        add(bottomButtonPanel, BorderLayout.NORTH);
        add(inputSubPanel, BorderLayout.CENTER);
    }
    
    // Getters
    public JTextField getMessageInputField() { return messageInputField; }
    public JButton getSendButton() { return sendButton; }
    public JButton getRetryButton() { return retryButton; }
    public JButton getEditLastUserButton() { return editLastUserButton; }
    public JButton getEditLastAiButton() { return editLastAiButton; }
    
    // Convenience methods
    public String getInputText() {
        return messageInputField.getText().trim();
    }
    
    public void clearInput() {
        messageInputField.setText("");
    }
    
    public void setInputText(String text) {
        messageInputField.setText(text);
    }
    
    public void setButtonsEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
        retryButton.setEnabled(enabled);
    }
    
    public void setEditButtonsEnabled(boolean hasHistory) {
        editLastUserButton.setEnabled(hasHistory);
        editLastAiButton.setEnabled(hasHistory);
    }
}

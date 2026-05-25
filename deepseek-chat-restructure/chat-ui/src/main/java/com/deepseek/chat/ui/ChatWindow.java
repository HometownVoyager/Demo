package com.deepseek.chat.ui;

import com.deepseek.chat.api.DeepSeekApiClient;
import com.deepseek.chat.core.listener.ChatListener;
import com.deepseek.chat.core.model.ChatMessage;
import com.deepseek.chat.core.model.CharacterCard;
import com.deepseek.chat.core.model.WorldBook;
import com.deepseek.chat.storage.CharacterStorage;
import com.deepseek.chat.storage.ConfigManager;
import com.deepseek.chat.storage.WorldBookStorage;
import com.deepseek.chat.ui.component.InputPanel;
import com.deepseek.chat.ui.component.SettingsPanel;
import com.deepseek.chat.ui.dialog.CharacterManager;
import com.deepseek.chat.ui.dialog.WorldBookManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * DeepSeek Chat 主窗口
 * 整合所有 UI 组件，实现聊天功能
 */
public class ChatWindow extends JFrame implements WorldBookManager.WorldBookCallback, CharacterManager.CharacterCallback {
    
    private final SettingsPanel settingsPanel;
    private final InputPanel inputPanel;
    private final JTextPane chatDisplayArea;
    private final JLabel statusLabel;
    
    private final ConfigManager configManager;
    private final WorldBookStorage worldBookStorage;
    private final CharacterStorage characterStorage;
    
    private List<ChatMessage> chatHistory;
    private List<WorldBook> worldBooks;
    private List<CharacterCard> characters;
    private DeepSeekApiClient apiClient;
    
    private boolean isLoading = false;
    private String lastUserMessage = null;
    
    public ChatWindow() {
        super("DeepSeek Chat Client");
        
        configManager = new ConfigManager();
        worldBookStorage = new WorldBookStorage();
        characterStorage = new CharacterStorage();
        
        chatHistory = new ArrayList<>();
        worldBooks = new ArrayList<>();
        characters = new ArrayList<>();
        
        settingsPanel = new SettingsPanel();
        inputPanel = new InputPanel();
        
        chatDisplayArea = new JTextPane();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        chatDisplayArea.setBackground(new Color(255, 255, 255));
        
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        initUI();
        loadData();
        setupListeners();
        
        // 检查是否需要强制输入 API Key
        Properties config = configManager.loadConfig();
        String apiKey = config.getProperty("api.key", "");
        String systemPrompt = config.getProperty("system.prompt", "你是一个有帮助的 AI 助手。请用中文回答用户的问题。");
        
        if (apiKey.isEmpty() || systemPrompt.isEmpty()) {
            showApiKeyDialog();
        }
    }
    
    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                configManager.saveHistory(chatHistory);
                System.exit(0);
            }
        });
        
        JScrollPane chatScrollPane = new JScrollPane(chatDisplayArea);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("对话记录"));
        
        add(statusLabel, BorderLayout.NORTH);
        add(settingsPanel, BorderLayout.CENTER);
        add(chatScrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // 重新调整布局
        remove(settingsPanel);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(settingsPanel, BorderLayout.NORTH);
        centerPanel.add(chatScrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private void loadData() {
        Properties config = configManager.loadConfig();
        String apiKey = config.getProperty("api.key", "");
        String systemPrompt = config.getProperty("system.prompt", "你是一个有帮助的 AI 助手。请用中文回答用户的问题。");
        
        if (!apiKey.isEmpty()) {
            apiClient = new DeepSeekApiClient(apiKey);
            settingsPanel.getApiKeyField().setText(apiKey);
            settingsPanel.getSystemPromptArea().setText(systemPrompt);
            appendToChat("[系统] 配置已加载");
        } else {
            appendToChat("[系统] 未找到配置文件，请先设置 API Key");
        }
        
        chatHistory = configManager.loadHistory();
        displayHistory();
        
        worldBooks = worldBookStorage.loadWorldBooks();
        characters = characterStorage.loadCharacters();
        updateComboItems();
    }
    
    private void setupListeners() {
        // 保存设置按钮
        settingsPanel.getSaveSettingsButton().addActionListener(e -> saveSettings());
        
        // 清空对话按钮
        settingsPanel.getClearButton().addActionListener(e -> clearChat());
        
        // 世界书管理按钮
        settingsPanel.getWorldBookButton().addActionListener(e -> openWorldBookManager());
        
        // 角色卡管理按钮
        settingsPanel.getCharacterButton().addActionListener(e -> openCharacterManager());
        
        // 发送按钮
        inputPanel.getSendButton().addActionListener(e -> sendMessage());
        inputPanel.getMessageInputField().addActionListener(e -> sendMessage());
        
        // Retry 按钮
        inputPanel.getRetryButton().addActionListener(e -> retryLastMessage());
        
        // 编辑按钮
        inputPanel.getEditLastUserButton().addActionListener(e -> editLastUserMessage());
        inputPanel.getEditLastAiButton().addActionListener(e -> editLastAiMessage());
    }
    
    private void saveSettings() {
        String apiKey = new String(settingsPanel.getApiKeyField().getPassword()).trim();
        String systemPrompt = settingsPanel.getSystemPromptArea().getText().trim();
        
        if (apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入 API Key", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        configManager.saveConfig(apiKey, systemPrompt);
        apiClient = new DeepSeekApiClient(apiKey);
        appendToChat("[系统] 设置已保存");
        statusLabel.setText("设置已保存");
        updateEditButtons();
    }
    
    private void sendMessage() {
        String userMessage = inputPanel.getInputText();
        
        if (userMessage.isEmpty() || isLoading || apiClient == null) {
            return;
        }
        
        chatHistory.add(new ChatMessage("user", userMessage));
        displayMessage("user", userMessage);
        lastUserMessage = userMessage;
        inputPanel.clearInput();
        updateEditButtons();
        
        isLoading = true;
        inputPanel.setButtonsEnabled(false);
        statusLabel.setText("正在发送请求...");
        
        new Thread(() -> {
            try {
                String systemPrompt = buildSystemPrompt();
                String response = apiClient.chat(systemPrompt, chatHistory);
                
                SwingUtilities.invokeLater(() -> {
                    chatHistory.add(new ChatMessage("assistant", response));
                    displayMessage("assistant", response);
                    configManager.saveHistory(chatHistory);
                    isLoading = false;
                    inputPanel.setButtonsEnabled(true);
                    inputPanel.getRetryButton().setEnabled(true);
                    updateEditButtons();
                    statusLabel.setText("就绪");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("[错误] " + ex.getMessage());
                    isLoading = false;
                    inputPanel.setButtonsEnabled(true);
                    inputPanel.getRetryButton().setEnabled(true);
                    updateEditButtons();
                    statusLabel.setText("请求失败");
                });
            }
        }).start();
    }
    
    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        
        // 基础 System Prompt
        String basePrompt = settingsPanel.getSystemPromptArea().getText().trim();
        if (!basePrompt.isEmpty()) {
            sb.append(basePrompt).append("\n\n");
        }
        
        // 添加世界书内容
        String selectedWorldBook = settingsPanel.getSelectedWorldBook();
        if (!"-- 无 --".equals(selectedWorldBook)) {
            for (WorldBook wb : worldBooks) {
                if (wb.getName().equals(selectedWorldBook)) {
                    sb.append("【世界观设定】\n").append(wb.getContent()).append("\n\n");
                    break;
                }
            }
        }
        
        // 添加角色卡提示词
        String selectedCharacter = settingsPanel.getSelectedCharacter();
        if (!"-- 无 --".equals(selectedCharacter)) {
            for (CharacterCard cc : characters) {
                if (cc.getName().equals(selectedCharacter)) {
                    sb.append("【角色设定】\n");
                    sb.append("名称：").append(cc.getName()).append("\n");
                    sb.append("描述：").append(cc.getDescription()).append("\n");
                    sb.append("性格：").append(cc.getPersonality()).append("\n");
                    if (cc.getGreeting() != null && !cc.getGreeting().isEmpty()) {
                        sb.append("问候语：").append(cc.getGreeting()).append("\n");
                    }
                    sb.append("\n");
                    break;
                }
            }
        }
        
        return sb.toString();
    }
    
    private void displayHistory() {
        chatDisplayArea.setText("");
        for (ChatMessage msg : chatHistory) {
            displayMessage(msg.getRole(), msg.getContent());
        }
        if (!chatHistory.isEmpty()) {
            appendToChat("[系统] 已加载 " + chatHistory.size() + " 条历史消息");
        }
    }
    
    private void displayMessage(String role, String content) {
        String displayName = "user".equals(role) ? "你" : "AI";
        String coloredText = "<b>" + displayName + ":</b><br>" + content.replace("\n", "<br>") + "<br><br>";
        chatDisplayArea.setContentType("text/html");
        chatDisplayArea.setText(chatDisplayArea.getText() + coloredText);
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
    }
    
    private void appendToChat(String text) {
        String coloredText = "<font color=\"#888888\">" + text + "</font><br><br>";
        chatDisplayArea.setContentType("text/html");
        chatDisplayArea.setText(chatDisplayArea.getText() + coloredText);
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
    }
    
    private void clearChat() {
        chatHistory.clear();
        chatDisplayArea.setText("");
        lastUserMessage = null;
        updateEditButtons();
        appendToChat("[系统] 对话已清空");
    }
    
    private void updateEditButtons() {
        boolean hasHistory = !chatHistory.isEmpty();
        inputPanel.setEditButtonsEnabled(hasHistory);
    }
    
    private void retryLastMessage() {
        if (lastUserMessage == null || chatHistory.size() < 2) {
            return;
        }
        
        // 移除最后一条 AI 回复
        int lastIndex = chatHistory.size() - 1;
        if ("assistant".equals(chatHistory.get(lastIndex).getRole())) {
            chatHistory.remove(lastIndex);
            refreshChatDisplay();
        }
        
        // 重新发送
        isLoading = true;
        inputPanel.setButtonsEnabled(false);
        statusLabel.setText("正在重新生成...");
        
        new Thread(() -> {
            try {
                String systemPrompt = buildSystemPrompt();
                String response = apiClient.chat(systemPrompt, chatHistory);
                
                SwingUtilities.invokeLater(() -> {
                    chatHistory.add(new ChatMessage("assistant", response));
                    displayMessage("assistant", response);
                    configManager.saveHistory(chatHistory);
                    isLoading = false;
                    inputPanel.setButtonsEnabled(true);
                    updateEditButtons();
                    statusLabel.setText("就绪");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("[错误] " + ex.getMessage());
                    isLoading = false;
                    inputPanel.setButtonsEnabled(true);
                    updateEditButtons();
                    statusLabel.setText("请求失败");
                });
            }
        }).start();
    }
    
    private void editLastUserMessage() {
        if (chatHistory.size() < 2) {
            return;
        }
        
        // 找到最后一条用户消息
        int lastUserIndex = -1;
        for (int i = chatHistory.size() - 1; i >= 0; i--) {
            if ("user".equals(chatHistory.get(i).getRole())) {
                lastUserIndex = i;
                break;
            }
        }
        
        if (lastUserIndex >= 0) {
            String lastUserMsg = chatHistory.get(lastUserIndex).getContent();
            inputPanel.setInputText(lastUserMsg);
            
            // 移除最后两条消息（用户消息和 AI 回复）
            if (lastUserIndex < chatHistory.size() - 1) {
                chatHistory.subList(lastUserIndex, chatHistory.size()).clear();
                refreshChatDisplay();
            } else {
                chatHistory.remove(lastUserIndex);
                refreshChatDisplay();
            }
            
            updateEditButtons();
            inputPanel.getMessageInputField().requestFocus();
        }
    }
    
    private void editLastAiMessage() {
        if (chatHistory.isEmpty()) {
            return;
        }
        
        int lastIndex = chatHistory.size() - 1;
        ChatMessage lastMsg = chatHistory.get(lastIndex);
        
        if ("assistant".equals(lastMsg.getRole())) {
            String edited = JOptionPane.showInputDialog(this, "编辑 AI 回复:", lastMsg.getContent());
            if (edited != null) {
                chatHistory.set(lastIndex, new ChatMessage("assistant", edited));
                refreshChatDisplay();
                configManager.saveHistory(chatHistory);
            }
        }
    }
    
    private void refreshChatDisplay() {
        chatDisplayArea.setText("");
        for (ChatMessage msg : chatHistory) {
            displayMessage(msg.getRole(), msg.getContent());
        }
    }
    
    private void openWorldBookManager() {
        new WorldBookManager(this, "世界书管理", worldBooks, this).setVisible(true);
    }
    
    private void openCharacterManager() {
        new CharacterManager(this, "角色卡管理", characters, this).setVisible(true);
    }
    
    @Override
    public void onSave() {
        worldBookStorage.saveWorldBooks(worldBooks);
        characterStorage.saveCharacters(characters);
    }
    
    @Override
    public void onUpdateCombo() {
        updateComboItems();
    }
    
    private void updateComboItems() {
        List<String> worldBookNames = new ArrayList<>();
        for (WorldBook wb : worldBooks) {
            worldBookNames.add(wb.getName());
        }
        settingsPanel.setWorldBookItems(worldBookNames.toArray(new String[0]));
        
        List<String> characterNames = new ArrayList<>();
        for (CharacterCard cc : characters) {
            characterNames.add(cc.getName());
        }
        settingsPanel.setCharacterItems(characterNames.toArray(new String[0]));
    }
    
    private void showApiKeyDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("欢迎使用 DeepSeek Chat Client");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JLabel descLabel = new JLabel("<html>首次使用，请先设置 API Key 和 System Prompt<br/>这些设置可以随时在设置面板中修改</html>");
        gbc.gridy = 1;
        panel.add(descLabel, gbc);
        
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("API Key:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JPasswordField apiKeyInput = new JPasswordField(30);
        panel.add(apiKeyInput, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("System Prompt:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextArea systemPromptInput = new JTextArea(3, 30);
        systemPromptInput.setLineWrap(true);
        systemPromptInput.setWrapStyleWord(true);
        systemPromptInput.setText("你是一个有帮助的 AI 助手。请用中文回答用户的问题。");
        JScrollPane sp = new JScrollPane(systemPromptInput);
        panel.add(sp, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "初始设置", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String inputApiKey = apiKeyInput.getText().trim();
            String inputSystemPrompt = systemPromptInput.getText().trim();
            
            if (inputApiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "API Key 不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                showApiKeyDialog();
                return;
            }
            
            configManager.saveConfig(inputApiKey, inputSystemPrompt);
            apiClient = new DeepSeekApiClient(inputApiKey);
            settingsPanel.getApiKeyField().setText(inputApiKey);
            settingsPanel.getSystemPromptArea().setText(inputSystemPrompt);
            appendToChat("[系统] 初始设置已保存");
        } else {
            appendToChat("[系统] 请手动在设置面板中输入 API Key");
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ChatWindow window = new ChatWindow();
            window.setVisible(true);
        });
    }
}

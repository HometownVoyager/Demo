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
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * DeepSeek Chat 主窗口 - 现代化美化版本
 * 整合所有 UI 组件，实现聊天功能，采用现代化设计风格
 */
public class ChatWindow extends JFrame implements WorldBookManager.WorldBookCallback, CharacterManager.CharacterCallback {
    
    // 颜色方案 - 现代化渐变配色
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);        // 靛蓝色主色调
    private static final Color PRIMARY_DARK = new Color(67, 56, 202);         // 深靛蓝
    private static final Color PRIMARY_LIGHT = new Color(99, 102, 241);       // 浅靛蓝
    private static final Color ACCENT_COLOR = new Color(16, 185, 129);        // 翠绿色强调色
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);   // 浅灰背景
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);    // 卡片白色背景
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);          // 主要文字
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);     // 次要文字
    private static final Color BORDER_COLOR = new Color(229, 231, 235);       // 边框颜色
    private static final Color USER_MESSAGE_BG = new Color(239, 246, 255);    // 用户消息背景
    private static final Color AI_MESSAGE_BG = new Color(255, 255, 255);      // AI 消息背景
    private static final Color SYSTEM_MESSAGE_COLOR = new Color(139, 92, 246);// 系统消息紫色
    
    private SettingsPanel settingsPanel;
    private final InputPanel inputPanel;
    private JTextPane chatDisplayArea;
    private JLabel statusLabel;
    private JPanel characterSelectorPanel;
    private JCheckBox summaryEnabledCheckBox;
    
    private final ConfigManager configManager;
    private final WorldBookStorage worldBookStorage;
    private final CharacterStorage characterStorage;
    
    private List<ChatMessage> chatHistory;
    private List<ChatMessage> summarizedHistory; // 总结后的上下文
    private List<WorldBook> worldBooks;
    private List<CharacterCard> characters;
    private DeepSeekApiClient defaultApiClient; // 默认 API 客户端
    
    private boolean isLoading = false;
    private String lastUserMessage = null;
    private int summaryThreshold = 10; // 触发总结的消息阈值
    
    public ChatWindow() {
        super("DeepSeek Chat - 智能多角色对话系统");
        
        configManager = new ConfigManager();
        worldBookStorage = new WorldBookStorage();
        characterStorage = new CharacterStorage();
        
        chatHistory = new ArrayList<>();
        summarizedHistory = new ArrayList<>();
        worldBooks = new ArrayList<>();
        characters = new ArrayList<>();
        
        inputPanel = new InputPanel();
        
        initModernUI();
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
    
    /**
     * 初始化现代化 UI 界面
     */
    private void initModernUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800); // 更大的窗口尺寸
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // 窗口关闭监听
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                configManager.saveHistory(chatHistory);
                System.exit(0);
            }
        });
        
        // 顶部标题栏 - 渐变背景
        JPanel headerPanel = createHeaderPanel();
        
        // 聊天显示区域 - 现代化样式
        chatDisplayArea = new JTextPane();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
        chatDisplayArea.setBackground(CARD_BACKGROUND);
        chatDisplayArea.setCaretColor(TEXT_PRIMARY);
        
        JScrollPane chatScrollPane = new JScrollPane(chatDisplayArea);
        chatScrollPane.setBorder(null);
        chatScrollPane.setBackground(CARD_BACKGROUND);
        chatScrollPane.getViewport().setBackground(CARD_BACKGROUND);
        
        // 左侧边栏 - 角色选择和设置
        JPanel sidebarPanel = createSidebarPanel();
        
        // 底部输入面板
        JPanel bottomPanel = createBottomPanel();
        
        // 主内容区域
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(CARD_BACKGROUND);
        mainContentPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainContentPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // 添加组件到主窗口
        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    /**
     * 创建顶部标题栏
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        // 标题和图标
        JLabel titleLabel = new JLabel("🤖 DeepSeek Chat");
        titleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("智能多角色对话系统");
        subtitleLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(200, 200, 255));
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // 状态标签
        statusLabel = new JLabel("● 就绪");
        statusLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(200, 255, 200));
        statusLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * 创建左侧边栏（角色选择和设置）
     */
    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(CARD_BACKGROUND);
        sidebarPanel.setPreferredSize(new Dimension(320, 0));
        sidebarPanel.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));
        
        // 滚动面板
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setBackground(CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 角色选择区域
        JPanel characterSelectPanel = new JPanel(new BorderLayout());
        characterSelectPanel.setBackground(new Color(249, 250, 251));
        characterSelectPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel charTitleLabel = new JLabel("🎭 角色选择");
        charTitleLabel.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 15));
        charTitleLabel.setForeground(TEXT_PRIMARY);
        
        characterSelectorPanel = new JPanel();
        characterSelectorPanel.setLayout(new BoxLayout(characterSelectorPanel, BoxLayout.Y_AXIS));
        characterSelectorPanel.setBackground(new Color(249, 250, 251));
        
        characterSelectPanel.add(charTitleLabel, BorderLayout.NORTH);
        characterSelectPanel.add(new JScrollPane(characterSelectorPanel), BorderLayout.CENTER);
        
        // 上下文总结开关
        summaryEnabledCheckBox = new JCheckBox("📝 启用上下文总结");
        summaryEnabledCheckBox.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        summaryEnabledCheckBox.setForeground(TEXT_SECONDARY);
        summaryEnabledCheckBox.setBackground(CARD_BACKGROUND);
        summaryEnabledCheckBox.setSelected(true);
        summaryEnabledCheckBox.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // 快捷操作按钮
        JPanel actionButtonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        actionButtonPanel.setBackground(CARD_BACKGROUND);
        actionButtonPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        JButton worldBookBtn = createStyledButton("📚 世界书", ACCENT_COLOR);
        JButton characterBtn = createStyledButton("🎴 角色卡", PRIMARY_LIGHT);
        JButton clearBtn = createStyledButton("🗑️ 清空", new Color(239, 68, 68));
        JButton settingsBtn = createStyledButton("⚙️ 设置", TEXT_SECONDARY);
        
        actionButtonPanel.add(worldBookBtn);
        actionButtonPanel.add(characterBtn);
        actionButtonPanel.add(clearBtn);
        actionButtonPanel.add(settingsBtn);
        
        // 添加到内容面板
        contentPanel.add(characterSelectPanel);
        contentPanel.add(summaryEnabledCheckBox);
        contentPanel.add(actionButtonPanel);
        
        // 存储按钮引用以便设置监听器
        settingsPanel = new SettingsPanelWrapper(worldBookBtn, characterBtn, clearBtn, settingsBtn);
        
        scrollPane.setViewportView(contentPanel);
        sidebarPanel.add(scrollPane, BorderLayout.CENTER);
        
        return sidebarPanel;
    }
    
    /**
     * 创建底部输入面板
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CARD_BACKGROUND);
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        bottomPanel.setPreferredSize(new Dimension(0, 100));
        
        // 输入区域 - 使用已声明的 inputPanel 字段
        JPanel inputPanelContainer = new JPanel(new BorderLayout());
        inputPanelContainer.setBackground(CARD_BACKGROUND);
        InputPanel localInputPanel = new InputPanel();
        localInputPanel.setBackground(CARD_BACKGROUND);
        localInputPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        inputPanelContainer.add(localInputPanel, BorderLayout.CENTER);
        
        bottomPanel.add(inputPanelContainer, BorderLayout.CENTER);
        
        return bottomPanel;
    }
    
    /**
     * 获取输入面板
     */
    public InputPanel getInputPanel() {
        return inputPanel;
    }
    
    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        button.setMaximumSize(new Dimension(120, 35));
        
        // 鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * 设置面板包装类（用于兼容现有代码）
     */
    private class SettingsPanelWrapper extends SettingsPanel {
        private final JButton worldBookBtn;
        private final JButton characterBtn;
        private final JButton clearBtn;
        private final JButton settingsBtn;
        
        public SettingsPanelWrapper(JButton wb, JButton cb, JButton cl, JButton st) {
            super();
            this.worldBookBtn = wb;
            this.characterBtn = cb;
            this.clearBtn = cl;
            this.settingsBtn = st;
        }
        
        @Override
        public JButton getWorldBookButton() { return worldBookBtn; }
        @Override
        public JButton getCharacterButton() { return characterBtn; }
        @Override
        public JButton getClearButton() { return clearBtn; }
        @Override
        public JButton getSaveSettingsButton() { return settingsBtn; }
    }
    
    @Deprecated
    private void initUI() {
        // 旧版初始化方法，已废弃
    }
    
    private void loadData() {
        Properties config = configManager.loadConfig();
        String apiKey = config.getProperty("api.key", "");
        String systemPrompt = config.getProperty("system.prompt", "你是一个有帮助的 AI 助手。请用中文回答用户的问题。");
        
        if (!apiKey.isEmpty()) {
            defaultApiClient = new DeepSeekApiClient(apiKey);
            settingsPanel.getApiKeyField().setText(apiKey);
            settingsPanel.getSystemPromptArea().setText(systemPrompt);
            appendToChat("[系统] 配置已加载");
        } else {
            appendToChat("[系统] 未找到配置文件，请先设置 API Key");
        }
        
        chatHistory = configManager.loadHistory();
        summarizedHistory = new ArrayList<>();
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
        defaultApiClient = new DeepSeekApiClient(apiKey);
        appendToChat("[系统] 设置已保存");
        statusLabel.setText("设置已保存");
        updateEditButtons();
    }
    
    /**
     * 获取选中的普通角色卡列表
     */
    private List<CharacterCard> getSelectedNormalCharacters() {
        List<String> selectedNames = getSelectedCharactersFromPanel();
        List<CharacterCard> result = new ArrayList<>();
        for (CharacterCard cc : characters) {
            if (!cc.isBackground() && selectedNames.contains(cc.getName())) {
                result.add(cc);
            }
        }
        return result;
    }
    
    /**
     * 获取背景角色卡（如果有）
     */
    private CharacterCard getBackgroundCharacter() {
        for (CharacterCard cc : characters) {
            if (cc.isBackground()) {
                return cc;
            }
        }
        return null;
    }
    
    private void sendMessage() {
        String userMessage = inputPanel.getInputText();
        List<CharacterCard> selectedChars = getSelectedNormalCharacters();
        
        if (userMessage.isEmpty() || isLoading || defaultApiClient == null) {
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
        
        // 使用后台线程处理多角色对话
        new Thread(() -> {
            try {
                CharacterCard backgroundChar = getBackgroundCharacter();
                
                // 如果有背景角色，先让背景角色发言
                if (backgroundChar != null) {
                    DeepSeekApiClient bgClient = new DeepSeekApiClient(
                        backgroundChar.getApiKey().isEmpty() ? 
                            new String(settingsPanel.getApiKeyField().getPassword()) : 
                            backgroundChar.getApiKey(),
                        backgroundChar.getModel(),
                        backgroundChar.isEnableThinking()
                    );
                    String bgSystemPrompt = buildCharacterSystemPrompt(backgroundChar);
                    String bgResponse = bgClient.chat(bgSystemPrompt, chatHistory);
                    
                    SwingUtilities.invokeLater(() -> {
                        chatHistory.add(new ChatMessage("assistant", "[" + backgroundChar.getName() + "] " + bgResponse));
                        displayMessageWithPrefix(backgroundChar.getName(), bgResponse);
                    });
                }
                
                // 为每个选中的角色调用 API
                for (CharacterCard cc : selectedChars) {
                    DeepSeekApiClient charClient = new DeepSeekApiClient(
                        cc.getApiKey().isEmpty() ? 
                            new String(settingsPanel.getApiKeyField().getPassword()) : 
                            cc.getApiKey(),
                        cc.getModel(),
                        cc.isEnableThinking()
                    );
                    String charSystemPrompt = buildCharacterSystemPrompt(cc);
                    String response = charClient.chat(charSystemPrompt, chatHistory);
                    
                    SwingUtilities.invokeLater(() -> {
                        chatHistory.add(new ChatMessage("assistant", "[" + cc.getName() + "] " + response));
                        displayMessageWithPrefix(cc.getName(), response);
                        configManager.saveHistory(chatHistory);
                    });
                }
                
                // 如果没有选中任何角色，使用默认 API
                if (selectedChars.isEmpty() && backgroundChar == null) {
                    String systemPrompt = buildSystemPrompt();
                    String response = defaultApiClient.chat(systemPrompt, chatHistory);
                    
                    SwingUtilities.invokeLater(() -> {
                        chatHistory.add(new ChatMessage("assistant", response));
                        displayMessage("assistant", response);
                        configManager.saveHistory(chatHistory);
                    });
                }
                
                // 检查是否需要总结
                maybeSummarizeHistory();
                
                SwingUtilities.invokeLater(() -> {
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
        
        return sb.toString();
    }
    
    /**
     * 为单个角色构建系统提示词
     */
    private String buildCharacterSystemPrompt(CharacterCard cc) {
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
        sb.append("【角色设定】\n");
        sb.append("名称：").append(cc.getName()).append("\n");
        sb.append("描述：").append(cc.getDescription()).append("\n");
        sb.append("性格：").append(cc.getPersonality()).append("\n");
        if (cc.getGreeting() != null && !cc.getGreeting().isEmpty()) {
            sb.append("问候语：").append(cc.getGreeting()).append("\n");
        }
        
        if (cc.isBackground()) {
            sb.append("\n你是一个旁白和宏观调控者，负责控制其他虚拟角色的发言和禁言，推动剧情发展。\n");
        } else {
            sb.append("\n请保持角色设定进行对话。\n");
        }
        
        return sb.toString();
    }
    
    /**
     * 显示带角色前缀的消息
     */
    private void displayMessageWithPrefix(String characterName, String content) {
        String coloredText = "<b>" + characterName + ":</b><br>" + content.replace("\n", "<br>") + "<br><br>";
        chatDisplayArea.setContentType("text/html");
        chatDisplayArea.setText(chatDisplayArea.getText() + coloredText);
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
    }
    
    /**
     * 检查是否需要总结历史消息
     */
    private void maybeSummarizeHistory() {
        if (chatHistory.size() >= summaryThreshold && defaultApiClient != null) {
            summarizeHistory();
        }
    }
    
    /**
     * 总结历史消息
     */
    private void summarizeHistory() {
        new Thread(() -> {
            try {
                String summaryPrompt = "请总结以下对话的主要内容，包括关键事件、人物关系和重要信息。用简洁的中文概括：\n";
                for (ChatMessage msg : chatHistory) {
                    String role = "user".equals(msg.getRole()) ? "用户" : "AI";
                    summaryPrompt += role + ": " + msg.getContent() + "\n";
                }
                
                List<ChatMessage> summaryMessages = new ArrayList<>();
                summaryMessages.add(new ChatMessage("user", summaryPrompt));
                
                String systemPrompt = "你是一个对话总结助手。请用简洁的中文总结对话内容，保留关键信息。";
                String summary = defaultApiClient.chat(systemPrompt, summaryMessages);
                
                SwingUtilities.invokeLater(() -> {
                    summarizedHistory.clear();
                    summarizedHistory.add(new ChatMessage("system", "[对话总结]\n" + summary));
                    appendToChat("[系统] 已生成对话总结（共 " + chatHistory.size() + " 条消息）");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("[系统] 总结失败：" + ex.getMessage());
                });
            }
        }).start();
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
                String response = defaultApiClient.chat(systemPrompt, chatHistory);
                
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
        // settingsPanel.setWorldBookItems(worldBookNames.toArray(new String[0]));
        
        // 更新角色选择面板
        characterSelectorPanel.removeAll();
        for (CharacterCard cc : characters) {
            JCheckBox checkBox = new JCheckBox(cc.getName());
            checkBox.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
            checkBox.setBackground(new Color(249, 250, 251));
            checkBox.setForeground(TEXT_PRIMARY);
            checkBox.setBorder(new EmptyBorder(8, 5, 8, 5));
            
            // 如果是背景角色，添加特殊标记
            if (cc.isBackground()) {
                checkBox.setText(cc.getName() + " 📜 (背景)");
            }
            
            characterSelectorPanel.add(checkBox);
        }
        characterSelectorPanel.revalidate();
        characterSelectorPanel.repaint();
    }
    
    /**
     * 获取选中的角色卡名称列表（从新的复选框面板）
     */
    public List<String> getSelectedCharactersFromPanel() {
        List<String> selected = new ArrayList<>();
        for (Component comp : characterSelectorPanel.getComponents()) {
            if (comp instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox) comp;
                if (cb.isSelected()) {
                    // 移除背景标记文本
                    String name = cb.getText().replace(" 📜 (背景)", "");
                    selected.add(name);
                }
            }
        }
        return selected;
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
            defaultApiClient = new DeepSeekApiClient(inputApiKey);
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

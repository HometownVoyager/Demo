import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * DeepSeek Chat Client - 图形界面聊天程序
 * 支持连接 DeepSeek API，本地存储 API Key 和历史对话
 * 支持自定义世界书和角色卡
 */
public class DeepSeekChatClient extends JFrame {
    
    // 配置文件路径
    private static final String CONFIG_FILE = "deepseek_config.properties";
    private static final String HISTORY_FILE = "chat_history.txt";
    private static final String WORLD_BOOK_FILE = "world_books.json";
    private static final String CHARACTER_FILE = "characters.json";
    
    // UI 组件
    private JPasswordField apiKeyField;
    private JTextArea systemPromptArea;
    private JTextPane chatDisplayArea;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton clearButton;
    private JButton saveSettingsButton;
    private JButton worldBookButton;
    private JButton characterButton;
    private JLabel statusLabel;
    private JComboBox<String> activeWorldBookCombo;
    private JComboBox<String> activeCharacterCombo;
    
    // 数据
    private String apiKey;
    private String systemPrompt;
    private List<ChatMessage> chatHistory;
    private List<WorldBook> worldBooks;
    private List<CharacterCard> characters;
    private boolean isLoading = false;
    
    // DeepSeek API 地址
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";
    
    public DeepSeekChatClient() {
        super("DeepSeek Chat Client");
        chatHistory = new ArrayList<>();
        worldBooks = new ArrayList<>();
        characters = new ArrayList<>();
        initUI();
        loadConfig();
        loadHistory();
        loadWorldBooks();
        loadCharacters();
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
                saveHistory();
                System.exit(0);
            }
        });
        
        // 顶部设置面板
        JPanel settingsPanel = createSettingsPanel();
        add(settingsPanel, BorderLayout.NORTH);
        
        // 中间聊天显示区域
        chatDisplayArea = new JTextPane();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        chatDisplayArea.setBackground(new Color(255, 255, 255));
        JScrollPane chatScrollPane = new JScrollPane(chatDisplayArea);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("对话记录"));
        add(chatScrollPane, BorderLayout.CENTER);
        
        // 底部输入面板
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
        
        // 状态栏
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.NORTH);
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("设置"));
        panel.setBackground(new Color(230, 230, 230));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // API Key
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("API Key:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        apiKeyField = new JPasswordField(30);
        panel.add(apiKeyField, gbc);
        
        // System Prompt
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("System Prompt:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        systemPromptArea = new JTextArea(3, 30);
        systemPromptArea.setLineWrap(true);
        systemPromptArea.setWrapStyleWord(true);
        systemPromptArea.setText("你是一个有帮助的AI助手。请用中文回答用户的问题。");
        JScrollPane sp = new JScrollPane(systemPromptArea);
        sp.setPreferredSize(new Dimension(400, 60));
        panel.add(sp, gbc);
        systemPromptArea.setText("你是一个有帮助的 AI 助手。请用中文回答用户的问题。");
        JScrollPane systemPromptScroll = new JScrollPane(systemPromptArea);
        systemPromptScroll.setPreferredSize(new Dimension(400, 60));
        panel.add(systemPromptScroll, gbc);
        
        // 世界书和角色卡选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("世界书:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        activeWorldBookCombo = new JComboBox<>();
        activeWorldBookCombo.addItem("-- 无 --");
        panel.add(activeWorldBookCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("角色卡:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        activeCharacterCombo = new JComboBox<>();
        activeCharacterCombo.addItem("-- 无 --");
        panel.add(activeCharacterCombo, gbc);
        
        // 按钮
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        saveSettingsButton = new JButton("保存设置");
        saveSettingsButton.addActionListener(this::saveSettings);
        panel.add(saveSettingsButton, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        clearButton = new JButton("清空对话");
        clearButton.addActionListener(e -> clearChat());
        worldBookButton = new JButton("管理世界书");
        worldBookButton.addActionListener(e -> openWorldBookManager());
        characterButton = new JButton("管理角色卡");
        characterButton.addActionListener(e -> openCharacterManager());
        
        buttonPanel.add(clearButton);
        buttonPanel.add(worldBookButton);
        buttonPanel.add(characterButton);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(230, 230, 230));
        
        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        messageInputField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        sendButton.addActionListener(e -> sendMessage());
        
        panel.add(messageInputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                apiKey = props.getProperty("api.key", "");
                systemPrompt = props.getProperty("system.prompt", "你是一个有帮助的AI助手。请用中文回答用户的问题。");
                
                apiKeyField.setText(apiKey);
                systemPromptArea.setText(systemPrompt);
                appendToChat("[系统] 配置已加载");
            } else {
                appendToChat("[系统] 未找到配置文件，请先设置 API Key");
            }
        } catch (Exception e) {
            appendToChat("[系统] 加载配置失败：" + e.getMessage());
        }
    }
    
    private void saveSettings(ActionEvent e) {
        apiKey = apiKeyField.getText().trim();
        systemPrompt = systemPromptArea.getText().trim();
        
        if (apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入 API Key", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Properties props = new Properties();
            props.setProperty("api.key", apiKey);
            props.setProperty("system.prompt", systemPrompt);
            
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "DeepSeek Chat Config");
            }
            
            appendToChat("[系统] 设置已保存");
            statusLabel.setText("设置已保存");
        } catch (Exception ex) {
            appendToChat("[系统] 保存设置失败：" + ex.getMessage());
            JOptionPane.showMessageDialog(this, "保存设置失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadHistory() {
        try {
            File historyFile = new File(HISTORY_FILE);
            if (historyFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
                    String line;
                    StringBuilder currentMessage = new StringBuilder();
                    String role = null;
                    
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("ROLE:")) {
                            if (role != null && currentMessage.length() > 0) {
                                chatHistory.add(new ChatMessage(role, currentMessage.toString().trim()));
                            }
                            role = line.substring(5).trim();
                            currentMessage = new StringBuilder();
                        } else if (line.startsWith("CONTENT:")) {
                            currentMessage.append(line.substring(8)).append("\n");
                        }
                    }
                    
                    // 添加最后一条消息
                    if (role != null && currentMessage.length() > 0) {
                        chatHistory.add(new ChatMessage(role, currentMessage.toString().trim()));
                    }
                }
                
                // 重新显示聊天记录
                chatDisplayArea.setText("");
                for (ChatMessage msg : chatHistory) {
                    displayMessage(msg.getRole(), msg.getContent());
                }
                
                if (!chatHistory.isEmpty()) {
                    appendToChat("[系统] 已加载 " + chatHistory.size() + " 条历史消息");
                }
            }
        } catch (Exception ex) {
            appendToChat("[系统] 加载历史记录失败：" + ex.getMessage());
        }
    }
    
    private void saveHistory() {
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE))) {
                for (ChatMessage msg : chatHistory) {
                    writer.println("ROLE:" + msg.getRole());
                    writer.println("CONTENT:" + msg.getContent());
                    writer.println();
                }
            }
        } catch (Exception e) {
            // 静默失败，避免在关闭时弹出对话框
        }
    }
    
    private void sendMessage() {
        String userMessage = messageInputField.getText().trim();
        
        if (userMessage.isEmpty()) {
            return;
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先设置 API Key", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (isLoading) {
            return;
        }
        
        // 添加用户消息到历史和显示
        chatHistory.add(new ChatMessage("user", userMessage));
        displayMessage("user", userMessage);
        messageInputField.setText("");
        
        // 发送请求到 API
        isLoading = true;
        sendButton.setEnabled(false);
        statusLabel.setText("正在发送请求...");
        
        new Thread(() -> {
            try {
                String response = callDeepSeekAPI(userMessage);
                
                SwingUtilities.invokeLater(() -> {
                    chatHistory.add(new ChatMessage("assistant", response));
                    displayMessage("assistant", response);
                    saveHistory();
                    isLoading = false;
                    sendButton.setEnabled(true);
                    statusLabel.setText("就绪");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("[错误] " + ex.getMessage());
                    isLoading = false;
                    sendButton.setEnabled(true);
                    statusLabel.setText("请求失败");
                });
            }
        }).start();
    }
    
    private String callDeepSeekAPI(String userMessage) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        
        // 构建请求体
        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{");
        jsonBody.append("\"model\":\"").append(MODEL).append("\",");
        jsonBody.append("\"messages\":[");
        
        // 构建完整的 system prompt（包含世界书和角色卡）
        StringBuilder fullSystemPrompt = new StringBuilder(systemPrompt);
        
        // 添加世界书内容
        String worldBookContent = getActiveWorldBookContent();
        if (!worldBookContent.isEmpty()) {
            fullSystemPrompt.append("\n\n【世界观设定】\n").append(worldBookContent);
        }
        
        // 添加角色卡提示词
        String characterPrompt = getActiveCharacterPrompt();
        if (!characterPrompt.isEmpty()) {
            fullSystemPrompt.append("\n\n").append(characterPrompt);
        }
        
        // 添加 system prompt
        jsonBody.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(fullSystemPrompt.toString())).append("\"},");
        
        // 添加历史消息
        for (int i = 0; i < chatHistory.size(); i++) {
            ChatMessage msg = chatHistory.get(i);
            jsonBody.append("{\"role\":\"").append(msg.getRole()).append("\",\"content\":\"")
                    .append(escapeJson(msg.getContent())).append("\"}");
            if (i < chatHistory.size() - 1) {
                jsonBody.append(",");
            }
        }
        
        jsonBody.append("]");
        jsonBody.append("}");
        
        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new Exception("API 请求失败 (HTTP " + responseCode + "): " + errorResponse.toString());
            }
        }
        
        // 读取响应
        StringBuilder response;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        // 解析 JSON 响应（简单解析，不使用外部库）
        String jsonResponse = response.toString();
        String content = extractContentFromJson(jsonResponse);
        
        if (content == null || content.isEmpty()) {
            throw new Exception("无法解析 API 响应");
        }
        
        conn.disconnect();
        return content;
    }
    
    private String extractContentFromJson(String json) {
        // 简单提取 content 字段
        int contentIndex = json.indexOf("\"content\":");
        if (contentIndex == -1) {
            return null;
        }
        
        int startIndex = json.indexOf("\"", contentIndex + 10);
        if (startIndex == -1) {
            return null;
        }
        startIndex++;
        
        int endIndex = startIndex;
        boolean escaped = false;
        while (endIndex < json.length()) {
            char c = json.charAt(endIndex);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            }
            endIndex++;
        }
        
        String content = json.substring(startIndex, endIndex);
        // 处理转义字符
        content = content.replace("\\n", "\n")
                        .replace("\\t", "\t")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
        
        return content;
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    private void displayMessage(String role, String content) {
        String prefix;
        Color color;
        
        if ("user".equals(role)) {
            prefix = "👤 我: ";
            color = new Color(230, 240, 255);
        } else if ("assistant".equals(role)) {
            prefix = "🤖 AI: ";
            color = new Color(240, 255, 240);
        } else {
            prefix = "";
            color = null;
        }
        
        if (color != null) {
            int start = chatDisplayArea.getDocument().getLength();
            chatDisplayArea.setText(chatDisplayArea.getText() + prefix + content + "\n\n");
            int end = chatDisplayArea.getDocument().getLength();
            
            try {
                chatDisplayArea.getStyledDocument().setCharacterAttributes(start, end - start, 
                    new javax.swing.text.SimpleAttributeSet() {{
                        addAttribute(javax.swing.text.StyleConstants.Background, color);
                    }}, false);
            } catch (Exception ignored) {}
        } else {
            chatDisplayArea.setText(chatDisplayArea.getText() + prefix + content + "\n\n");
        }
        
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
    }
    
    private void appendToChat(String text) {
        chatDisplayArea.setText(chatDisplayArea.getText() + text + "\n");
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
    }
    
    private void clearChat() {
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要清空所有对话记录吗？", 
            "确认", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            chatHistory.clear();
            chatDisplayArea.setText("");
            appendToChat("[系统] 对话记录已清空");
            saveHistory();
        }
    }
    
    // 打开世界书管理器
    private void openWorldBookManager() {
        WorldBookManager manager = new WorldBookManager(this, worldBooks);
        manager.setVisible(true);
    }
    
    // 打开角色卡管理器
    private void openCharacterManager() {
        CharacterManager manager = new CharacterManager(this, characters);
        manager.setVisible(true);
    }
    
    // 加载世界书
    private void loadWorldBooks() {
        try {
            File file = new File(WORLD_BOOK_FILE);
            if (file.exists()) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                worldBooks = parseWorldBooks(content.toString());
                updateWorldBookCombo();
                appendToChat("[系统] 已加载 " + worldBooks.size() + " 个世界书");
            }
        } catch (Exception e) {
            appendToChat("[系统] 加载世界书失败：" + e.getMessage());
        }
    }
    
    // 保存世界书
    void saveWorldBooks() {
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(WORLD_BOOK_FILE))) {
                for (WorldBook wb : worldBooks) {
                    writer.println("NAME:" + wb.name);
                    writer.println("CONTENT:" + wb.content.replace("\n", "\\n"));
                    writer.println("---END---");
                }
            }
        } catch (Exception e) {
            // 静默失败
        }
    }
    
    // 解析世界书
    private List<WorldBook> parseWorldBooks(String jsonLike) {
        List<WorldBook> list = new ArrayList<>();
        String[] entries = jsonLike.split("---END---");
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            String name = extractField(entry, "NAME:");
            String content = extractField(entry, "CONTENT:").replace("\\n", "\n");
            if (name != null && !name.isEmpty()) {
                list.add(new WorldBook(name, content));
            }
        }
        return list;
    }
    
    // 更新世界书下拉框
    void updateWorldBookCombo() {
        activeWorldBookCombo.removeAllItems();
        activeWorldBookCombo.addItem("-- 无 --");
        for (WorldBook wb : worldBooks) {
            activeWorldBookCombo.addItem(wb.name);
        }
    }
    
    // 加载角色卡
    private void loadCharacters() {
        try {
            File file = new File(CHARACTER_FILE);
            if (file.exists()) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                characters = parseCharacters(content.toString());
                updateCharacterCombo();
                appendToChat("[系统] 已加载 " + characters.size() + " 个角色卡");
            }
        } catch (Exception e) {
            appendToChat("[系统] 加载角色卡失败：" + e.getMessage());
        }
    }
    
    // 保存角色卡
    void saveCharacters() {
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(CHARACTER_FILE))) {
                for (CharacterCard cc : characters) {
                    writer.println("NAME:" + cc.name);
                    writer.println("DESCRIPTION:" + cc.description.replace("\n", "\\n"));
                    writer.println("PERSONALITY:" + cc.personality.replace("\n", "\\n"));
                    writer.println("GREETING:" + cc.greeting.replace("\n", "\\n"));
                    writer.println("---END---");
                }
            }
        } catch (Exception e) {
            // 静默失败
        }
    }
    
    // 解析角色卡
    private List<CharacterCard> parseCharacters(String jsonLike) {
        List<CharacterCard> list = new ArrayList<>();
        String[] entries = jsonLike.split("---END---");
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            String name = extractField(entry, "NAME:");
            String description = extractField(entry, "DESCRIPTION:").replace("\\n", "\n");
            String personality = extractField(entry, "PERSONALITY:").replace("\\n", "\n");
            String greeting = extractField(entry, "GREETING:").replace("\\n", "\n");
            if (name != null && !name.isEmpty()) {
                list.add(new CharacterCard(name, description, personality, greeting));
            }
        }
        return list;
    }
    
    // 更新角色卡下拉框
    void updateCharacterCombo() {
        activeCharacterCombo.removeAllItems();
        activeCharacterCombo.addItem("-- 无 --");
        for (CharacterCard cc : characters) {
            activeCharacterCombo.addItem(cc.name);
        }
    }
    
    // 提取字段值
    private String extractField(String text, String prefix) {
        int start = text.indexOf(prefix);
        if (start == -1) return "";
        start += prefix.length();
        int end = text.indexOf("\n", start);
        if (end == -1) return text.substring(start).trim();
        return text.substring(start, end).trim();
    }
    
    // 获取当前激活的世界书内容
    private String getActiveWorldBookContent() {
        String selected = (String) activeWorldBookCombo.getSelectedItem();
        if (selected == null || "-- 无 --".equals(selected)) return "";
        for (WorldBook wb : worldBooks) {
            if (wb.name.equals(selected)) {
                return wb.content;
            }
        }
        return "";
    }
    
    // 获取当前激活的角色卡提示词
    private String getActiveCharacterPrompt() {
        String selected = (String) activeCharacterCombo.getSelectedItem();
        if (selected == null || "-- 无 --".equals(selected)) return "";
        for (CharacterCard cc : characters) {
            if (cc.name.equals(selected)) {
                StringBuilder sb = new StringBuilder();
                sb.append("你现在扮演 ").append(cc.name).append("。\n");
                if (!cc.description.isEmpty()) {
                    sb.append("角色描述：").append(cc.description).append("\n");
                }
                if (!cc.personality.isEmpty()) {
                    sb.append("性格特点：").append(cc.personality).append("\n");
                }
                return sb.toString();
            }
        }
        return "";
    }
    
    // 内部类：世界书
    private static class WorldBook {
        String name;
        String content;
        
        public WorldBook(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }
    
    // 内部类：角色卡
    private static class CharacterCard {
        String name;
        String description;
        String personality;
        String greeting;
        
        public CharacterCard(String name, String description, String personality, String greeting) {
            this.name = name;
            this.description = description;
            this.personality = personality;
            this.greeting = greeting;
        }
    }
    
    // 内部类：聊天消息
    private static class ChatMessage {
        private final String role;
        private final String content;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
    }
    
    public static void main(String[] args) {
        // 设置 Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            DeepSeekChatClient client = new DeepSeekChatClient();
            client.setVisible(true);
        });
    }

    // 世界书管理器窗口
    private static class WorldBookManager extends JDialog {
        private List<WorldBook> worldBooks;
        private DeepSeekChatClient parent;
        private JList<String> worldBookList;
        private DefaultListModel<String> listModel;
        private JTextField nameField;
        private JTextArea contentArea;
        
        public WorldBookManager(DeepSeekChatClient parent, List<WorldBook> worldBooks) {
            super(parent, "世界书管理", true);
            this.parent = parent;
            this.worldBooks = worldBooks;
            setSize(600, 500);
            setLocationRelativeTo(parent);
            initUI();
            refreshList();
        }
        
        private void initUI() {
            setLayout(new BorderLayout(10, 10));
            
            listModel = new DefaultListModel<>();
            worldBookList = new JList<>(listModel);
            worldBookList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedWorldBook();
                }
            });
            
            JScrollPane listScrollPane = new JScrollPane(worldBookList);
            listScrollPane.setBorder(BorderFactory.createTitledBorder("世界书列表"));
            add(listScrollPane, BorderLayout.WEST);
            
            JPanel editPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(new JLabel("名称:"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 1;
            nameField = new JTextField(20);
            editPanel.add(nameField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            editPanel.add(new JLabel("内容:"), gbc);
            
            gbc.gridx = 1;
            gbc.weighty = 1;
            contentArea = new JTextArea(15, 30);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            JScrollPane contentScroll = new JScrollPane(contentArea);
            editPanel.add(contentScroll, gbc);
            
            add(editPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton addButton = new JButton("新建");
            addButton.addActionListener(e -> addWorldBook());
            JButton saveButton = new JButton("保存");
            saveButton.addActionListener(e -> saveWorldBook());
            JButton deleteButton = new JButton("删除");
            deleteButton.addActionListener(e -> deleteWorldBook());
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dispose());
            
            buttonPanel.add(addButton);
            buttonPanel.add(saveButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(closeButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void refreshList() {
            listModel.clear();
            for (WorldBook wb : worldBooks) {
                listModel.addElement(wb.name);
            }
        }
        
        private void loadSelectedWorldBook() {
            int index = worldBookList.getSelectedIndex();
            if (index >= 0 && index < worldBooks.size()) {
                WorldBook wb = worldBooks.get(index);
                nameField.setText(wb.name);
                contentArea.setText(wb.content);
            }
        }
        
        private void addWorldBook() {
            nameField.setText("");
            contentArea.setText("");
            nameField.requestFocus();
        }
        
        private void saveWorldBook() {
            String name = nameField.getText().trim();
            String content = contentArea.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入名称", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 检查是否已存在
            int existingIndex = -1;
            for (int i = 0; i < worldBooks.size(); i++) {
                if (worldBooks.get(i).name.equals(name)) {
                    existingIndex = i;
                    break;
                }
            }
            
            if (existingIndex >= 0) {
                worldBooks.get(existingIndex).content = content;
            } else {
                worldBooks.add(new WorldBook(name, content));
            }
            
            parent.saveWorldBooks();
            refreshList();
            parent.updateWorldBookCombo();
            JOptionPane.showMessageDialog(this, "世界书已保存");
        }
        
        private void deleteWorldBook() {
            int index = worldBookList.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的世界书", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int result = JOptionPane.showConfirmDialog(this, "确定要删除这个世界书吗？", "确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                worldBooks.remove(index);
                parent.saveWorldBooks();
                refreshList();
                parent.updateWorldBookCombo();
                nameField.setText("");
                contentArea.setText("");
            }
        }
    }

    // 角色卡管理器窗口
    private static class CharacterManager extends JDialog {
        private List<CharacterCard> characters;
        private DeepSeekChatClient parent;
        private JList<String> characterList;
        private DefaultListModel<String> listModel;
        private JTextField nameField;
        private JTextArea descriptionArea;
        private JTextArea personalityArea;
        private JTextArea greetingArea;
        
        public CharacterManager(DeepSeekChatClient parent, List<CharacterCard> characters) {
            super(parent, "角色卡管理", true);
            this.parent = parent;
            this.characters = characters;
            setSize(700, 600);
            setLocationRelativeTo(parent);
            initUI();
            refreshList();
        }
        
        private void initUI() {
            setLayout(new BorderLayout(10, 10));
            
            listModel = new DefaultListModel<>();
            characterList = new JList<>(listModel);
            characterList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedCharacter();
                }
            });
            
            JScrollPane listScrollPane = new JScrollPane(characterList);
            listScrollPane.setBorder(BorderFactory.createTitledBorder("角色卡列表"));
            listScrollPane.setPreferredSize(new Dimension(150, 0));
            add(listScrollPane, BorderLayout.WEST);
            
            JPanel editPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(new JLabel("名称:"), gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 1;
            nameField = new JTextField(20);
            editPanel.add(nameField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0;
            editPanel.add(new JLabel("描述:"), gbc);
            
            gbc.gridx = 1;
            gbc.weighty = 1;
            descriptionArea = new JTextArea(4, 25);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            editPanel.add(new JScrollPane(descriptionArea), gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weighty = 0;
            editPanel.add(new JLabel("性格:"), gbc);
            
            gbc.gridx = 1;
            gbc.weighty = 1;
            personalityArea = new JTextArea(4, 25);
            personalityArea.setLineWrap(true);
            personalityArea.setWrapStyleWord(true);
            editPanel.add(new JScrollPane(personalityArea), gbc);
            
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.weighty = 0;
            editPanel.add(new JLabel("问候语:"), gbc);
            
            gbc.gridx = 1;
            gbc.weighty = 1;
            greetingArea = new JTextArea(3, 25);
            greetingArea.setLineWrap(true);
            greetingArea.setWrapStyleWord(true);
            editPanel.add(new JScrollPane(greetingArea), gbc);
            
            add(editPanel, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton addButton = new JButton("新建");
            addButton.addActionListener(e -> addCharacter());
            JButton saveButton = new JButton("保存");
            saveButton.addActionListener(e -> saveCharacter());
            JButton deleteButton = new JButton("删除");
            deleteButton.addActionListener(e -> deleteCharacter());
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dispose());
            
            buttonPanel.add(addButton);
            buttonPanel.add(saveButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(closeButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void refreshList() {
            listModel.clear();
            for (CharacterCard cc : characters) {
                listModel.addElement(cc.name);
            }
        }
        
        private void loadSelectedCharacter() {
            int index = characterList.getSelectedIndex();
            if (index >= 0 && index < characters.size()) {
                CharacterCard cc = characters.get(index);
                nameField.setText(cc.name);
                descriptionArea.setText(cc.description);
                personalityArea.setText(cc.personality);
                greetingArea.setText(cc.greeting);
            }
        }
        
        private void addCharacter() {
            nameField.setText("");
            descriptionArea.setText("");
            personalityArea.setText("");
            greetingArea.setText("");
            nameField.requestFocus();
        }
        
        private void saveCharacter() {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            String personality = personalityArea.getText().trim();
            String greeting = greetingArea.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入名称", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 检查是否已存在
            int existingIndex = -1;
            for (int i = 0; i < characters.size(); i++) {
                if (characters.get(i).name.equals(name)) {
                    existingIndex = i;
                    break;
                }
            }
            
            if (existingIndex >= 0) {
                CharacterCard cc = characters.get(existingIndex);
                cc.description = description;
                cc.personality = personality;
                cc.greeting = greeting;
            } else {
                characters.add(new CharacterCard(name, description, personality, greeting));
            }
            
            parent.saveCharacters();
            refreshList();
            parent.updateCharacterCombo();
            JOptionPane.showMessageDialog(this, "角色卡已保存");
        }
        
        private void deleteCharacter() {
            int index = characterList.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的角色卡", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int result = JOptionPane.showConfirmDialog(this, "确定要删除这个角色卡吗？", "确认", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                characters.remove(index);
                parent.saveCharacters();
                refreshList();
                parent.updateCharacterCombo();
                nameField.setText("");
                descriptionArea.setText("");
                personalityArea.setText("");
                greetingArea.setText("");
            }
        }
    }
}

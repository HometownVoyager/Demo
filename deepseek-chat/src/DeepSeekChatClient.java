import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
 */
public class DeepSeekChatClient extends JFrame {
    
    // 配置文件路径
    private static final String CONFIG_FILE = "deepseek_config.properties";
    private static final String HISTORY_FILE = "chat_history.txt";
    
    // UI 组件
    private JPasswordField apiKeyField;
    private JTextArea systemPromptArea;
    private JTextPane chatDisplayArea;
    private JTextField messageInputField;
    private JButton sendButton;
    private JButton clearButton;
    private JButton saveSettingsButton;
    private JLabel statusLabel;
    
    // 数据
    private String apiKey;
    private String systemPrompt;
    private List<ChatMessage> chatHistory;
    private boolean isLoading = false;
    
    // DeepSeek API 地址
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-chat";
    
    public DeepSeekChatClient() {
        super("DeepSeek Chat Client");
        chatHistory = new ArrayList<>();
        initUI();
        loadConfig();
        loadHistory();
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
        
        // 按钮
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        saveSettingsButton = new JButton("保存设置");
        saveSettingsButton.addActionListener(this::saveSettings);
        panel.add(saveSettingsButton, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        clearButton = new JButton("清空对话");
        clearButton.addActionListener(e -> clearChat());
        panel.add(clearButton, gbc);
        
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
        
        // 添加 system prompt
        jsonBody.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"},");
        
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
}

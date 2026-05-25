package com.deepseek.chat.ui.dialog;

import com.deepseek.chat.core.model.CharacterCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 角色卡管理对话框
 * 用于创建、编辑和删除角色卡
 */
public class CharacterManager extends JDialog {
    
    private final List<CharacterCard> characters;
    private final CharacterCallback callback;
    private final DefaultListModel<String> listModel;
    private final JList<String> characterList;
    private final JTextField nameField;
    private final JTextArea descriptionArea;
    private final JTextArea personalityArea;
    private final JTextArea greetingArea;
    
    /**
     * 回调接口，用于通知主窗口角色卡的变化
     */
    public interface CharacterCallback {
        void onSave();
        void onUpdateCombo();
    }
    
    public CharacterManager(Frame parent, String title, List<CharacterCard> characters, CharacterCallback callback) {
        super(parent, title, true);
        this.characters = characters;
        this.callback = callback;
        
        listModel = new DefaultListModel<>();
        characterList = new JList<>(listModel);
        nameField = new JTextField(20);
        descriptionArea = new JTextArea(4, 25);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        personalityArea = new JTextArea(4, 25);
        personalityArea.setLineWrap(true);
        personalityArea.setWrapStyleWord(true);
        greetingArea = new JTextArea(3, 25);
        greetingArea.setLineWrap(true);
        greetingArea.setWrapStyleWord(true);
        
        setSize(700, 600);
        setLocationRelativeTo(parent);
        initUI();
        refreshList();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        // 左侧列表
        characterList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedCharacter();
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(characterList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("角色卡列表"));
        listScrollPane.setPreferredSize(new Dimension(150, 0));
        add(listScrollPane, BorderLayout.WEST);
        
        // 右侧编辑面板
        JPanel editPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(new JLabel("名称:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        editPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        editPanel.add(new JLabel("描述:"), gbc);
        
        gbc.gridx = 1;
        gbc.weighty = 1;
        editPanel.add(new JScrollPane(descriptionArea), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0;
        editPanel.add(new JLabel("性格:"), gbc);
        
        gbc.gridx = 1;
        gbc.weighty = 1;
        editPanel.add(new JScrollPane(personalityArea), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0;
        editPanel.add(new JLabel("问候语:"), gbc);
        
        gbc.gridx = 1;
        gbc.weighty = 1;
        editPanel.add(new JScrollPane(greetingArea), gbc);
        
        add(editPanel, BorderLayout.CENTER);
        
        // 底部按钮
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
            listModel.addElement(cc.getName());
        }
    }
    
    private void loadSelectedCharacter() {
        int index = characterList.getSelectedIndex();
        if (index >= 0 && index < characters.size()) {
            CharacterCard cc = characters.get(index);
            nameField.setText(cc.getName());
            descriptionArea.setText(cc.getDescription());
            personalityArea.setText(cc.getPersonality());
            greetingArea.setText(cc.getGreeting());
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
            if (characters.get(i).getName().equals(name)) {
                existingIndex = i;
                break;
            }
        }
        
        if (existingIndex >= 0) {
            CharacterCard cc = characters.get(existingIndex);
            cc.setDescription(description);
            cc.setPersonality(personality);
            cc.setGreeting(greeting);
        } else {
            characters.add(new CharacterCard(name, description, personality, greeting));
        }
        
        if (callback != null) {
            callback.onSave();
            callback.onUpdateCombo();
        }
        
        refreshList();
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
            if (callback != null) {
                callback.onSave();
                callback.onUpdateCombo();
            }
            refreshList();
            nameField.setText("");
            descriptionArea.setText("");
            personalityArea.setText("");
            greetingArea.setText("");
        }
    }
}

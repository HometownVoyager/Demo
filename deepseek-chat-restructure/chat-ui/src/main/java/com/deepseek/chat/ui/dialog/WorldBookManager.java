package com.deepseek.chat.ui.dialog;

import com.deepseek.chat.core.model.WorldBook;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * 世界书管理对话框
 * 用于创建、编辑和删除世界书
 */
public class WorldBookManager extends JDialog {
    
    private final List<WorldBook> worldBooks;
    private final WorldBookCallback callback;
    private final DefaultListModel<String> listModel;
    private final JList<String> worldBookList;
    private final JTextField nameField;
    private final JTextArea contentArea;
    
    /**
     * 回调接口，用于通知主窗口世界书的变化
     */
    public interface WorldBookCallback {
        void onSave();
        void onUpdateCombo();
    }
    
    public WorldBookManager(Frame parent, String title, List<WorldBook> worldBooks, WorldBookCallback callback) {
        super(parent, title, true);
        this.worldBooks = worldBooks;
        this.callback = callback;
        
        listModel = new DefaultListModel<>();
        worldBookList = new JList<>(listModel);
        nameField = new JTextField(20);
        contentArea = new JTextArea(15, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
        initUI();
        refreshList();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        // 左侧列表
        worldBookList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedWorldBook();
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(worldBookList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("世界书列表"));
        add(listScrollPane, BorderLayout.WEST);
        
        // 右侧编辑面板
        JPanel editPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        editPanel.add(new JLabel("名称:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        editPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        editPanel.add(new JLabel("内容:"), gbc);
        
        gbc.gridx = 1;
        gbc.weighty = 1;
        JScrollPane contentScroll = new JScrollPane(contentArea);
        editPanel.add(contentScroll, gbc);
        
        add(editPanel, BorderLayout.CENTER);
        
        // 底部按钮
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
            listModel.addElement(wb.getName());
        }
    }
    
    private void loadSelectedWorldBook() {
        int index = worldBookList.getSelectedIndex();
        if (index >= 0 && index < worldBooks.size()) {
            WorldBook wb = worldBooks.get(index);
            nameField.setText(wb.getName());
            contentArea.setText(wb.getContent());
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
            if (worldBooks.get(i).getName().equals(name)) {
                existingIndex = i;
                break;
            }
        }
        
        if (existingIndex >= 0) {
            worldBooks.get(existingIndex).setContent(content);
        } else {
            worldBooks.add(new WorldBook(name, content));
        }
        
        if (callback != null) {
            callback.onSave();
            callback.onUpdateCombo();
        }
        
        refreshList();
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
            if (callback != null) {
                callback.onSave();
                callback.onUpdateCombo();
            }
            refreshList();
            nameField.setText("");
            contentArea.setText("");
        }
    }
}

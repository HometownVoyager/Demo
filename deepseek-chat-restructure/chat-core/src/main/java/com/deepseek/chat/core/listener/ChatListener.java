package com.deepseek.chat.core.listener;

import com.deepseek.chat.core.model.ChatMessage;

/**
 * 聊天监听器接口
 * 用于监听聊天事件，如消息发送、接收等
 */
public interface ChatListener {
    
    /**
     * 当消息即将发送时调用
     * @param message 要发送的消息
     */
    void onBeforeSend(ChatMessage message);
    
    /**
     * 当消息发送成功后调用
     * @param message 已发送的消息
     * @param response AI 的回复消息
     */
    void onAfterReceive(ChatMessage message, ChatMessage response);
    
    /**
     * 当发生错误时调用
     * @param error 错误信息
     */
    void onError(String error);
    
    /**
     * 当加载状态变化时调用
     * @param isLoading 是否正在加载
     */
    void onLoadingChanged(boolean isLoading);
}

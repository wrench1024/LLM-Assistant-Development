package com.uni.research.module.writing.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface WritingService {
    /**
     * Process text for writing assistance (旧版，保持兼容)
     * 
     * @param userId      Current user ID
     * @param text        Text to process
     * @param instruction Instruction (polish, expand, etc.)
     * @param context     Optional context
     * @return SSE Emitter
     */
    SseEmitter processText(Long userId, String text, String instruction, String context);
    
    /**
     * Process text with genre-specific prompt template (新版)
     * 
     * @param userId      Current user ID
     * @param text        Text to process
     * @param genre       Writing genre (academic_paper, literature_review, research_report)
     * @param action      Action type (polish, expand, continue, etc.)
     * @param context     Optional context
     * @param templateId  Optional custom template ID
     * @return SSE Emitter
     */
    SseEmitter processWithTemplate(Long userId, String text, String genre, String action, 
                                   String context, Long templateId);
}

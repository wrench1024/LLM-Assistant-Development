package com.uni.research.module.writing.enums;

import lombok.Getter;

/**
 * 写作辅助动作类型枚举
 */
@Getter
public enum PromptAction {
    
    POLISH("polish", "润色优化", "对文本进行语言润色和表达优化"),
    EXPAND("expand", "扩写延伸", "对文本内容进行扩展和深化"),
    CONTINUE("continue", "续写补充", "根据上下文继续撰写后续内容"),
    FIX_GRAMMAR("fix_grammar", "语法修正", "修正语法错误和表达问题"),
    TRANSLATE("translate", "中英互译", "在中英文之间进行翻译"),
    SUMMARIZE("summarize", "摘要总结", "提取核心内容生成摘要");
    
    private final String code;
    private final String name;
    private final String description;
    
    PromptAction(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public static PromptAction fromCode(String code) {
        for (PromptAction action : values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown prompt action code: " + code);
    }
}

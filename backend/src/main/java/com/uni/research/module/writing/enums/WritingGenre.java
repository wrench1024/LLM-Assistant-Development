package com.uni.research.module.writing.enums;

import lombok.Getter;

/**
 * 科研文体类型枚举
 */
@Getter
public enum WritingGenre {
    
    ACADEMIC_PAPER("academic_paper", "学术论文", "用于撰写学术期刊论文、会议论文等正式学术文献"),
    LITERATURE_REVIEW("literature_review", "文献综述", "用于系统性地综述和分析某一研究领域的文献"),
    RESEARCH_REPORT("research_report", "研究报告", "用于撰写课题研究报告、项目总结报告等");
    
    private final String code;
    private final String name;
    private final String description;
    
    WritingGenre(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public static WritingGenre fromCode(String code) {
        for (WritingGenre genre : values()) {
            if (genre.code.equals(code)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Unknown writing genre code: " + code);
    }
}

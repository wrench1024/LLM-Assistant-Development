package com.uni.research.module.sensitive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uni.research.module.sensitive.dto.CheckTextRequest;
import com.uni.research.module.sensitive.dto.CheckTextResponse;
import com.uni.research.module.sensitive.dto.SensitiveWordRequest;
import com.uni.research.module.sensitive.entity.SensitiveWord;

import java.util.List;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService {
    
    /**
     * 添加敏感词
     */
    SensitiveWord addWord(SensitiveWordRequest request);
    
    /**
     * 更新敏感词
     */
    SensitiveWord updateWord(Long id, SensitiveWordRequest request);
    
    /**
     * 删除敏感词
     */
    void deleteWord(Long id);
    
    /**
     * 批量删除敏感词
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 获取敏感词详情
     */
    SensitiveWord getWord(Long id);
    
    /**
     * 分页查询敏感词
     */
    Page<SensitiveWord> pageWords(Integer current, Integer size, String keyword, Integer type, Integer enabled);
    
    /**
     * 获取所有启用的敏感词
     */
    List<SensitiveWord> getAllEnabledWords();
    
    /**
     * 检测文本中的敏感词
     */
    CheckTextResponse checkText(CheckTextRequest request);
    
    /**
     * 刷新敏感词缓存
     */
    void refreshCache();
}

package com.uni.research.module.sensitive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uni.research.common.exception.BizException;
import com.uni.research.common.result.ResultCode;
import com.uni.research.module.sensitive.dto.CheckTextRequest;
import com.uni.research.module.sensitive.dto.CheckTextResponse;
import com.uni.research.module.sensitive.dto.SensitiveWordRequest;
import com.uni.research.module.sensitive.entity.SensitiveWord;
import com.uni.research.module.sensitive.mapper.SensitiveWordMapper;
import com.uni.research.module.sensitive.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感词服务实现
 * 使用 DFA 算法进行敏感词检测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl implements SensitiveWordService {
    
    private final SensitiveWordMapper sensitiveWordMapper;
    
    /**
     * DFA 算法的敏感词树
     */
    private Map<Character, Object> sensitiveWordTree = new ConcurrentHashMap<>();
    
    /**
     * 敏感词信息映射（用于返回类型等信息）
     */
    private Map<String, SensitiveWord> sensitiveWordMap = new ConcurrentHashMap<>();
    
    /**
     * 初始化敏感词树
     */
    @PostConstruct
    public void init() {
        refreshCache();
    }
    
    @Override
    public SensitiveWord addWord(SensitiveWordRequest request) {
        // 检查敏感词是否已存在
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getWord, request.getWord());
        if (sensitiveWordMapper.selectCount(wrapper) > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "敏感词已存在");
        }
        
        SensitiveWord word = new SensitiveWord();
        BeanUtils.copyProperties(request, word);
        sensitiveWordMapper.insert(word);
        
        // 刷新缓存
        refreshCache();
        
        return word;
    }
    
    @Override
    public SensitiveWord updateWord(Long id, SensitiveWordRequest request) {
        SensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null) {
            throw new BizException(ResultCode.NOT_FOUND, "敏感词不存在");
        }
        
        // 如果修改了敏感词内容，检查是否与其他敏感词重复
        if (!word.getWord().equals(request.getWord())) {
            LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getWord, request.getWord());
            wrapper.ne(SensitiveWord::getId, id);
            if (sensitiveWordMapper.selectCount(wrapper) > 0) {
                throw new BizException(ResultCode.BAD_REQUEST, "敏感词已存在");
            }
        }
        
        BeanUtils.copyProperties(request, word);
        sensitiveWordMapper.updateById(word);
        
        // 刷新缓存
        refreshCache();
        
        return word;
    }
    
    @Override
    public void deleteWord(Long id) {
        sensitiveWordMapper.deleteById(id);
        refreshCache();
    }
    
    @Override
    public void batchDelete(List<Long> ids) {
        sensitiveWordMapper.deleteBatchIds(ids);
        refreshCache();
    }
    
    @Override
    public SensitiveWord getWord(Long id) {
        SensitiveWord word = sensitiveWordMapper.selectById(id);
        if (word == null) {
            throw new BizException(ResultCode.NOT_FOUND, "敏感词不存在");
        }
        return word;
    }
    
    @Override
    public Page<SensitiveWord> pageWords(Integer current, Integer size, String keyword, Integer type, Integer enabled) {
        Page<SensitiveWord> page = new Page<>(current, size);
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SensitiveWord::getWord, keyword);
        }
        if (type != null) {
            wrapper.eq(SensitiveWord::getType, type);
        }
        if (enabled != null) {
            wrapper.eq(SensitiveWord::getEnabled, enabled);
        }
        
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        
        return sensitiveWordMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Cacheable(value = "sensitiveWords", key = "'all'")
    public List<SensitiveWord> getAllEnabledWords() {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getEnabled, 1);
        return sensitiveWordMapper.selectList(wrapper);
    }
    
    @Override
    public CheckTextResponse checkText(CheckTextRequest request) {
        String text = request.getText();
        if (!StringUtils.hasText(text)) {
            return new CheckTextResponse(false, Collections.emptyList());
        }
        
        List<CheckTextResponse.SensitiveWordMatch> matches = new ArrayList<>();
        
        int textLength = text.length();
        for (int i = 0; i < textLength; i++) {
            int matchLength = checkSensitiveWord(text, i);
            if (matchLength > 0) {
                String matchedWord = text.substring(i, i + matchLength);
                SensitiveWord wordInfo = sensitiveWordMap.get(matchedWord);
                
                CheckTextResponse.SensitiveWordMatch match = new CheckTextResponse.SensitiveWordMatch();
                match.setWord(matchedWord);
                match.setType(wordInfo != null ? wordInfo.getType() : 4);
                match.setStartIndex(i);
                match.setEndIndex(i + matchLength);
                
                matches.add(match);
                i += matchLength - 1; // 跳过已匹配的部分
            }
        }
        
        return new CheckTextResponse(!matches.isEmpty(), matches);
    }
    
    @Override
    @CacheEvict(value = "sensitiveWords", allEntries = true)
    public void refreshCache() {
        log.info("开始刷新敏感词缓存");
        
        // 清空旧的树和映射
        sensitiveWordTree.clear();
        sensitiveWordMap.clear();
        
        // 直接从数据库获取所有启用的敏感词（不使用缓存）
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getEnabled, 1);
        List<SensitiveWord> words = sensitiveWordMapper.selectList(wrapper);
        
        // 构建敏感词树
        for (SensitiveWord word : words) {
            addWordToTree(word.getWord());
            sensitiveWordMap.put(word.getWord(), word);
        }
        
        log.info("敏感词缓存刷新完成，共加载 {} 个敏感词", words.size());
    }
    
    /**
     * 将敏感词添加到 DFA 树中
     */
    private void addWordToTree(String word) {
        if (!StringUtils.hasText(word)) {
            return;
        }
        
        Map<Character, Object> currentMap = sensitiveWordTree;
        
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            
            @SuppressWarnings("unchecked")
            Map<Character, Object> nextMap = (Map<Character, Object>) currentMap.get(c);
            
            if (nextMap == null) {
                nextMap = new ConcurrentHashMap<>();
                currentMap.put(c, nextMap);
            }
            
            currentMap = nextMap;
            
            // 最后一个字符，标记为结束
            if (i == word.length() - 1) {
                currentMap.put('$', true); // $ 表示一个词的结束
            }
        }
    }
    
    /**
     * 检查从指定位置开始是否存在敏感词
     * 
     * @param text 待检测文本
     * @param startIndex 开始位置
     * @return 匹配的敏感词长度，0 表示未匹配
     */
    private int checkSensitiveWord(String text, int startIndex) {
        Map<Character, Object> currentMap = sensitiveWordTree;
        int matchLength = 0;
        int tempMatchLength = 0;
        
        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            
            @SuppressWarnings("unchecked")
            Map<Character, Object> nextMap = (Map<Character, Object>) currentMap.get(c);
            
            if (nextMap == null) {
                break;
            }
            
            tempMatchLength++;
            
            // 检查是否到达词的结尾
            if (nextMap.containsKey('$')) {
                matchLength = tempMatchLength;
            }
            
            currentMap = nextMap;
        }
        
        return matchLength;
    }
}

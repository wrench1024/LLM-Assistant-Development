package com.uni.research.module.sensitive.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.uni.research.common.result.Result;
import com.uni.research.module.sensitive.dto.CheckTextRequest;
import com.uni.research.module.sensitive.dto.CheckTextResponse;
import com.uni.research.module.sensitive.dto.SensitiveWordRequest;
import com.uni.research.module.sensitive.entity.SensitiveWord;
import com.uni.research.module.sensitive.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 敏感词管理控制器
 */
@Tag(name = "敏感词管理")
@RestController
@RequestMapping("/sensitive")
@RequiredArgsConstructor
public class SensitiveWordController {
    
    private final SensitiveWordService sensitiveWordService;
    
    @Operation(summary = "添加敏感词")
    @PostMapping("/words")
    public Result<SensitiveWord> addWord(@Valid @RequestBody SensitiveWordRequest request) {
        return Result.success(sensitiveWordService.addWord(request));
    }
    
    @Operation(summary = "更新敏感词")
    @PutMapping("/words/{id}")
    public Result<SensitiveWord> updateWord(
            @Parameter(description = "敏感词ID") @PathVariable Long id,
            @Valid @RequestBody SensitiveWordRequest request) {
        return Result.success(sensitiveWordService.updateWord(id, request));
    }
    
    @Operation(summary = "删除敏感词")
    @DeleteMapping("/words/{id}")
    public Result<Void> deleteWord(@Parameter(description = "敏感词ID") @PathVariable Long id) {
        sensitiveWordService.deleteWord(id);
        return Result.success();
    }
    
    @Operation(summary = "批量删除敏感词")
    @DeleteMapping("/words/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        sensitiveWordService.batchDelete(ids);
        return Result.success();
    }
    
    @Operation(summary = "获取敏感词详情")
    @GetMapping("/words/{id}")
    public Result<SensitiveWord> getWord(@Parameter(description = "敏感词ID") @PathVariable Long id) {
        return Result.success(sensitiveWordService.getWord(id));
    }
    
    @Operation(summary = "分页查询敏感词")
    @GetMapping("/words")
    public Result<Page<SensitiveWord>> pageWords(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "是否启用") @RequestParam(required = false) Integer enabled) {
        return Result.success(sensitiveWordService.pageWords(current, size, keyword, type, enabled));
    }
    
    @Operation(summary = "检测文本中的敏感词")
    @PostMapping("/check")
    public Result<CheckTextResponse> checkText(@Valid @RequestBody CheckTextRequest request) {
        return Result.success(sensitiveWordService.checkText(request));
    }
    
    @Operation(summary = "刷新敏感词缓存")
    @PostMapping("/refresh")
    public Result<Void> refreshCache() {
        sensitiveWordService.refreshCache();
        return Result.success();
    }
}

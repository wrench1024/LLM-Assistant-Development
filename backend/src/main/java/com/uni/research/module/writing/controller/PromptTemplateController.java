package com.uni.research.module.writing.controller;

import com.uni.research.common.result.Result;
import com.uni.research.module.auth.entity.User;
import com.uni.research.module.auth.mapper.UserMapper;
import com.uni.research.module.writing.dto.PromptTemplateCreateRequest;
import com.uni.research.module.writing.dto.PromptTemplateDTO;
import com.uni.research.module.writing.dto.PromptTemplateUpdateRequest;
import com.uni.research.module.writing.enums.PromptAction;
import com.uni.research.module.writing.enums.WritingGenre;
import com.uni.research.module.writing.service.PromptTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "提示词模板管理", description = "提示词模板的增删改查接口")
@RestController
@RequestMapping("/writing/templates")
@RequiredArgsConstructor
public class PromptTemplateController {
    
    private final PromptTemplateService promptTemplateService;
    private final UserMapper userMapper;
    
    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user.getId();
    }
    
    @Operation(summary = "获取文体类型列表")
    @GetMapping("/genres")
    public Result<List<Map<String, String>>> getGenres() {
        List<Map<String, String>> genres = Arrays.stream(WritingGenre.values())
            .map(genre -> {
                Map<String, String> map = new HashMap<>();
                map.put("code", genre.getCode());
                map.put("name", genre.getName());
                map.put("description", genre.getDescription());
                return map;
            })
            .collect(Collectors.toList());
        return Result.success(genres);
    }
    
    @Operation(summary = "获取动作类型列表")
    @GetMapping("/actions")
    public Result<List<Map<String, String>>> getActions() {
        List<Map<String, String>> actions = Arrays.stream(PromptAction.values())
            .map(action -> {
                Map<String, String> map = new HashMap<>();
                map.put("code", action.getCode());
                map.put("name", action.getName());
                map.put("description", action.getDescription());
                return map;
            })
            .collect(Collectors.toList());
        return Result.success(actions);
    }
    
    @Operation(summary = "查询模板列表")
    @GetMapping
    public Result<List<PromptTemplateDTO>> listTemplates(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String action) {
        Long userId = getUserId();
        List<PromptTemplateDTO> templates = promptTemplateService.listTemplates(userId, genre, action);
        return Result.success(templates);
    }
    
    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<PromptTemplateDTO> getTemplate(@PathVariable Long id) {
        Long userId = getUserId();
        PromptTemplateDTO template = promptTemplateService.getTemplateDetail(userId, id);
        return Result.success(template);
    }
    
    @Operation(summary = "创建自定义模板")
    @PostMapping
    public Result<PromptTemplateDTO> createTemplate(
            @Validated @RequestBody PromptTemplateCreateRequest request) {
        Long userId = getUserId();
        PromptTemplateDTO template = promptTemplateService.createTemplate(userId, request);
        return Result.success(template);
    }
    
    @Operation(summary = "更新自定义模板")
    @PutMapping
    public Result<PromptTemplateDTO> updateTemplate(
            @Validated @RequestBody PromptTemplateUpdateRequest request) {
        Long userId = getUserId();
        PromptTemplateDTO template = promptTemplateService.updateTemplate(userId, request);
        return Result.success(template);
    }
    
    @Operation(summary = "删除自定义模板")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        Long userId = getUserId();
        promptTemplateService.deleteTemplate(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "初始化系统内置模板（管理员）")
    @PostMapping("/init")
    public Result<Void> initBuiltinTemplates() {
        promptTemplateService.initBuiltinTemplates();
        return Result.success();
    }
}

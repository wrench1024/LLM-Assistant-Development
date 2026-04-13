package com.uni.research.module.writing.controller;

import com.uni.research.module.writing.dto.WritingProcessRequest;
import com.uni.research.module.writing.dto.WritingRequest;
import com.uni.research.module.writing.service.WritingService;
import com.uni.research.module.auth.mapper.UserMapper;
import com.uni.research.module.auth.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "写作辅助", description = "AI 写作辅助功能")
@RestController
@RequestMapping("/writing")
@RequiredArgsConstructor
public class WritingController {

    private final WritingService writingService;
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

    @Operation(summary = "写作辅助处理（旧版，保持兼容）")
    @PostMapping(value = "/process", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter process(@RequestBody WritingRequest request) {
        Long userId = getUserId();
        return writingService.processText(userId, request.getText(), request.getInstruction(), request.getContext());
    }
    
    @Operation(summary = "写作辅助处理（新版，支持文体和提示词模板）")
    @PostMapping(value = "/process-with-template", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter processWithTemplate(@Validated @RequestBody WritingProcessRequest request) {
        Long userId = getUserId();
        return writingService.processWithTemplate(
            userId, 
            request.getText(), 
            request.getGenre(), 
            request.getAction(), 
            request.getContext(),
            request.getTemplateId()
        );
    }
}

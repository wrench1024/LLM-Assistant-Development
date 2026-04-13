package com.uni.research.module.writing.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uni.research.module.writing.entity.PromptTemplate;
import com.uni.research.module.writing.service.PromptTemplateService;
import com.uni.research.module.writing.service.WritingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingServiceImpl implements WritingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PromptTemplateService promptTemplateService;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(Duration.ofMinutes(3))
            .build();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final String PYTHON_WRITE_URL = "http://localhost:8000/api/v1/write/process";

    @Override
    public SseEmitter processText(Long userId, String text, String instruction, String context) {
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutes

        executor.execute(() -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("text", text);
                payload.put("instruction", instruction);
                if (context != null) {
                    payload.put("context", context);
                }

                String jsonBody = objectMapper.writeValueAsString(payload);
                RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(PYTHON_WRITE_URL)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        emitter.send(SseEmitter.event().data("Error from AI Service: " + response.code()));
                        emitter.complete();
                        return;
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data.trim())) {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                break;
                            }
                            emitter.send(SseEmitter.event().data(data));
                        }
                    }
                    emitter.complete();
                }
            } catch (Exception e) {
                log.error("Writing Service Error", e);
                try {
                    emitter.send(SseEmitter.event().data("Error: " + e.getMessage()));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
    
    @Override
    public SseEmitter processWithTemplate(Long userId, String text, String genre, String action, 
                                         String context, Long templateId) {
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutes

        executor.execute(() -> {
            try {
                // 获取提示词模板
                PromptTemplate template = promptTemplateService.getTemplate(userId, genre, action, templateId);
                
                // 构建完整的提示词
                String fullPrompt = buildPrompt(template.getPromptContent(), text, context);
                
                // 调用 AI 服务
                Map<String, Object> payload = new HashMap<>();
                payload.put("text", fullPrompt);
                payload.put("instruction", action);
                payload.put("genre", genre);

                String jsonBody = objectMapper.writeValueAsString(payload);
                RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(PYTHON_WRITE_URL)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        emitter.send(SseEmitter.event().data("Error from AI Service: " + response.code()));
                        emitter.complete();
                        return;
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data.trim())) {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                break;
                            }
                            emitter.send(SseEmitter.event().data(data));
                        }
                    }
                    emitter.complete();
                }
            } catch (Exception e) {
                log.error("Writing Service with Template Error", e);
                try {
                    emitter.send(SseEmitter.event().data("Error: " + e.getMessage()));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
    
    /**
     * 构建完整的提示词，替换占位符
     */
    private String buildPrompt(String template, String text, String context) {
        String prompt = template.replace("{text}", text != null ? text : "");
        
        if (context != null && !context.isEmpty()) {
            prompt = prompt.replace("{context}", "上下文信息：\n" + context);
        } else {
            prompt = prompt.replace("{context}", "");
        }
        
        return prompt;
    }
}


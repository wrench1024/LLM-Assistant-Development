package com.uni.research.module.writing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.uni.research.common.exception.BizException;
import com.uni.research.module.writing.dto.PromptTemplateCreateRequest;
import com.uni.research.module.writing.dto.PromptTemplateDTO;
import com.uni.research.module.writing.dto.PromptTemplateUpdateRequest;
import com.uni.research.module.writing.entity.PromptTemplate;
import com.uni.research.module.writing.enums.PromptAction;
import com.uni.research.module.writing.enums.WritingGenre;
import com.uni.research.module.writing.mapper.PromptTemplateMapper;
import com.uni.research.module.writing.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {
    
    private final PromptTemplateMapper promptTemplateMapper;
    
    @PostConstruct
    public void init() {
        initBuiltinTemplates();
    }

    @Override
    @Transactional
    public void initBuiltinTemplates() {
        // 检查是否已初始化
        Long count = promptTemplateMapper.selectCount(
            new LambdaQueryWrapper<PromptTemplate>().eq(PromptTemplate::getIsBuiltin, true)
        );
        if (count > 0) {
            log.info("系统内置模板已存在，跳过初始化");
            return;
        }
        
        log.info("开始初始化系统内置提示词模板...");
        List<PromptTemplate> templates = new ArrayList<>();
        
        // 学术论文模板
        templates.addAll(createAcademicPaperTemplates());
        
        // 文献综述模板
        templates.addAll(createLiteratureReviewTemplates());
        
        // 研究报告模板
        templates.addAll(createResearchReportTemplates());
        
        // 批量插入
        templates.forEach(promptTemplateMapper::insert);
        log.info("系统内置模板初始化完成，共 {} 个模板", templates.size());
    }
    
    private List<PromptTemplate> createAcademicPaperTemplates() {
        List<PromptTemplate> templates = new ArrayList<>();
        String genre = WritingGenre.ACADEMIC_PAPER.getCode();
        
        templates.add(createTemplate(genre, PromptAction.POLISH.getCode(),
            "学术论文润色",
            "你是一位资深的学术论文编辑。请对以下学术论文文本进行润色优化：\n\n" +
            "要求：\n" +
            "1. 保持学术严谨性和专业性\n" +
            "2. 使用规范的学术表达和术语\n" +
            "3. 确保逻辑清晰、论证严密\n" +
            "4. 优化句式结构，提升可读性\n" +
            "5. 保持原文的核心观点和数据不变\n\n" +
            "原文：\n{text}\n\n" +
            "{context}\n\n" +
            "请直接输出润色后的文本：",
            1));
        
        templates.add(createTemplate(genre, PromptAction.EXPAND.getCode(),
            "学术论文扩写",
            "你是一位学术写作专家。请对以下学术论文片段进行扩写和深化：\n\n" +
            "要求：\n" +
            "1. 补充相关的理论支撑和文献引用\n" +
            "2. 增加必要的论证和分析\n" +
            "3. 丰富实验细节或案例说明\n" +
            "4. 保持学术规范和逻辑连贯性\n" +
            "5. 扩写后字数增加50%-100%\n\n" +
            "原文：\n{text}\n\n" +
            "{context}\n\n" +
            "请输出扩写后的文本：",
            2));
        
        templates.add(createTemplate(genre, PromptAction.CONTINUE.getCode(),
            "学术论文续写",
            "你是一位学术论文写作助手。请根据以下内容继续撰写学术论文：\n\n" +
            "要求：\n" +
            "1. 延续前文的研究思路和论证逻辑\n" +
            "2. 保持学术写作风格的一致性\n" +
            "3. 合理推进论文结构（如从方法到结果，从结果到讨论）\n" +
            "4. 使用恰当的学术表达和过渡\n\n" +
            "已有内容：\n{text}\n\n" +
            "{context}\n\n" +
            "请继续撰写：",
            3));
        
        templates.add(createTemplate(genre, PromptAction.FIX_GRAMMAR.getCode(),
            "学术论文语法修正",
            "你是一位英文学术论文校对专家。请修正以下文本中的语法错误和表达问题：\n\n" +
            "要求：\n" +
            "1. 修正语法错误、拼写错误\n" +
            "2. 改进不地道的英文表达\n" +
            "3. 统一时态和语态\n" +
            "4. 保持学术写作规范\n" +
            "5. 不改变原文意思\n\n" +
            "原文：\n{text}\n\n" +
            "请输出修正后的文本：",
            4));
        
        templates.add(createTemplate(genre, PromptAction.TRANSLATE.getCode(),
            "学术论文翻译",
            "你是一位专业的学术翻译专家。请对以下学术文本进行翻译：\n\n" +
            "要求：\n" +
            "1. 准确传达学术概念和专业术语\n" +
            "2. 保持学术严谨性和专业性\n" +
            "3. 符合目标语言的学术表达习惯\n" +
            "4. 保留原文的逻辑结构\n\n" +
            "原文：\n{text}\n\n" +
            "{context}\n\n" +
            "请输出翻译结果：",
            5));
        
        templates.add(createTemplate(genre, PromptAction.SUMMARIZE.getCode(),
            "学术论文摘要",
            "你是一位学术论文摘要撰写专家。请为以下学术论文内容生成摘要：\n\n" +
            "要求：\n" +
            "1. 概括研究背景、目的、方法、结果和结论\n" +
            "2. 突出创新点和主要贡献\n" +
            "3. 语言简洁、信息密度高\n" +
            "4. 符合学术摘要规范（200-300字）\n\n" +
            "论文内容：\n{text}\n\n" +
            "请生成摘要：",
            6));
        
        return templates;
    }
    
    private List<PromptTemplate> createLiteratureReviewTemplates() {
        List<PromptTemplate> templates = new ArrayList<>();
        String genre = WritingGenre.LITERATURE_REVIEW.getCode();
        
        templates.add(createTemplate(genre, PromptAction.POLISH.getCode(),
            "文献综述润色",
            "你是一位文献综述写作专家。请对以下文献综述文本进行润色：\n\n" +
            "要求：\n" +
            "1. 强化文献之间的逻辑关联和对比分析\n" +
            "2. 优化综述的系统性和全面性表达\n" +
            "3. 突出研究脉络和发展趋势\n" +
            "4. 保持客观中立的评述态度\n" +
            "5. 规范文献引用格式\n\n" +
            "原文：\n{text}\n\n" +
            "{context}\n\n" +
            "请输出润色后的文本：",
            1));
        
        templates.add(createTemplate(genre, PromptAction.EXPAND.getCode(),
            "文献综述扩写",
            "你是一位文献综述撰写专家。请对以下文献综述片段进行扩写：\n\n" +
            "要求：\n" +
            "1. 补充相关研究的详细介绍\n" +
            "2. 增加不同研究之间的对比分析\n" +
            "3. 深化对研究方法和结论的评述\n" +
            "4. 梳理研究发展脉络和趋势\n" +
            "5. 指出现有研究的不足和未来方向\n\n" +
            "原文：\n{text}\n\n" +
            "{context}\n\n" +
            "请输出扩写后的文本：",
            2));
        
        templates.add(createTemplate(genre, PromptAction.CONTINUE.getCode(),
            "文献综述续写",
            "你是一位文献综述写作助手。请根据以下内容继续撰写文献综述：\n\n" +
            "要求：\n" +
            "1. 延续前文的综述思路和分类框架\n" +
            "2. 保持综述的系统性和逻辑性\n" +
            "3. 合理推进综述结构（如从早期研究到近期研究）\n" +
            "4. 保持客观评述的写作风格\n\n" +
            "已有内容：\n{text}\n\n" +
            "{context}\n\n" +
            "请继续撰写：",
            3));
        
        templates.add(createTemplate(genre, PromptAction.SUMMARIZE.getCode(),
            "文献综述总结",
            "你是一位文献综述专家。请对以下文献综述内容进行总结：\n\n" +
            "要求：\n" +
            "1. 概括主要研究领域和研究主题\n" +
            "2. 总结主流研究方法和理论框架\n" +
            "3. 归纳研究发展趋势和热点\n" +
            "4. 指出研究空白和未来方向\n" +
            "5. 语言简洁、结构清晰\n\n" +
            "综述内容：\n{text}\n\n" +
            "请生成总结：",
            4));
        
        return templates;
    }
    
    private List<PromptTemplate> createResearchReportTemplates() {
        List<PromptTemplate> templates = new ArrayList<>();
        String genre = WritingGenre.RESEARCH_REPORT.getCode();
        
        templates.add(createTemplate(genre, PromptAction.POLISH.getCode(),
            "研究报告润色",
            "你是一位研究报告撰写专家。请对以下研究报告文本进行润色：\n\n" +
            "要求：\n" +
            "1. 保持报告的专业性和可读性\n" +
            "2. 优化数据呈现和图表说明\n" +
            "3. 强化结论的说服力和实用性\n" +
            "4. 确保逻辑清晰、层次分明\n" +
            "5. 使用规范的报告语言\n\n" +
            "原文：\n{text}\n\n" +
            "{context}\n\n" +
            "请输出润色后的文本：",
            1));
        
        templates.add(createTemplate(genre, PromptAction.EXPAND.getCode(),
            "研究报告扩写",
            "你是一位研究报告写作专家。请对以下研究报告片段进行扩写：\n\n" +
            "要求：\n" +
            "1. 补充详细的研究过程和方法说明\n" +
            "2. 增加数据分析和结果解读\n" +
            "3. 丰富案例和实证材料\n" +
            "4. 强化结论的论证和建议的可行性\n" +
            "5. 保持报告的实用性和针对性\n\n" +
            "原文：\n{text}\n\n" +
            "{context}\n\n" +
            "请输出扩写后的文本：",
            2));
        
        templates.add(createTemplate(genre, PromptAction.CONTINUE.getCode(),
            "研究报告续写",
            "你是一位研究报告写作助手。请根据以下内容继续撰写研究报告：\n\n" +
            "要求：\n" +
            "1. 延续前文的研究主题和分析框架\n" +
            "2. 保持报告的专业性和实用性\n" +
            "3. 合理推进报告结构（如从现状到问题，从分析到建议）\n" +
            "4. 保持语言风格的一致性\n\n" +
            "已有内容：\n{text}\n\n" +
            "{context}\n\n" +
            "请继续撰写：",
            3));
        
        templates.add(createTemplate(genre, PromptAction.SUMMARIZE.getCode(),
            "研究报告摘要",
            "你是一位研究报告撰写专家。请为以下研究报告内容生成执行摘要：\n\n" +
            "要求：\n" +
            "1. 概括研究背景、目的和意义\n" +
            "2. 总结主要研究发现和结论\n" +
            "3. 提炼核心建议和实施要点\n" +
            "4. 语言简洁、重点突出\n" +
            "5. 适合决策者快速阅读（300-500字）\n\n" +
            "报告内容：\n{text}\n\n" +
            "请生成执行摘要：",
            4));
        
        return templates;
    }
    
    private PromptTemplate createTemplate(String genre, String action, String name, 
                                         String promptContent, int sortOrder) {
        PromptTemplate template = new PromptTemplate();
        template.setName(name);
        template.setGenre(genre);
        template.setAction(action);
        template.setPromptContent(promptContent);
        template.setIsBuiltin(true);
        template.setUserId(null);
        template.setDescription("系统内置模板");
        template.setSortOrder(sortOrder);
        template.setEnabled(true);
        return template;
    }
    
    @Override
    public PromptTemplate getTemplate(Long userId, String genre, String action, Long templateId) {
        // 如果指定了模板ID，直接查询
        if (templateId != null) {
            PromptTemplate template = promptTemplateMapper.selectById(templateId);
            if (template == null) {
                throw new BizException("模板不存在");
            }
            // 检查权限：系统模板或用户自己的模板
            if (!template.getIsBuiltin() && !template.getUserId().equals(userId)) {
                throw new BizException("无权访问该模板");
            }
            return template;
        }
        
        // 优先查找用户自定义模板
        PromptTemplate userTemplate = promptTemplateMapper.selectOne(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getUserId, userId)
                .eq(PromptTemplate::getGenre, genre)
                .eq(PromptTemplate::getAction, action)
                .eq(PromptTemplate::getEnabled, true)
                .orderByDesc(PromptTemplate::getSortOrder)
                .last("LIMIT 1")
        );
        
        if (userTemplate != null) {
            return userTemplate;
        }
        
        // 查找系统内置模板
        PromptTemplate builtinTemplate = promptTemplateMapper.selectOne(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getIsBuiltin, true)
                .eq(PromptTemplate::getGenre, genre)
                .eq(PromptTemplate::getAction, action)
                .eq(PromptTemplate::getEnabled, true)
                .orderByDesc(PromptTemplate::getSortOrder)
                .last("LIMIT 1")
        );
        
        if (builtinTemplate == null) {
            throw new BizException("未找到匹配的提示词模板");
        }
        
        return builtinTemplate;
    }
    
    @Override
    public List<PromptTemplateDTO> listTemplates(Long userId, String genre, String action) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        
        // 查询系统模板或用户自己的模板
        wrapper.and(w -> w.eq(PromptTemplate::getIsBuiltin, true)
                         .or()
                         .eq(PromptTemplate::getUserId, userId));
        
        if (genre != null && !genre.isEmpty()) {
            wrapper.eq(PromptTemplate::getGenre, genre);
        }
        
        if (action != null && !action.isEmpty()) {
            wrapper.eq(PromptTemplate::getAction, action);
        }
        
        wrapper.orderByAsc(PromptTemplate::getGenre)
               .orderByAsc(PromptTemplate::getAction)
               .orderByDesc(PromptTemplate::getSortOrder);
        
        List<PromptTemplate> templates = promptTemplateMapper.selectList(wrapper);
        
        return templates.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public PromptTemplateDTO createTemplate(Long userId, PromptTemplateCreateRequest request) {
        // 验证枚举值
        WritingGenre.fromCode(request.getGenre());
        PromptAction.fromCode(request.getAction());
        
        PromptTemplate template = new PromptTemplate();
        BeanUtils.copyProperties(request, template);
        template.setUserId(userId);
        template.setIsBuiltin(false);
        
        promptTemplateMapper.insert(template);
        
        return convertToDTO(template);
    }
    
    @Override
    @Transactional
    public PromptTemplateDTO updateTemplate(Long userId, PromptTemplateUpdateRequest request) {
        PromptTemplate template = promptTemplateMapper.selectById(request.getId());
        
        if (template == null) {
            throw new BizException("模板不存在");
        }
        
        // 只能修改自己的模板
        if (template.getIsBuiltin() || !template.getUserId().equals(userId)) {
            throw new BizException("无权修改该模板");
        }
        
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getPromptContent() != null) {
            template.setPromptContent(request.getPromptContent());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getSortOrder() != null) {
            template.setSortOrder(request.getSortOrder());
        }
        if (request.getEnabled() != null) {
            template.setEnabled(request.getEnabled());
        }
        
        promptTemplateMapper.updateById(template);
        
        return convertToDTO(template);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Long userId, Long templateId) {
        PromptTemplate template = promptTemplateMapper.selectById(templateId);
        
        if (template == null) {
            throw new BizException("模板不存在");
        }
        
        // 只能删除自己的模板
        if (template.getIsBuiltin() || !template.getUserId().equals(userId)) {
            throw new BizException("无权删除该模板");
        }
        
        promptTemplateMapper.deleteById(templateId);
    }
    
    @Override
    public PromptTemplateDTO getTemplateDetail(Long userId, Long templateId) {
        PromptTemplate template = promptTemplateMapper.selectById(templateId);
        
        if (template == null) {
            throw new BizException("模板不存在");
        }
        
        // 检查权限
        if (!template.getIsBuiltin() && !template.getUserId().equals(userId)) {
            throw new BizException("无权访问该模板");
        }
        
        return convertToDTO(template);
    }
    
    private PromptTemplateDTO convertToDTO(PromptTemplate template) {
        PromptTemplateDTO dto = new PromptTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        
        try {
            WritingGenre genre = WritingGenre.fromCode(template.getGenre());
            dto.setGenreName(genre.getName());
            
            PromptAction action = PromptAction.fromCode(template.getAction());
            dto.setActionName(action.getName());
        } catch (Exception e) {
            log.warn("转换枚举名称失败: {}", e.getMessage());
        }
        
        return dto;
    }
}

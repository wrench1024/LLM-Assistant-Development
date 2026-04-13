<script setup lang="ts">
import { ref, computed } from 'vue'
import { marked } from 'marked'
import { Document, Packer, Paragraph, TextRun, TableOfContents } from 'docx'
import { saveAs } from 'file-saver'
import { checkText, type CheckTextResponse } from '@/api/sensitive'
import { templateAPI, type PromptTemplate } from '@/api/write'
import { ElMessage } from 'element-plus'
import SensitiveTextHighlight from '@/components/SensitiveTextHighlight.vue'
import TemplateManager from '@/components/TemplateManager.vue'

const content = ref('')
const instruction = ref('polish')
const customContext = ref('')
const isProcessing = ref(false)
const resultContent = ref('')

// 模板管理相关
const showTemplateManager = ref(false)
const selectedTemplate = ref<PromptTemplate | null>(null)
const selectedGenre = ref('academic_paper')
const selectedAction = ref('polish')

// 敏感词检测相关
const sensitiveCheckResult = ref<CheckTextResponse | null>(null)
const isCheckingSensitive = ref(false)

const wordCount = computed(() => {
  // Count non-whitespace characters
  return content.value.replace(/\s+/g, '').length
})
// ... (keep existing refs)

// 检测敏感词
const checkSensitiveWords = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请先输入内容')
    return
  }
  
  isCheckingSensitive.value = true
  try {
    const res = await checkText({ text: content.value })
    sensitiveCheckResult.value = res.data
    
    if (res.data.hasSensitive) {
      ElMessage.warning(`检测到 ${res.data.matches.length} 个敏感词`)
    } else {
      ElMessage.success('未检测到敏感词')
    }
  } catch (error) {
    ElMessage.error('敏感词检测失败')
  } finally {
    isCheckingSensitive.value = false
  }
}

// Export to Word function
// Export to Word function
const exportToWord = async () => {
  if (!content.value.trim()) {
    alert('内容为空，无法导出')
    return
  }

  const lines = content.value.split('\n').filter(line => line.trim())
  const docBody: (Paragraph | TableOfContents)[] = []

  // 1. Add TOC
  docBody.push(
    new Paragraph({
      children: [
        new TextRun({
          text: "目录",
          bold: true,
          size: 32,
          font: "Songti SC",
        }),
      ],
      alignment: "center",
      spacing: { after: 400 },
    }),
    new TableOfContents("Summary", {
      hyperlink: true,
      headingStyleRange: "1-5",
    }),
    new Paragraph({
      children: [new TextRun({ text: "", break: 1 })], // Page break after TOC? docx doesn't always support easy page break in flow, but we can try
      pageBreakBefore: true,
    })
  )

  // 2. Parse Content
  lines.forEach((line, index) => {
    const trimmed = line.trim()
    let headingLevel: any = undefined
    let isTitle = false

    // Relaxed Regex for Headings
    const isHeadingPattern = 
        /^(#+\s)/.test(trimmed) || 
        /^第[一二三四五六七八九十\d]+章/.test(trimmed) ||
        /^[\d]+[\.、]/.test(trimmed) || // 1.xxx or 1、xxx (allow no space)
        /^[一二三四五六七八九十]+[\.、]/.test(trimmed) || // 一、xxx (allow no space)
        /^(摘要|引言|目录|前言|背景|方法|结果|讨论|结论|参考文献|致谢|附录|概述|现状分析|问题识别|建议方案|预期成效)$/.test(trimmed)

    // Heuristic 1: First line is Title (unless it looks like a numbered heading)
    if (index === 0 && !isHeadingPattern) {
      isTitle = true
    }
    // Heuristic 2: Headings
    else if (
        isHeadingPattern ||
        (trimmed.length < 20 && !/[。；，：]$/.test(trimmed) && index < 5 && index > 0) // Short lines early on, excluding colons
    ) {
       headingLevel = "Heading1"
       // Strip markdown chars if present
       line = line.replace(/^(#+\s)/, '')
    }

    const para = new Paragraph({
      children: [
        new TextRun({
          text: line,
          font: "Songti SC",
          size: isTitle ? 32 : (headingLevel ? 28 : 24), // Title=16pt, H1=14pt, Body=12pt (Adjusted sizes)
          bold: isTitle || !!headingLevel,
          color: (headingLevel && !isTitle) ? "2E74B5" : "000000" // Optional: Blue for headings for visibility
        }),
      ],
      heading: isTitle ? "Title" : headingLevel, // Correct mapping
      spacing: {
        before: isTitle ? 0 : (headingLevel ? 400 : 0),
        after: 200,
      },
      alignment: isTitle ? "center" : "left",
      outlineLevel: headingLevel === "Heading1" ? 0 : undefined // Ensure it shows in TOC
    })

    docBody.push(para)
  })

  const doc = new Document({
    features: {
      updateFields: true, // Auto-update TOC on open
    },
    sections: [{
      properties: {},
      children: docBody,
    }],
  })

  try {
    const blob = await Packer.toBlob(doc)
    saveAs(blob, `academic_draft_${new Date().toISOString().slice(0,10)}.docx`)
  } catch (error) {
    console.error('Export failed:', error)
    alert('导出失败')
  }
}

// ... (keep existing logic)
// In template:
// <div class="toolbar">
//   <button class="tool-btn" @click="showTemplateSelector = true">📂 使用模版</button>
//   <button class="tool-btn" @click="exportToWord">💾 导出 Word</button>
//   <div class="spacer"></div>

// 选中文本相关状态
const selectedText = ref('')
const selectionStart = ref(0)
const selectionEnd = ref(0)
const showSelectionToolbar = ref(false)
const toolbarPosition = ref({ top: 0, left: 0 })
const editorRef = ref<HTMLTextAreaElement | null>(null)

const tools = [
  { label: '学术润色', value: 'polish', icon: '✨' },
  { label: '智能扩写', value: 'expand', icon: '📝' },
  { label: '续写段落', value: 'continue', icon: '⏩' },
  { label: '语法纠错', value: 'fix_grammar', icon: '✅' }
]

const cancelProcessing = () => {
  isProcessing.value = false
  resultContent.value = ''
}

const processText = async () => {
  if (!content.value.trim()) return
  
  isProcessing.value = true
  resultContent.value = ''
  
  try {
    // 使用新版 API（支持文体和模板）
    await templateAPI.processWithTemplate(
      {
        text: content.value,
        genre: selectedGenre.value,
        action: selectedAction.value,
        context: customContext.value,
        templateId: selectedTemplate.value?.id
      },
      (text) => {
        resultContent.value += text
      },
      () => {
        isProcessing.value = false
      },
      (error) => {
        resultContent.value = `Error: ${error.message}`
        isProcessing.value = false
      }
    )
  } catch (e: any) {
    resultContent.value = `Error: ${e}`
    isProcessing.value = false
  }
}

// 选择模板
const handleTemplateSelect = (template: PromptTemplate) => {
  selectedTemplate.value = template
  selectedGenre.value = template.genre
  selectedAction.value = template.action
  ElMessage.success(`已选择模板：${template.name}`)
}


// 处理选中文本的 AI 操作
const processSelection = async (tool: string) => {
  if (!selectedText.value.trim()) return
  
  showSelectionToolbar.value = false
  isProcessing.value = true
  resultContent.value = ''
  
  try {
    await templateAPI.processWithTemplate(
      {
        text: selectedText.value,
        genre: selectedGenre.value,
        action: tool,
        context: customContext.value,
        templateId: selectedTemplate.value?.id
      },
      (text) => {
        resultContent.value += text
      },
      () => {
        isProcessing.value = false
      },
      (error) => {
        resultContent.value = `Error: ${error.message}`
        isProcessing.value = false
      }
    )
  } catch (e: any) {
    if (e.name === 'AbortError') {
      console.log('Request aborted')
    } else {
      resultContent.value = `Error: ${e}`
    }
    isProcessing.value = false
  }
}

// 监听选区变化
const handleSelect = () => {
  const textarea = editorRef.value
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  
  if (start !== end) {
    selectedText.value = content.value.substring(start, end)
    selectionStart.value = start
    selectionEnd.value = end
    
    // 计算工具条位置 (简化处理：显示在编辑器上方)
    const rect = textarea.getBoundingClientRect()
    toolbarPosition.value = {
      top: rect.top - 50,
      left: rect.left + (rect.width / 2) - 100
    }
    showSelectionToolbar.value = true
  } else {
    showSelectionToolbar.value = false
    selectedText.value = ''
  }
}

// 应用结果到选中区域
const applyResultToSelection = () => {
  if (selectionStart.value !== selectionEnd.value) {
    // 替换选中区域
    content.value = 
      content.value.substring(0, selectionStart.value) + 
      resultContent.value + 
      content.value.substring(selectionEnd.value)
  }
  resultContent.value = ''
  selectedText.value = ''
}

const stopGeneration = () => {
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
  }
  isProcessing.value = false
  // Do not clear resultContent, keep it for review
}

const applyResult = () => {
  if (selectedText.value) {
    applyResultToSelection()
  } else {
    content.value = resultContent.value
    resultContent.value = ''
  }
}

// Templates Logic
const showTemplateSelector = ref(false)
const templates = [
  { 
    id: 'paper', 
    name: '学术论文 (Academic Paper)', 
    structure: '标题\n\n摘要\n    [在此处撰写摘要]\n\n引言\n    [研究背景与目的]\n\n方法\n    [描述研究方法]\n\n结果\n    [展示主要发现]\n\n讨论\n    [结果分析与意义]\n\n结论\n    [总结全文]'
  },
  { 
    id: 'report', 
    name: '研究报告 (Research Report)', 
    structure: '研究报告\n\n1. 概述\n\n2. 现状分析\n\n3. 问题识别\n\n4. 建议方案\n\n5. 预期成效' 
  },
  { 
    id: 'review', 
    name: '文献综述 (Literature Review)', 
    structure: '文献综述\n\n引言\n\n关键概念\n\n现有研究进展\n\n主要争论焦点\n\n研究不足与展望' 
  }
]

const applyTemplate = (tpl: any) => {
  if (content.value && !confirm('当前编辑器已有内容，是否覆盖？')) return
  content.value = tpl.structure
  showTemplateSelector.value = false
}

// Preprocess markdown to fix common formatting issues
const preprocessMarkdown = (content: string): string => {
  return content
    .replace(/^(#{1,6})(\S)/gm, '$1 $2')
    .replace(/([^\n])\n([^\n#\-\*\d])/g, '$1\n\n$2')
    .replace(/^(\s*[-*])(\S)/gm, '$1 $2')
    .replace(/^(\s*\d+\.)(\S)/gm, '$1 $2')
}

// Computed property for rendered markdown result
const renderedResult = computed(() => {
  if (!resultContent.value) return ''
  const processed = preprocessMarkdown(resultContent.value)
  return marked(processed) as string
})
</script>

<template>
  <div class="writing-container">
    <div class="editor-area">
      <div class="toolbar">
        <button class="tool-btn" @click="showTemplateManager = true">📚 模板管理</button>
        <button class="tool-btn" @click="showTemplateSelector = true">📂 使用模版</button>
        <button class="tool-btn" @click="exportToWord">💾 导出 Word</button>
        <button class="tool-btn" @click="checkSensitiveWords" :disabled="isCheckingSensitive">
          {{ isCheckingSensitive ? '检测中...' : '🔍 敏感词检测' }}
        </button>
        <div class="spacer"></div>
        <span v-if="selectedTemplate" class="selected-template">
          当前模板：{{ selectedTemplate.name }}
        </span>
        <span class="word-count">字数: {{ wordCount }}</span>
      </div>
      
      <!-- 敏感词检测结果提示 -->
      <div v-if="sensitiveCheckResult?.hasSensitive" class="sensitive-alert">
        <div class="alert-header">
          <span>⚠️ 检测到 {{ sensitiveCheckResult.matches.length }} 个敏感词</span>
          <button @click="sensitiveCheckResult = null" class="close-alert">✕</button>
        </div>
        <div class="sensitive-words-list">
          <span 
            v-for="(match, index) in sensitiveCheckResult.matches" 
            :key="index"
            class="sensitive-tag"
          >
            {{ match.word }}
          </span>
        </div>
      </div>
      
      <div v-if="sensitiveCheckResult?.hasSensitive" class="editor-with-highlight">
        <div class="highlight-label">内容预览（敏感词已标红）：</div>
        <div class="highlight-content">
          <SensitiveTextHighlight
            :text="content"
            :matches="sensitiveCheckResult.matches"
          />
        </div>
      </div>
      
      <textarea 
        v-else
        ref="editorRef"
        v-model="content" 
        placeholder="在此输入您的学术文本... 或点击上方'使用模版'开始。\n\n💡 提示：选中任意文字后，会出现快捷工具条。"
        class="main-editor"
        @mouseup="handleSelect"
        @keyup="handleSelect"
      ></textarea>
      
      <!-- 选中文本时的悬浮工具条 -->
      <Teleport to="body">
        <div 
          v-if="showSelectionToolbar && !isProcessing" 
          class="selection-toolbar"
          :style="{ top: toolbarPosition.top + 'px', left: toolbarPosition.left + 'px' }"
        >
          <button @click="processSelection('polish')" title="润色">✨ 润色</button>
          <button @click="processSelection('fix_grammar')" title="纠错">✅ 纠错</button>
          <button @click="processSelection('expand')" title="扩写">📝 扩写</button>
        </div>
      </Teleport>
      
      <div v-if="resultContent" class="result-preview">
        <div class="preview-header">
          <span>AI 建议结果</span>
          <div>
            <button v-if="isProcessing" @click="stopGeneration" class="stop-btn">⏹ 停止</button>
            <button v-else @click="applyResult" class="apply-btn">采纳</button>
            <button @click="cancelProcessing" class="cancel-btn">取消</button>
          </div>
        </div>
        <div class="preview-body markdown-body" v-html="renderedResult"></div>
      </div>
    </div>
    
    <div class="tools-sidebar">
      <h3>AI 写作工具</h3>
      
      <div class="tool-grid">
        <div 
          v-for="tool in tools" 
          :key="tool.value"
          class="tool-card"
          :class="{ active: instruction === tool.value }"
          @click="instruction = tool.value"
        >
          <span class="icon">{{ tool.icon }}</span>
          <span class="label">{{ tool.label }}</span>
        </div>
      </div>
      
      <div class="context-input">
        <label>额外背景/要求 (可选)</label>
        <textarea v-model="customContext" placeholder="例如：使用更正式的语气..."></textarea>
      </div>
      
      <button 
        class="action-btn" 
        :disabled="isProcessing || !content"
        @click="processText"
      >
        {{ isProcessing ? '处理中...' : '开始处理' }}
      </button>
    </div>
    <!-- Template Selector Dialog -->
    <div v-if="showTemplateSelector" class="modal-overlay" @click.self="showTemplateSelector = false">
      <div class="modal">
        <h3>选择写作模版</h3>
        <div class="template-list">
          <div 
            v-for="tpl in templates" 
            :key="tpl.id" 
            class="template-item"
            @click="applyTemplate(tpl)"
          >
            <h4>{{ tpl.name }}</h4>
            <pre>{{ tpl.structure.slice(0, 50) }}...</pre>
          </div>
        </div>
        <button class="close-btn" @click="showTemplateSelector = false">取消</button>
      </div>
    </div>
    
    <!-- 模板管理对话框 -->
    <TemplateManager 
      v-if="showTemplateManager"
      @close="showTemplateManager = false"
      @select="handleTemplateSelect"
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.tool-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tool-btn:hover {
  border-color: #667eea;
  color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  transform: translateY(-1px);
}

.spacer {
  flex: 1;
}

.word-count {
  color: #909399;
  font-size: 13px;
  padding: 4px 12px;
  background: rgba(144, 147, 153, 0.1);
  border-radius: 20px;
}

.selected-template {
  color: #667eea;
  font-size: 13px;
  padding: 4px 12px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 20px;
  font-weight: 500;
}

.writing-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
}

.editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 40px;
  position: relative;
}

.main-editor {
  flex: 1;
  width: 100%;
  border: none;
  padding: 30px;
  font-size: 16px;
  line-height: 1.8;
  resize: none;
  border-radius: 16px;
  outline: none;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  font-family: 'Inter', 'Noto Sans SC', system-ui, sans-serif;
}

.main-editor:focus {
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.tools-sidebar {
  width: 340px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-left: 1px solid rgba(255, 255, 255, 0.5);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.05);
}

.tools-sidebar h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tools-sidebar h3::before {
  content: '🤖';
}

.tool-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.tool-card {
  background: linear-gradient(145deg, #ffffff, #f8f9fb);
  border: 2px solid transparent;
  padding: 18px 12px;
  border-radius: 14px;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.tool-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 14px;
  padding: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s;
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.18);
}

.tool-card:hover::before {
  opacity: 1;
}

.tool-card.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.tool-card.active .label {
  color: white;
}

.tool-card.active::before {
  opacity: 0;
}

.icon {
  display: block;
  font-size: 24px;
  margin-bottom: 5px;
}

.context-input textarea {
  width: 100%;
  height: 80px;
  margin-top: 5px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.action-btn {
  width: 100%;
  padding: 14px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
  letter-spacing: 0.5px;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.45);
}

.action-btn:active:not(:disabled) {
  transform: translateY(0);
}

.action-btn:disabled {
  background: linear-gradient(135deg, #c0c4cc, #909399);
  box-shadow: none;
  cursor: not-allowed;
}

.result-preview {
  position: absolute;
  top: 40px;
  right: 40px;
  left: 40px;
  bottom: 40px;
  background: white;
  border: none;
  border-radius: 20px;
  padding: 28px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(102, 126, 234, 0.25);
  animation: previewSlideIn 0.3s ease-out;
}

@keyframes previewSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f2f5;
  padding-bottom: 16px;
}

.preview-header > span {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-header > span::before {
  content: '✨';
}

.preview-body {
  flex: 1;
  overflow-y: auto;
  font-size: 16px;
  line-height: 1.8;
  padding: 10px 0;
}

/* Markdown Styles for WritingView */
.preview-body.markdown-body :deep(h1),
.preview-body.markdown-body :deep(h2),
.preview-body.markdown-body :deep(h3) {
  margin-top: 16px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
}

.preview-body.markdown-body :deep(h1) { font-size: 1.4em; }
.preview-body.markdown-body :deep(h2) { font-size: 1.25em; }
.preview-body.markdown-body :deep(h3) { font-size: 1.1em; color: #409eff; }

.preview-body.markdown-body :deep(p) {
  margin: 10px 0;
}

.preview-body.markdown-body :deep(ul),
.preview-body.markdown-body :deep(ol) {
  padding-left: 20px;
  margin: 10px 0;
}

.preview-body.markdown-body :deep(li) {
  margin: 6px 0;
}

.preview-body.markdown-body :deep(strong) {
  font-weight: 600;
  color: #303133;
}

.preview-body.markdown-body :deep(code) {
  background: #f5f7fa;
  padding: 2px 5px;
  border-radius: 3px;
  font-size: 0.9em;
}

.apply-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 10px;
  margin-right: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(17, 153, 142, 0.3);
}

.apply-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(17, 153, 142, 0.4);
}
.stop-btn {
  background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 10px;
  margin-right: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 126, 95, 0.3);
}

.stop-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 126, 95, 0.4);
}

.cancel-btn {
  background: #f0f2f5;
  color: #606266;
  border: none;
  padding: 10px 24px;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  background: #e4e7ed;
  color: #303133;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
}

.modal h3 {
  margin-top: 0;
  margin-bottom: 20px;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.template-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-item:hover {
  background: #f5f7fa;
  border-color: #409eff;
}

.template-item h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.template-item pre {
  margin: 0;
  color: #909399;
  font-size: 12px;
  background: transparent;
  padding: 0;
}

.close-btn {
  width: 100%;
  padding: 10px;
  background: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* 选中文本时的悬浮工具条样式 */
.selection-toolbar {
  position: fixed;
  z-index: 9999;
  display: flex;
  gap: 4px;
  padding: 6px 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  animation: toolbarFadeIn 0.2s ease-out;
}

@keyframes toolbarFadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.selection-toolbar button {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  white-space: nowrap;
}

.selection-toolbar button:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-1px);
}

/* 敏感词检测相关样式 */
.sensitive-alert {
  margin-bottom: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
  border: 1px solid #ffccc7;
  border-radius: 12px;
  animation: alertSlideIn 0.3s ease-out;
}

@keyframes alertSlideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
  color: #cf1322;
}

.close-alert {
  background: none;
  border: none;
  font-size: 18px;
  color: #cf1322;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-alert:hover {
  background: rgba(207, 19, 34, 0.1);
}

.sensitive-words-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sensitive-tag {
  padding: 4px 12px;
  background: #fff;
  border: 1px solid #ff4d4f;
  border-radius: 16px;
  color: #ff4d4f;
  font-size: 13px;
  font-weight: 500;
}

.editor-with-highlight {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.highlight-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f2f5;
}

.highlight-content {
  flex: 1;
  overflow-y: auto;
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>

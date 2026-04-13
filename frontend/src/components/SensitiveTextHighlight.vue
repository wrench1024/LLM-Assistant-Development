<template>
  <div class="sensitive-text-container">
    <span
      v-for="(segment, index) in segments"
      :key="index"
      :class="{ 'sensitive-word': segment.isSensitive }"
      :title="segment.isSensitive ? `敏感词类型: ${getTypeLabel(segment.type)}` : ''"
    >
      {{ segment.text }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { SensitiveWordMatch } from '@/api/sensitive'

interface Props {
  text: string
  matches: SensitiveWordMatch[]
}

const props = defineProps<Props>()

interface TextSegment {
  text: string
  isSensitive: boolean
  type?: number
}

// 将文本分割成普通文本和敏感词片段
const segments = computed<TextSegment[]>(() => {
  if (!props.text || !props.matches || props.matches.length === 0) {
    return [{ text: props.text || '', isSensitive: false }]
  }

  const result: TextSegment[] = []
  let lastIndex = 0

  // 按起始位置排序
  const sortedMatches = [...props.matches].sort((a, b) => a.startIndex - b.startIndex)

  sortedMatches.forEach((match) => {
    // 添加敏感词之前的普通文本
    if (match.startIndex > lastIndex) {
      result.push({
        text: props.text.substring(lastIndex, match.startIndex),
        isSensitive: false
      })
    }

    // 添加敏感词
    result.push({
      text: match.word,
      isSensitive: true,
      type: match.type
    })

    lastIndex = match.endIndex
  })

  // 添加最后的普通文本
  if (lastIndex < props.text.length) {
    result.push({
      text: props.text.substring(lastIndex),
      isSensitive: false
    })
  }

  return result
})

// 获取敏感词类型标签
const getTypeLabel = (type?: number): string => {
  const typeMap: Record<number, string> = {
    1: '政治敏感',
    2: '色情暴力',
    3: '广告营销',
    4: '其他'
  }
  return typeMap[type || 4] || '未知'
}
</script>

<style scoped>
.sensitive-text-container {
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.sensitive-word {
  color: #ff4d4f;
  font-weight: 600;
  background-color: rgba(255, 77, 79, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
  cursor: help;
  transition: all 0.2s;
}

.sensitive-word:hover {
  background-color: rgba(255, 77, 79, 0.2);
}
</style>

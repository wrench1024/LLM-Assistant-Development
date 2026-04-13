<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { templateAPI, type PromptTemplate, type GenreOption, type ActionOption } from '@/api/write'
import { ElMessage, ElMessageBox } from 'element-plus'

const emit = defineEmits(['close', 'select'])

const genres = ref<GenreOption[]>([])
const actions = ref<ActionOption[]>([])
const templates = ref<PromptTemplate[]>([])
const loading = ref(false)

const filterGenre = ref('')
const filterAction = ref('')

const showCreateDialog = ref(false)
const formData = ref({
  name: '',
  genre: '',
  action: '',
  promptContent: '',
  description: '',
  sortOrder: 10,
  enabled: true
})

onMounted(async () => {
  await loadGenres()
  await loadActions()
  await loadTemplates()
})

const loadGenres = async () => {
  try {
    genres.value = await templateAPI.getGenres()
  } catch (error) {
    ElMessage.error('加载文体类型失败')
  }
}

const loadActions = async () => {
  try {
    actions.value = await templateAPI.getActions()
  } catch (error) {
    ElMessage.error('加载动作类型失败')
  }
}

const loadTemplates = async () => {
  loading.value = true
  try {
    templates.value = await templateAPI.listTemplates(
      filterGenre.value || undefined,
      filterAction.value || undefined
    )
  } catch (error) {
    ElMessage.error('加载模板失败')
  } finally {
    loading.value = false
  }
}

const createTemplate = async () => {
  if (!formData.value.name || !formData.value.genre || !formData.value.action || !formData.value.promptContent) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    await templateAPI.createTemplate(formData.value)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    resetForm()
    await loadTemplates()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const deleteTemplate = async (template: PromptTemplate) => {
  if (template.isBuiltin) {
    ElMessage.warning('系统模板不能删除')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await templateAPI.deleteTemplate(template.id)
    ElMessage.success('删除成功')
    await loadTemplates()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const selectTemplate = (template: PromptTemplate) => {
  emit('select', template)
  emit('close')
}

const resetForm = () => {
  formData.value = {
    name: '',
    genre: '',
    action: '',
    promptContent: '',
    description: '',
    sortOrder: 10,
    enabled: true
  }
}

const filteredTemplates = computed(() => {
  return templates.value
})
</script>

<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>📚 提示词模板管理</h2>
        <button @click="$emit('close')" class="close-btn">✕</button>
      </div>

      <div class="modal-body">
        <!-- 筛选器 -->
        <div class="filters">
          <el-select v-model="filterGenre" placeholder="所有文体" clearable @change="loadTemplates" style="width: 180px">
            <el-option label="所有文体" value="" />
            <el-option v-for="genre in genres" :key="genre.code" :label="genre.name" :value="genre.code" />
          </el-select>

          <el-select v-model="filterAction" placeholder="所有动作" clearable @change="loadTemplates" style="width: 180px">
            <el-option label="所有动作" value="" />
            <el-option v-for="action in actions" :key="action.code" :label="action.name" :value="action.code" />
          </el-select>

          <button @click="showCreateDialog = true" class="create-btn">
            ➕ 创建自定义模板
          </button>
        </div>

        <!-- 模板列表 -->
        <div v-loading="loading" class="template-list">
          <div 
            v-for="template in filteredTemplates" 
            :key="template.id"
            class="template-card"
            :class="{ builtin: template.isBuiltin }"
          >
            <div class="template-header">
              <div class="template-title">
                <h3>{{ template.name }}</h3>
                <span class="badge" :class="{ builtin: template.isBuiltin }">
                  {{ template.isBuiltin ? '系统' : '自定义' }}
                </span>
              </div>
              <div class="template-actions">
                <button @click="selectTemplate(template)" class="use-btn">使用</button>
                <button 
                  v-if="!template.isBuiltin" 
                  @click="deleteTemplate(template)" 
                  class="delete-btn"
                >
                  删除
                </button>
              </div>
            </div>
            <div class="template-info">
              <span class="tag">{{ template.genreName }}</span>
              <span class="tag">{{ template.actionName }}</span>
            </div>
            <div class="template-description">
              {{ template.description || '暂无描述' }}
            </div>
          </div>

          <div v-if="filteredTemplates.length === 0" class="empty-state">
            <p>暂无模板</p>
          </div>
        </div>
      </div>

      <!-- 创建模板对话框 -->
      <el-dialog v-model="showCreateDialog" title="创建自定义模板" width="600px">
        <el-form :model="formData" label-width="100px">
          <el-form-item label="模板名称" required>
            <el-input v-model="formData.name" placeholder="例如：我的论文润色模板" />
          </el-form-item>

          <el-form-item label="文体类型" required>
            <el-select v-model="formData.genre" placeholder="请选择文体" style="width: 100%">
              <el-option v-for="genre in genres" :key="genre.code" :label="genre.name" :value="genre.code" />
            </el-select>
          </el-form-item>

          <el-form-item label="动作类型" required>
            <el-select v-model="formData.action" placeholder="请选择动作" style="width: 100%">
              <el-option v-for="action in actions" :key="action.code" :label="action.name" :value="action.code" />
            </el-select>
          </el-form-item>

          <el-form-item label="提示词内容" required>
            <el-input 
              v-model="formData.promptContent" 
              type="textarea" 
              :rows="8"
              placeholder="输入提示词内容，使用 {text} 和 {context} 作为占位符"
            />
            <div class="hint">提示：使用 {text} 表示用户输入的文本，{context} 表示上下文信息</div>
          </el-form-item>

          <el-form-item label="模板描述">
            <el-input v-model="formData.description" placeholder="简要描述这个模板的用途" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="createTemplate">创建</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 900px;
  max-width: 95vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  border-bottom: 2px solid #f0f2f5;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f5f7fa;
  color: #303133;
}

.modal-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 20px 28px;
}

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.create-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
  margin-left: auto;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.template-list {
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
  padding: 4px;
}

.template-card {
  background: white;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  cursor: pointer;
}

.template-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.template-card.builtin {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.template-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.template-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #e4e7ed;
  color: #606266;
}

.badge.builtin {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.template-actions {
  display: flex;
  gap: 8px;
}

.use-btn, .delete-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.use-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.use-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(17, 153, 142, 0.3);
}

.delete-btn {
  background: #f5f7fa;
  color: #909399;
}

.delete-btn:hover {
  background: #ff4d4f;
  color: white;
}

.template-info {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.tag {
  padding: 4px 10px;
  background: #f0f2f5;
  border-radius: 6px;
  font-size: 12px;
  color: #606266;
}

.template-description {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.hint {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>

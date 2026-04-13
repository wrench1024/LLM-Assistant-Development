<template>
  <div class="sensitive-word-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">敏感词管理</span>
          <div class="actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加敏感词
            </el-button>
            <el-button @click="handleRefreshCache">
              <el-icon><Refresh /></el-icon>
              刷新缓存
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索敏感词"
          clearable
          style="width: 300px"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="searchForm.type"
          placeholder="敏感词类型"
          clearable
          style="width: 150px"
          @change="handleSearch"
        >
          <el-option label="政治敏感" :value="1" />
          <el-option label="色情暴力" :value="2" />
          <el-option label="广告营销" :value="3" />
          <el-option label="其他" :value="4" />
        </el-select>
        <el-select
          v-model="searchForm.enabled"
          placeholder="状态"
          clearable
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" min-width="150">
          <template #default="{ row }">
            <span class="sensitive-word-text">{{ row.word }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedIds.length > 0" class="batch-actions">
        <span>已选择 {{ selectedIds.length }} 项</span>
        <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="form.word" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="政治敏感" :value="1" />
            <el-option label="色情暴力" :value="2" />
            <el-option label="广告营销" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-radio-group v-model="form.enabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  pageSensitiveWords,
  addSensitiveWord,
  updateSensitiveWord,
  deleteSensitiveWord,
  batchDeleteSensitiveWords,
  refreshCache,
  type SensitiveWord
} from '@/api/sensitive'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: undefined as number | undefined,
  enabled: undefined as number | undefined
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref<SensitiveWord[]>([])
const loading = ref(false)
const selectedIds = ref<number[]>([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加敏感词')
const formRef = ref<FormInstance>()
const form = reactive({
  id: undefined as number | undefined,
  word: '',
  type: 4,
  enabled: 1
})

// 表单验证规则
const rules: FormRules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

// 获取类型标签
const getTypeLabel = (type: number): string => {
  const typeMap: Record<number, string> = {
    1: '政治敏感',
    2: '色情暴力',
    3: '广告营销',
    4: '其他'
  }
  return typeMap[type] || '未知'
}

// 获取类型标签颜色
const getTypeTagType = (type: number): string => {
  const typeMap: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'info',
    4: ''
  }
  return typeMap[type] || ''
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await pageSensitiveWords({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      type: searchForm.type,
      enabled: searchForm.enabled
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.type = undefined
  searchForm.enabled = undefined
  handleSearch()
}

// 添加
const handleAdd = () => {
  dialogTitle.value = '添加敏感词'
  form.id = undefined
  form.word = ''
  form.type = 4
  form.enabled = 1
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: SensitiveWord) => {
  dialogTitle.value = '编辑敏感词'
  form.id = row.id
  form.word = row.word
  form.type = row.type
  form.enabled = row.enabled
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: SensitiveWord) => {
  try {
    await ElMessageBox.confirm('确定要删除该敏感词吗？', '提示', {
      type: 'warning'
    })
    await deleteSensitiveWord(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个敏感词吗？`, '提示', {
      type: 'warning'
    })
    await batchDeleteSensitiveWords(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (form.id) {
        await updateSensitiveWord(form.id, {
          word: form.word,
          type: form.type,
          enabled: form.enabled
        })
        ElMessage.success('更新成功')
      } else {
        await addSensitiveWord({
          word: form.word,
          type: form.type,
          enabled: form.enabled
        })
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(form.id ? '更新失败' : '添加失败')
    }
  })
}

// 刷新缓存
const handleRefreshCache = async () => {
  try {
    await refreshCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 选择变化
const handleSelectionChange = (selection: SensitiveWord[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 分页变化
const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sensitive-word-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.sensitive-word-text {
  color: #ff4d4f;
  font-weight: 600;
}

.batch-actions {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>

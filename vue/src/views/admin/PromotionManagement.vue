<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <TrendCharts />
        </el-icon>
        <span>晋级名单管理</span>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 160px" @clear="load" />
        <el-button type="success" @click="load" :icon="Search">检索晋级名单</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="danger" @click="handleBatchCancel" :disabled="!selectedIds.length">批量取消晋级</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="competitionId" label="竞赛ID" width="100" align="center" />
        <el-table-column prop="studentId" label="学生ID" width="100" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="primary">已晋级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="晋级备注/评价" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="晋级确认时间" width="180" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleCancelPromotion(row)">取消晋级</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @change="load" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, TrendCharts } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')

const reset = () => { searchCompId.value = ''; load() }

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/registration/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, competitionId: searchCompId.value || undefined, status: 3 }
    }) as any
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleCancelPromotion = (row: any) => {
  ElMessageBox.confirm('确定取消该学生的晋级状态？状态将恢复为“已参赛”。', '警告', { type: 'warning' }).then(async () => {
    await request.put('/registration', { id: row.id, status: 1 })
    ElMessage.success('已恢复为参赛状态')
    load()
  })
}

const selectedIds = ref<number[]>([])
const handleSelectionChange = (val: any[]) => {
    selectedIds.value = val.map(item => item.id)
}

const handleBatchCancel = () => {
    ElMessageBox.confirm(`确定取消这 ${selectedIds.value.length} 个项目的晋级状态吗？`, '批量操作').then(async () => {
        for (const id of selectedIds.value) {
            await request.put('/registration', { id, status: 1 })
        }
        ElMessage.success('批量操作完成')
        load()
    })
}

onMounted(load)
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.title-icon {
  font-size: 22px;
  color: #16a34a;
}

.table-card {
  border-radius: 12px;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.data-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
}

.pagination-bar {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

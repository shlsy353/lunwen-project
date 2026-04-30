<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <Medal />
        </el-icon>
        <span>获奖名单总览</span>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 160px" />
        <el-input v-model="searchStudId" placeholder="学生ID..." clearable style="width: 160px" />
        <el-button type="success" @click="load" :icon="Search">检索结果</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="competitionId" label="竞赛ID" width="100" align="center" />
        <el-table-column prop="studentId" label="学生ID" width="100" align="center" />
        <el-table-column prop="name" label="奖项名称" min-width="150" />
        <el-table-column label="获奖等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="awardType(row.awardLevel)">{{ row.awardLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="详细说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="获奖日期" width="180" />
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
import { Search, Medal } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')
const searchStudId = ref('')

const awardType = (level: string) => {
  const map: any = { '特等奖': 'danger', '一等奖': 'warning', '二等奖': '', '三等奖': 'success', '优秀奖': 'info' }
  return map[level] || 'info'
}

const reset = () => { searchCompId.value = ''; searchStudId.value = ''; load() }

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/award/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, competitionId: searchCompId.value || undefined, studentId: searchStudId.value || undefined }
    }) as any
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
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

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <Calendar />
        </el-icon>
        <span>往年成绩存档</span>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchComp" placeholder="竞赛名检索..." clearable style="width: 200px" />
        <el-button type="success" @click="load" :icon="Search">检索存档</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="competitionName" label="历史竞赛名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="competitionType" label="竞赛类型" width="130" align="center" />
        <el-table-column prop="date" label="获奖日期" width="120" align="center" />
        <el-table-column prop="teamName" label="获奖团队/个人" min-width="130" show-overflow-tooltip />
        <el-table-column prop="score" label="最终成绩/奖项" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="font-weight: 700; color: #16a34a">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaderTeacher" label="指导教师" width="120" align="center" />
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
import { Search, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchComp = ref('')

const reset = () => { searchComp.value = ''; load() }

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/historical-record/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, competitionName: searchComp.value || undefined }
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

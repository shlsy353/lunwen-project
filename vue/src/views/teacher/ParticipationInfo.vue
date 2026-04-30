<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <InfoFilled />
        </el-icon>
        <span>参赛信息管理</span>
      </div>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 160px" />
        <el-input v-model="searchStudId" placeholder="学生ID..." clearable style="width: 160px" />
        <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 150px">
          <el-option label="待审核" :value="0" />
          <el-option label="已参赛" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已晋级" :value="3" />
        </el-select>
        <el-button type="primary" @click="load" :icon="Search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="competitionId" label="竞赛ID" width="90" align="center" />
        <el-table-column prop="studentId" label="学生ID" width="90" align="center" />
        <el-table-column label="当前状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审核反馈" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="报名时间" width="180" />
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
import { Search, InfoFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')
const searchStudId = ref('')
const searchStatus = ref(undefined)

const statusText = (s: number) => ['待审核', '已参赛', '已拒绝', '已晋级'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger', 'primary'] as const)[s] ?? 'info'

const reset = () => { searchCompId.value = ''; searchStudId.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/registration/page', {
      params: {
        pageNum: pageNum.value, pageSize: pageSize.value,
        competitionId: searchCompId.value || undefined,
        studentId: searchStudId.value || undefined,
        status: searchStatus.value
      }
    })
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
  color: #3b82f6;
}

.table-card {
  border-radius: 12px;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
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

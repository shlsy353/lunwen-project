<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <Medal />
        </el-icon>
        <span>中奖名单大屏幕</span>
      </div>
    </div>

    <el-card class="premium-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchCompId" placeholder="搜索竞赛ID..." clearable style="width: 200px" />
        <el-input v-model="searchStudId" placeholder="搜索学号..." clearable style="width: 200px" />
        <el-button type="primary" @click="load" :icon="Search" round>检索荣誉榜</el-button>
        <el-button @click="reset" round>重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="competitionId" label="项目ID" width="100" align="center" />
        <el-table-column prop="studentId" label="获奖号/学号" width="120" align="center" />
        <el-table-column prop="name" label="获奖项目" min-width="150" show-overflow-tooltip />
        <el-table-column label="获奖等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="awardType(row.awardLevel)" effect="dark">{{ row.awardLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="颁奖词/说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="揭晓日期" width="180" />
      </el-table>

      <div class="pagination-bar" v-if="total > pageSize">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @change="load" />
      </div>
    </el-card>

    <div class="medal-decor">
      <el-icon class="decor-icon">
        <Trophy />
      </el-icon>
      <span>公示期内外，对获奖名单有异议可与组委会核实。</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, Medal, Trophy } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
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
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        competitionId: searchCompId.value || undefined,
        studentId: searchStudId.value || undefined
      }
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
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.title-icon {
  font-size: 24px;
  color: #3b82f6;
}

.premium-card {
  border-radius: 16px;
  border: none;
  min-height: 50vh;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
}

.data-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #475569;
  font-weight: 700;
}

.pagination-bar {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.medal-decor {
  margin-top: 40px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.decor-icon {
  font-size: 18px;
  color: #fbbf24;
}
</style>

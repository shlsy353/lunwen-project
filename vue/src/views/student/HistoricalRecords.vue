<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <Calendar />
        </el-icon>
        <span>光荣榜 - 往年记录</span>
      </div>
    </div>

    <el-card class="premium-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchComp" placeholder="搜索历史竞赛..." clearable style="width: 240px" />
        <el-button type="primary" @click="load" :icon="Search" round>检索荣誉</el-button>
        <el-button @click="reset" round>重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="competitionName" label="项目名称" min-width="150" />
        <el-table-column prop="competitionType" label="类型" width="120" align="center" />
        <el-table-column prop="date" label="获奖年度" width="110" align="center" />
        <el-table-column prop="teamName" label="获奖团队/个人" min-width="120" show-overflow-tooltip />
        <el-table-column prop="score" label="获奖名次/成绩" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="history-score">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaderTeacher" label="指导教师" width="110" align="center" />
      </el-table>

      <div class="pagination-bar" v-if="total > pageSize">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @change="load" />
      </div>
    </el-card>

    <div class="honor-footer">
      <el-icon class="footer-icon">
        <Star />
      </el-icon>
      <span>榜单数据仅供交流学习，详情请关注历届总结。</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, Calendar, Star } from '@element-plus/icons-vue'

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

.history-score {
  font-weight: 700;
  color: #3b82f6;
  text-decoration: underline;
  text-underline-offset: 4px;
}

.pagination-bar {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.honor-footer {
  margin-top: 40px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.footer-icon {
  font-size: 18px;
  color: #fbbf24;
}
</style>

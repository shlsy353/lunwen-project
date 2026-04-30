<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <Avatar />
        </el-icon>
        <span>我的参赛详情</span>
      </div>
    </div>

    <el-card class="premium-card" shadow="never">
      <div v-if="loading" v-loading="loading" style="height: 300px"></div>
      <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty description="你还没有参赛记录，快去探索竞赛吧！" />
      </div>
      <div v-else class="part-list">
        <el-row :gutter="20">
          <el-col v-for="item in tableData" :key="item.id" :xs="24" :sm="12" :md="8">
            <div class="part-card" :class="statusClass(item.status)">
              <div class="part-card-header">
                <div class="part-comp-id">#ID: {{ item.competitionId }}</div>
                <div class="part-status-tag">{{ statusText(item.status) }}</div>
              </div>
              <div class="part-card-body">
                <p class="part-comment-label">阅卷反馈 / 参赛历程：</p>
                <p class="part-comment-text">{{ item.reviewComment || '参赛过程顺利进行中...' }}</p>
              </div>
              <div class="part-card-footer">
                <span class="part-date"><el-icon>
                    <Calendar />
                  </el-icon> {{ item.createTime?.substring(0, 10) }}</span>
                <el-button link type="primary"
                  @click="$router.push('/competition/' + item.competitionId)">详阅项目</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="pagination-bar" v-if="total > pageSize">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @change="load" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Avatar, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

const statusText = (s: number) => ['审核中', '已参赛', '被驳回', '已晋级'][s] ?? '未知'
const statusClass = (s: number) => (['s-warning', 's-success', 's-danger', 's-primary'] as const)[s] ?? ''

const load = async () => {
  loading.value = true
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  try {
    const res = await request.get('/registration/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, studentId: user.id }
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

.part-list {}

.part-card {
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #f1f5f9;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.part-card:hover {
  transform: translateY(-5px);
  border-color: #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.part-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.part-comp-id {
  font-size: 13px;
  font-weight: 800;
  color: #94a3b8;
  letter-spacing: 0.5px;
}

.part-status-tag {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #64748b;
  text-transform: uppercase;
}

.s-warning .part-status-tag {
  background: #fef3c7;
  color: #d97706;
}

.s-success .part-status-tag {
  background: #d1fae5;
  color: #059669;
}

.s-danger .part-status-tag {
  background: #fee2e2;
  color: #dc2626;
}

.s-primary .part-status-tag {
  background: #dbeafe;
  color: #2563eb;
}

.s-primary::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #3b82f6;
}

.part-card-body {
  margin: 16px 0;
  min-height: 80px;
}

.part-comment-label {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
  margin: 0 0 8px;
}

.part-comment-text {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
  font-style: italic;
}

.part-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f1f5f9;
  padding-top: 12px;
}

.part-date {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-bar {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}
</style>

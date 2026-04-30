<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <DataBoard />
        </el-icon>
        <span>教师工作台</span>
      </div>
    </div>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-blue pointer" @click="$router.push('/teacher/registrations')">
          <div class="stat-icon"><el-icon>
              <Trophy />
            </el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.competitions }}</div>
            <div class="stat-label">进行中竞赛</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-green pointer" @click="$router.push('/teacher/lead-students')">
          <div class="stat-icon"><el-icon>
              <User />
            </el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.students }}</div>
            <div class="stat-label">关联学生</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-orange pointer" @click="$router.push('/teacher/registrations')">
          <div class="stat-icon"><el-icon>
              <Edit />
            </el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.myRegistrations }}</div>
            <div class="stat-label">参与/代报名</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-purple pointer" @click="$router.push('/teacher/appeals')">
          <div class="stat-icon"><el-icon>
              <ChatDotRound />
            </el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.appeals }}</div>
            <div class="stat-label">待处理申诉</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Competition List with Pagination -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title"><el-icon>
                  <Tickets />
                </el-icon> 全校竞赛大厅</span>
            </div>
          </template>

          <el-table v-loading="loading" :data="competitions" stripe style="width: 100%">
            <el-table-column prop="name" label="竞赛名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="organizer" label="主办方" width="180" />
            <el-table-column prop="level" label="竞赛级别" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.level || '校级' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '报名中' : '已结束' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleViewDetail(row)">发布详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
              layout="total, prev, pager, next" @current-change="fetchCompetitions" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Competition Detail Dialog -->
    <el-dialog v-model="detailVisible" title="竞赛详细信息" width="800px">
      <div v-if="currentComp" class="detail-content">
        <h2 class="comp-title">{{ currentComp.name }}</h2>
        <div class="comp-meta">
          <el-tag type="info">级别：{{ currentComp.level || '校级' }}</el-tag>
          <el-tag type="success">主办：{{ currentComp.organizer }}</el-tag>
          <el-tag v-if="currentComp.competitionDate" type="warning">日期：{{ currentComp.competitionDate }}</el-tag>
        </div>
        <el-divider />
        <div class="comp-section">
          <h4>竞赛内容</h4>
          <p class="text-content">{{ currentComp.content || '暂无详细内容说明' }}</p>
        </div>
        <div class="comp-section">
          <h4>竞赛宗旨</h4>
          <p class="text-content">{{ currentComp.purpose || '暂无宗旨说明' }}</p>
        </div>
        <div class="comp-section" v-if="currentComp.location">
          <h4>竞赛地点</h4>
          <p class="text-content">{{ currentComp.location }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Trophy, User, ChatDotRound, Calendar, DataBoard, Edit, Tickets } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const stats = ref({ competitions: 0, students: 0, myRegistrations: 0, appeals: 0 })

const loading = ref(false)
const competitions = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const currentComp = ref<any>(null)

const fetchStats = async () => {
  try {
    const [cRes, rRes, sRes, apRes] = await Promise.allSettled([
      request.get('/competition/page', { params: { pageSize: 1, status: 1 } }),
      request.get('/registration/page', { params: { leaderId: user.id || -1, pageSize: 1 } }),
      request.get('/student-leader/my-students', { params: { leaderId: user.id || -1 } }),
      request.get('/score-appeal/page', { params: { status: 0, leaderId: user.id || -1, pageSize: 1 } }),
    ])
    if (cRes.status === 'fulfilled') stats.value.competitions = cRes.value.data?.total || 0
    if (rRes.status === 'fulfilled') stats.value.myRegistrations = rRes.value.data?.total || 0
    if (sRes.status === 'fulfilled') stats.value.students = sRes.value.data?.length || 0
    if (apRes.status === 'fulfilled') stats.value.appeals = apRes.value.data?.total || 0
  } catch (e) { console.error(e) }
}

const fetchCompetitions = async () => {
  loading.value = true
  try {
    const res = await request.get('/competition/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    }) as any
    if (res.code === 200) {
      competitions.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row: any) => {
  currentComp.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchStats()
  fetchCompetitions()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
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

.stats-row {
  margin-bottom: 4px;
}

.stat-card {
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
  margin-bottom: 16px;
  transition: transform 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.pointer {
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-blue {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.stat-green {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-orange {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-purple {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.stat-icon {
  font-size: 36px;
  opacity: 0.9;
}

.stat-icon .el-icon {
  font-size: 36px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 4px;
}

.content-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1e293b;
  font-size: 15px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  padding: 0 20px;
}

.comp-title {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 16px;
}

.comp-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.comp-section {
  margin-bottom: 24px;
}

.comp-section h4 {
  color: #334155;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 700;
}

.text-content {
  color: #64748b;
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 14px;
}
</style>

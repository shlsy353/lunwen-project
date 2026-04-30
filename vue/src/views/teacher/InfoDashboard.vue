<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <TrendCharts />
        </el-icon>
        <span>竞赛信息外网门户</span>
      </div>
    </div>

    <el-card class="hall-panel" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchName" placeholder="全站竞赛检索..." clearable style="width: 240px" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load" round>查询</el-button>
        <el-button @click="reset" round>重置</el-button>
      </div>

      <el-row :gutter="20" class="comp-grid" v-loading="loading">
        <el-col v-for="item in tableData" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="comp-card" shadow="hover" @click="handleViewDetail(item)">
            <div class="comp-status-badge" :class="statusClass(item.status)">
              {{ statusText(item.status) }}
            </div>
            <div class="comp-card-body">
              <h3 class="comp-name">{{ item.name }}</h3>
              <p class="comp-organizer"><el-icon>
                  <OfficeBuilding />
                </el-icon> {{ item.organizer }}</p>
              <div class="comp-tags">
                <el-tag size="small">{{ item.level || '校级' }}</el-tag>
              </div>
              <div class="comp-meta-card">
                <el-icon>
                  <Calendar />
                </el-icon>
                <span>截止：{{ item.endTime || '长期' }}</span>
              </div>
            </div>
            <div class="comp-card-footer">
              <span>查阅详情</span>
              <el-icon>
                <ArrowRight />
              </el-icon>
            </div>
          </el-card>
        </el-col>
        <el-col v-if="tableData.length === 0" :span="24">
          <el-empty description="暂无相关竞赛信息" />
        </el-col>
      </el-row>

      <div class="pagination-bar">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
          :page-sizes="[8, 16, 24]" layout="total, sizes, prev, pager, next" @size-change="load"
          @current-change="load" />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="项目详情门户" width="750px" destroy-on-close>
      <div v-if="currentComp" class="portal-detail">
        <div class="detail-hero">
          <h1>{{ currentComp.name }}</h1>
          <div class="meta-row">
            <span class="meta-item"><el-icon>
                <Calendar />
              </el-icon> 截止：{{ currentComp.endTime || '长期' }}</span>
            <span class="meta-item"><el-icon>
                <Location />
              </el-icon> {{ currentComp.location || '线上/待定' }}</span>
          </div>
        </div>
        <el-divider />

        <div class="portal-section">
          <div class="section-title">竞赛详情说明</div>
          <div class="section-text">{{ currentComp.content || '暂无详细介绍' }}</div>
        </div>

        <div class="portal-section">
          <div class="section-title">项目宗旨与要求</div>
          <div class="section-text">{{ currentComp.purpose || '暂无详细说明' }}</div>
        </div>

        <div class="portal-section" v-if="currentComp.awardDetails">
          <div class="section-title">奖励设置</div>
          <div class="section-text">{{ currentComp.awardDetails }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, TrendCharts, OfficeBuilding, ArrowRight, Calendar, Location } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(8)
const searchName = ref('')

const detailVisible = ref(false)
const currentComp = ref<any>(null)

const statusText = (s: number) => ['已结束', '报名中', '已开始'][s] ?? '竞赛中'
const statusClass = (s: number) => ['status-end', 'status-active', 'status-progress'][s] ?? 'status-progress'

const reset = () => { searchName.value = ''; load() }

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/competition/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        name: searchName.value || undefined
      }
    }) as any
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } finally { loading.value = false }
}

const handleViewDetail = (item: any) => {
  currentComp.value = item
  detailVisible.value = true
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
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}

.title-icon {
  font-size: 26px;
  color: #6366f1;
}

.hall-panel {
  border-radius: 16px;
  border: none;
  background: transparent;
}

.search-bar {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.comp-grid {
  margin-bottom: 24px;
}

.comp-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.comp-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}

.comp-status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  color: #fff;
}

.status-active {
  background: #10b981;
}

.status-end {
  background: #94a3b8;
}

.status-progress {
  background: #3b82f6;
}

.comp-card-body {
  padding: 20px;
  flex: 1;
}

.comp-name {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px;
  line-height: 1.4;
}

.comp-organizer {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.comp-meta-card {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
}

.comp-tags {
  margin-top: auto;
}

.comp-card-footer {
  padding: 12px 20px;
  background: #f8fafc;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #6366f1;
  font-weight: 600;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
}

/* Detail Portal Style */
.portal-detail {
  padding: 0 10px;
}

.detail-hero h1 {
  font-size: 26px;
  color: #1e293b;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.meta-row {
  display: flex;
  gap: 20px;
  color: #64748b;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.portal-section {
  margin-top: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 800;
  color: #334155;
  margin-bottom: 12px;
  border-left: 4px solid #6366f1;
  padding-left: 12px;
}

.section-text {
  background: #f1f5f9;
  padding: 16px;
  border-radius: 8px;
  color: #475569;
  line-height: 1.8;
  font-size: 14px;
  white-space: pre-wrap;
}
</style>

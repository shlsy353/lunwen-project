<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <Trophy />
        </el-icon>
        <span>全校竞赛大厅</span>
      </div>
    </div>

    <el-card class="hall-panel" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchName" placeholder="搜竞赛名称..." clearable style="width: 200px" />
        <el-select v-model="searchStatus" placeholder="所有状态" clearable style="width: 140px">
          <el-option label="报名中" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="竞赛项目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="organizer" label="主办单位" width="180" />
        <el-table-column prop="level" label="级别" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.level || '校级' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">详情查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="竞赛详细内容汇编" width="800px" destroy-on-close>
      <div v-if="currentComp" class="detail-content">
        <div class="detail-header">
           <h2>{{ currentComp.name }}</h2>
           <div class="tags">
             <el-tag>{{ currentComp.organizer }}</el-tag>
             <el-tag type="warning">{{ currentComp.level || '校级' }}</el-tag>
           </div>
        </div>
        <el-divider />
        <div class="detail-section">
          <h3><el-icon><Notebook /></el-icon> 竞赛内容</h3>
          <div class="text-box">{{ currentComp.content || '暂无内容' }}</div>
        </div>
        <div class="detail-section">
          <h3><el-icon><Compass /></el-icon> 竞赛宗旨</h3>
          <div class="text-box">{{ currentComp.purpose || '暂无宗旨' }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, Trophy, Notebook, Compass } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchName = ref('')
const searchStatus = ref<number | undefined>(undefined)

const detailVisible = ref(false)
const currentComp = ref<any>(null)

const reset = () => { searchName.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/competition/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                name: searchName.value || undefined,
                status: searchStatus.value
            }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
        }
    } finally { loading.value = false }
}

const handleViewDetail = (row: any) => {
    currentComp.value = row
    detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-title { display: flex; align-items: center; gap: 10px; font-size: 22px; font-weight: 700; color: #1e293b; }
.title-icon { font-size: 26px; color: #f59e0b; }
.hall-panel { border-radius: 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 24px; }
.pagination-bar { margin-top: 24px; display: flex; justify-content: flex-end; }
.detail-content { padding: 0 10px; }
.detail-header h2 { margin: 0 0 12px; font-size: 24px; color: #1e293b; }
.tags { display: flex; gap: 8px; }
.detail-section h3 { display: flex; align-items: center; gap: 8px; font-size: 18px; color: #334155; margin-bottom: 12px; }
.text-box { background: #f8fafc; padding: 16px; border-radius: 8px; line-height: 1.8; color: #475569; white-space: pre-wrap; }
</style>

<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <ChatDotRound />
                </el-icon>
                <span>成绩申诉管理</span>
            </div>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchStudentId" placeholder="学生学号搜索..." clearable style="width: 200px" @clear="loadAppeals" />
                <el-select v-model="selectStatus" placeholder="申诉状态" clearable style="width: 150px" @change="loadAppeals">
                    <el-option label="待处理" :value="0" />
                    <el-option label="已回复" :value="1" />
                </el-select>
                <el-button type="primary" @click="loadAppeals" :icon="Search">查询</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe border>
                <el-table-column prop="id" label="ID" width="80" align="center" />
                <el-table-column label="申诉竞赛" min-width="180" show-overflow-tooltip>
                    <template #default="{ row }">
                        {{ competitionMap[row.competitionId] || ('ID: ' + row.competitionId) }}
                    </template>
                </el-table-column>
                <el-table-column label="申诉学生" width="150">
                    <template #default="{ row }">
                        {{ studentMap[row.studentId] || ('ID: ' + row.studentId) }}
                    </template>
                </el-table-column>
                <el-table-column prop="score" label="申请复核分数" width="120" align="center" />
                <el-table-column prop="reason" label="申诉理由" min-width="150" show-overflow-tooltip />
                <el-table-column label="状态" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag v-if="row.status === 0" type="warning">待处理</el-tag>
                        <el-tag v-else type="success">已回复</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="申诉时间" width="170" align="center" />
                <el-table-column label="操作" width="120" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="row.status === 0" type="primary" link @click="handleReview(row)">去处理</el-button>
                        <el-button v-else type="info" link @click="handleReview(row)">查看</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @current-change="loadAppeals" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="currentAppeal?.status === 0 ? '处理成绩申诉' : '查看申诉详情'" width="500px">
            <div v-if="currentAppeal" class="appeal-detail">
                <div class="detail-item">
                    <span class="label">申诉学生：</span>
                    <span class="val">{{ studentMap[currentAppeal.studentId] }}</span>
                </div>
                <div class="detail-item">
                    <span class="label">申诉理由：</span>
                    <div class="reason-box">{{ currentAppeal.reason }}</div>
                </div>
                <el-divider />
                <el-form :model="reviewForm" label-width="80px">
                    <el-form-item label="回复内容">
                        <el-input v-model="reviewForm.response" type="textarea" :rows="4" 
                            :placeholder="currentAppeal.status === 0 ? '请输入回复意见...' : '暂无回复'" 
                            :disabled="currentAppeal.status === 1" />
                    </el-form-item>
                </el-form>
            </div>
            <template #footer v-if="currentAppeal?.status === 0">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="confirmReview">确认回复</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, ChatDotRound } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchStudentId = ref('')
const selectStatus = ref<number | undefined>(undefined)

const studentMap = ref<Record<number, string>>({})
const competitionMap = ref<Record<number, string>>({})

const dialogVisible = ref(false)
const currentAppeal = ref<any>(null)
const reviewForm = reactive({ response: '' })

const loadAppeals = async () => {
    loading.value = true
    try {
        const res = await request.get('/score-appeal/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                status: selectStatus.value,
                // Assuming backend can filter student by something, but let's keep it simple
            }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
            resolveContext(tableData.value)
        }
    } finally {
        loading.value = false
    }
}

const resolveContext = async (list: any[]) => {
    const sIds = [...new Set(list.map(i => i.studentId))]
    const cIds = [...new Set(list.map(i => i.competitionId))]
    
    for (const sid of sIds) {
        if (!studentMap.value[sid]) {
            const res = await request.get(`/user/${sid}`) as any
            if (res.data) studentMap.value[sid] = res.data.name || res.data.username
        }
    }
    for (const cid of cIds) {
        if (!competitionMap.value[cid]) {
            const res = await request.get(`/competition/${cid}`) as any
            if (res.data) competitionMap.value[cid] = res.data.name
        }
    }
}

const handleReview = (appeal: any) => {
    currentAppeal.value = appeal
    reviewForm.response = appeal.response || ''
    dialogVisible.value = true
}

const confirmReview = async () => {
    if (!reviewForm.response.trim()) return ElMessage.warning('请输入回复内容')
    try {
        await request.put('/score-appeal', {
            id: currentAppeal.value.id,
            response: reviewForm.response,
            status: 1
        })
        ElMessage.success('已回复申诉')
        dialogVisible.value = false
        loadAppeals()
    } catch (e) {
        ElMessage.error('处理失败')
    }
}

onMounted(loadAppeals)
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-title { display: flex; align-items: center; gap: 10px; font-size: 20px; font-weight: 600; color: #1e293b; }
.title-icon { font-size: 22px; color: #3b82f6; }
.table-card { border-radius: 12px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.pagination-bar { margin-top: 20px; display: flex; justify-content: flex-end; }

.appeal-detail { padding: 10px 0; }
.detail-item { margin-bottom: 12px; display: flex; flex-direction: column; gap: 6px; }
.detail-item .label { font-weight: 600; color: #64748b; font-size: 14px; }
.detail-item .val { color: #1e293b; }
.reason-box { 
    background: #f8fafc; padding: 12px; border-radius: 8px; border: 1px solid #f1f5f9; 
    color: #475569; line-height: 1.6; font-size: 14px;
}
</style>

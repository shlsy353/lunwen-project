<template>
    <div class="page-container">
        <div v-if="!isLeader" class="apply-prompt-container">
            <el-card class="apply-card">
                <div class="apply-content">
                    <el-icon class="apply-icon">
                        <Lock />
                    </el-icon>
                    <h3>{{ user.leaderStatus === 1 ? '领队申请审核中' : '尚未获得领队资格' }}</h3>
                    <p>{{ user.leaderStatus === 1 ? '您的领队教师资格申请已提交，正在等待管理员审核。' : '成绩申诉功能仅开放给领队老师，请先申请领队资格。' }}</p>
                    <div v-if="user.leaderStatus === 1">
                        <el-tag type="warning" size="large" effect="plain" round>
                            <el-icon>
                                <InfoFilled />
                            </el-icon> 审核中
                        </el-tag>
                    </div>
                    <el-button v-else type="primary" size="large" round
                        @click="$router.push('/teacher/profile')">前往个人中心申请</el-button>
                    <el-button v-if="user.leaderStatus === 1" plain size="large" round
                        @click="$router.push('/teacher/profile')" style="margin-top: 15px">查看申请进度</el-button>
                </div>
            </el-card>
        </div>
        <template v-else>
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
                    <el-input v-model="searchReason" placeholder="申诉理由..." clearable style="width: 200px" />
                    <el-select v-model="searchStatus" placeholder="处理状态" clearable style="width: 150px">
                        <el-option label="待处理" :value="0" />
                        <el-option label="已同意" :value="1" />
                        <el-option label="已驳回" :value="2" />
                    </el-select>
                    <el-button type="primary" @click="load" :icon="Search">查询</el-button>
                    <el-button @click="reset">重置</el-button>
                </div>

                <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="studentId" label="学生ID" width="100" align="center" />
                    <el-table-column prop="scoreId" label="成绩ID" width="100" align="center" />
                    <el-table-column label="处理状态" width="110" align="center">
                        <template #default="{ row }">
                            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="reason" label="申诉原因" min-width="150" show-overflow-tooltip />
                    <el-table-column prop="content" label="详细内容" min-width="200" show-overflow-tooltip />
                    <el-table-column prop="result" label="处理结果" min-width="150" show-overflow-tooltip>
                        <template #default="{ row }">
                            <span v-if="row.result" :style="{ color: row.status === 1 ? '#10b981' : '#f43f5e' }">
                                {{ row.result }}
                            </span>
                            <span v-else style="color: #94a3b8">尚未回复</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="申诉时间" width="180" />
                    <el-table-column label="操作" width="180" align="center" fixed="right">
                        <template #default="{ row }">
                            <template v-if="row.status === 0">
                                <el-button link type="success" @click="handleReview(row, 1)">同意</el-button>
                                <el-divider direction="vertical" />
                                <el-button link type="danger" @click="handleReview(row, 2)">驳回</el-button>
                            </template>
                            <span v-else class="no-action">已处理</span>
                        </template>
                    </el-table-column>
                </el-table>

                <div class="pagination-bar">
                    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                        layout="total, prev, pager, next" @change="load" />
                </div>
            </el-card>

            <el-dialog v-model="dialogVisible" title="申诉审核" width="500px">
                <el-form :model="reviewForm" label-width="80px" class="dialog-form">
                    <el-form-item label="审核结果">
                        <el-input v-model="reviewForm.result" type="textarea" :rows="3" placeholder="请输入审核处理意见..." />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="confirmReview">提交处理</el-button>
                </template>
            </el-dialog>
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, ChatDotRound, Lock, InfoFilled } from '@element-plus/icons-vue'

const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const isLeader = computed(() => user.value.role === 'LEADER' || user.value.leaderStatus === 2)

const fetchUserInfo = async () => {
    const res = await request.get('/user/info') as any
    if (res.code === 200) {
        user.value = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
    }
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchReason = ref('')
const searchStatus = ref(undefined)
const dialogVisible = ref(false)
const reviewForm = reactive({ id: undefined, status: 0, result: '' })

const statusText = (s: number) => ['待处理', '已同意', '已驳回'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger'] as const)[s] ?? 'info'

const reset = () => { searchReason.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/score-appeal/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                reason: searchReason.value || undefined,
                status: searchStatus.value,
                leaderId: user.value.id
            }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
        }
    } finally {
        loading.value = false
    }
}

const handleReview = (row: any, status: number) => {
    reviewForm.id = row.id
    reviewForm.status = status
    reviewForm.result = status === 1 ? '同意申诉' : '理由不充分，驳回申诉'
    dialogVisible.value = true
}

const confirmReview = async () => {
    await request.put('/score-appeal', reviewForm)
    ElMessage.success('处理成功')
    dialogVisible.value = false
    load()
}

onMounted(async () => {
    await fetchUserInfo()
    if (isLeader.value) {
        load()
    }
})
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

.no-action {
    color: #94a3b8;
    font-size: 13px;
}

.dialog-form {
    padding: 10px 0;
}

.apply-prompt-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 50vh;
}

.apply-card {
    width: 100%;
    max-width: 500px;
    border-radius: 20px;
    text-align: center;
    padding: 40px 20px;
    border: none;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.apply-icon {
    font-size: 64px;
    color: #f59e0b;
    margin-bottom: 20px;
    opacity: 0.8;
}

.apply-content h3 {
    font-size: 22px;
    color: #1e293b;
    margin-bottom: 12px;
}

.apply-content p {
    color: #64748b;
    margin-bottom: 30px;
    line-height: 1.6;
}
</style>

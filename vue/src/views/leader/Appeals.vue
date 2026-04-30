<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <ChatDotRound />
                </el-icon>
                <span>成绩申诉决策</span>
            </div>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchReason" placeholder="申诉关键词..." clearable style="width: 200px" />
                <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 140px">
                    <el-option label="待研判" :value="0" />
                    <el-option label="已驳回" :value="2" />
                    <el-option label="受理中" :value="1" />
                </el-select>
                <el-button type="success" @click="load" :icon="Search">研判查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="studentId" label="学生ID" width="100" align="center" />
                <el-table-column prop="reason" label="申诉理由" min-width="150" show-overflow-tooltip />
                <el-table-column prop="content" label="详情简述" min-width="200" show-overflow-tooltip />
                <el-table-column label="状态" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="result" label="处理终判" min-width="180" show-overflow-tooltip />
                <el-table-column label="操作" width="140" align="center" fixed="right">
                    <template #default="{ row }">
                        <template v-if="row.status === 0">
                            <el-button link type="success" @click="handleReview(row, 1)">受理</el-button>
                            <el-divider direction="vertical" />
                            <el-button link type="danger" @click="handleReview(row, 2)">驳回</el-button>
                        </template>
                        <span v-else class="no-action">研判完成</span>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" title="申诉研判决策" width="450px">
            <el-form label-width="80px" class="dialog-form">
                <el-form-item label="研判意见">
                    <el-input v-model="reviewResult" type="textarea" :rows="3" placeholder="填写最终研判结论..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">返回</el-button>
                <el-button type="success" @click="confirmReview">下达决策</el-button>
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
const searchReason = ref('')
const searchStatus = ref(undefined)
const dialogVisible = ref(false)
const reviewData = reactive({ id: undefined, status: 0 })
const reviewResult = ref('')

const statusText = (s: number) => ['待研判', '受理中', '已驳回'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger'] as const)[s] ?? 'info'

const reset = () => { searchReason.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/score-appeal/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, reason: searchReason.value || undefined, status: searchStatus.value }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const handleReview = (row: any, status: number) => {
    reviewData.id = row.id
    reviewData.status = status
    reviewResult.value = status === 1 ? '情况属实，着手复核' : '理由不充分，维持原判'
    dialogVisible.value = true
}

const confirmReview = async () => {
    await request.put('/score-appeal', { id: reviewData.id, status: reviewData.status, result: reviewResult.value })
    ElMessage.success('决策已下达')
    dialogVisible.value = false
    load()
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

.no-action {
    color: #94a3b8;
    font-size: 13px;
}
</style>

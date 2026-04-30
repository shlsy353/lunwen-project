<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Tickets />
                </el-icon>
                <span>报名信息审核</span>
            </div>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 140px" />
                <el-input v-model="searchStudId" placeholder="学生ID..." clearable style="width: 140px" />
                <el-select v-model="searchStatus" placeholder="报名状态" clearable style="width: 140px">
                    <el-option label="待审核" :value="0" />
                    <el-option label="已参赛" :value="1" />
                    <el-option label="已拒绝" :value="2" />
                </el-select>
                <el-button type="success" @click="load" :icon="Search">查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="competitionId" label="竞赛ID" width="100" align="center" />
                <el-table-column prop="studentId" label="学生ID" width="100" align="center" />
                <el-table-column label="审核状态" width="110" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="reviewComment" label="反馈意见" min-width="200" show-overflow-tooltip />
                <el-table-column prop="createTime" label="报名时间" width="180" />
                <el-table-column label="操作" width="160" align="center" fixed="right">
                    <template #default="{ row }">
                        <template v-if="row.status === 0">
                            <el-button link type="success" @click="handleReview(row, 1)">通过</el-button>
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

        <el-dialog v-model="dialogVisible" title="报名审核" width="450px">
            <el-form :model="reviewForm" label-width="80px" class="dialog-form">
                <el-form-item label="反馈意见">
                    <el-input v-model="reviewForm.reviewComment" type="textarea" :rows="3"
                        placeholder="填写审核不通过理由或通过备注..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="confirmReview">提交审核</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, Tickets } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')
const searchStudId = ref('')
const searchStatus = ref(undefined)
const dialogVisible = ref(false)
const reviewForm = reactive({ id: undefined, status: 0, reviewComment: '' })

const statusText = (s: number) => ['待审核', '已参赛', '已拒绝', '已晋级'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger', 'primary'] as const)[s] ?? 'info'

const reset = () => { searchCompId.value = ''; searchStudId.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/registration/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, competitionId: searchCompId.value || undefined, studentId: searchStudId.value || undefined, status: searchStatus.value }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const handleReview = (row: any, nextStatus: number) => {
    reviewForm.id = row.id
    reviewForm.status = nextStatus
    reviewForm.reviewComment = nextStatus === 1 ? '报名通过' : '申请不符合要求'
    dialogVisible.value = true
}

const confirmReview = async () => {
    await request.put('/registration', reviewForm)
    ElMessage.success('审核完成')
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

.dialog-form {
    padding: 10px 0;
}
</style>

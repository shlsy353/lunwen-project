<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <PriceTag />
                </el-icon>
                <span>报销清单审核</span>
            </div>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchItem" placeholder="报销项..." clearable style="width: 200px" />
                <el-select v-model="searchStatus" placeholder="审核状态" clearable style="width: 140px">
                    <el-option label="待审核" :value="0" />
                    <el-option label="已通过" :value="1" />
                    <el-option label="已驳回" :value="2" />
                </el-select>
                <el-button type="success" @click="load" :icon="Search">检索审核项</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="itemName" label="报销项" min-width="150" show-overflow-tooltip />
                <el-table-column prop="amount" label="金额" width="120" align="right">
                    <template #default="{ row }">
                        <span style="color: #16a34a; font-weight: 700">¥ {{ row.amount?.toFixed(2) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="110" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述归口" min-width="200" show-overflow-tooltip />
                <el-table-column prop="createTime" label="申请日期" width="180" />
                <el-table-column label="操作" width="160" align="center" fixed="right">
                    <template #default="{ row }">
                        <template v-if="row.status === 0">
                            <el-button link type="success" @click="handleReview(row, 1)">通过</el-button>
                            <el-divider direction="vertical" />
                            <el-button link type="danger" @click="handleReview(row, 2)">驳回</el-button>
                        </template>
                        <span v-else class="no-action">已审</span>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" title="报销初审/复核" width="450px">
            <el-form label-width="80px" class="dialog-form">
                <el-form-item label="备注说明">
                    <el-input v-model="reviewComment" type="textarea" :rows="3" placeholder="填写审核决策理由..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="confirmReview">下达指令</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, PriceTag } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchItem = ref('')
const searchStatus = ref(undefined)
const dialogVisible = ref(false)
const reviewData = reactive({ id: undefined, status: 0 })
const reviewComment = ref('')

const statusText = (s: number) => ['待审核', '已通过', '已驳回'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger'] as const)[s] ?? 'info'

const reset = () => { searchItem.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/expense/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, itemName: searchItem.value || undefined, status: searchStatus.value }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const handleReview = (row: any, status: number) => {
    reviewData.id = row.id
    reviewData.status = status
    reviewComment.value = status === 1 ? '同意报销，转入财务流程' : '票据不全或不符合报销范围'
    dialogVisible.value = true
}

const confirmReview = async () => {
    await request.put('/expense', { id: reviewData.id, status: reviewData.status })
    ElMessage.success('审核决策已生效')
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

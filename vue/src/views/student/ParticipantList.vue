<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <UserFilled />
                </el-icon>
                <span>参赛名单公示</span>
            </div>
        </div>

        <el-card class="premium-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchCompId" placeholder="按竞赛ID筛选..." clearable style="width: 200px" />
                <el-button type="primary" @click="load" :icon="Search" round>检索名单</el-button>
                <el-button @click="reset" round>重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="competitionId" label="项目ID" width="100" align="center" />
                <el-table-column prop="studentId" label="学号/识别码" width="120" align="center" />
                <el-table-column label="参与状态" width="120" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'primary'" effect="plain">
                            {{ row.status === 1 ? '正式参赛' : '晋级选手' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="确立日期" width="180" align="center" />
                <el-table-column prop="reviewComment" label="备注说明" min-width="200" show-overflow-tooltip />
            </el-table>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <div class="notice-box">
            <el-icon>
                <InfoFilled />
            </el-icon>
            <span>名单仅公示已通过审核的正式参赛选手及晋级选手。</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, UserFilled, InfoFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(15)
const searchCompId = ref('')

const reset = () => { searchCompId.value = ''; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/registration/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                competitionId: searchCompId.value || undefined,
                status: 1 // Only confirmed
            }
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

.pagination-bar {
    margin-top: 32px;
    display: flex;
    justify-content: center;
}

.notice-box {
    margin-top: 24px;
    padding: 12px 16px;
    background: #eff6ff;
    border-radius: 8px;
    color: #3b82f6;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 8px;
}
</style>

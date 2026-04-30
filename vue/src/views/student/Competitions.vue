<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Collection />
                </el-icon>
                <span>竞赛大厅</span>
            </div>
        </div>

        <!-- Stats Panel -->
        <el-card class="premium-card hall-panel" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchName" placeholder="全站搜索竞赛..." clearable style="width: 240px" />
                <el-select v-model="searchType" placeholder="竞赛分类" clearable style="width: 160px">
                    <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                </el-select>
                <el-select v-model="searchStatus" placeholder="所有状态" clearable style="width: 140px">
                    <el-option label="报名中" :value="0" />
                    <el-option label="进行中" :value="1" />
                    <el-option label="已结束" :value="2" />
                </el-select>
                <el-button type="primary" @click="load" :icon="Search" round>检索竞赛</el-button>
                <el-button @click="reset" round>重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table" @row-click="goToDetail">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="name" label="项目名称" min-width="200" show-overflow-tooltip>
                    <template #default="{ row }">
                        <span class="row-comp-name">{{ row.name }}</span>
                    </template>
                </el-table-column>
                <el-table-column prop="typeName" label="所属分类" width="140" align="center">
                    <template #default="{ row }">
                        <span>{{ row.typeName }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="当前状态" width="120" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)" effect="dark">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="endTime" label="截止日期" width="120" align="center">
                    <template #default="{ row }">
                        {{ row.endTime || '长期' }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="primary" @click.stop="goToDetail(row)">立即查看</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="load"
                    @current-change="load" />
            </div>
        </el-card>

    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import request from '@/utils/request'
import { useRouter, useRoute } from 'vue-router'
import { Search, Collection, Star } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchName = ref('')
const searchType = ref<number | undefined>(undefined)
const searchStatus = ref<number | undefined>(undefined)
const types = ref<any[]>([])

const statusText = (s: number) => ['已结束', '进行中', '未开始'][s] ?? '未知'
const statusType = (s: number) => (['info', 'success', 'warning'] as const)[s] ?? 'info'

const reset = () => { searchName.value = ''; searchType.value = undefined; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/competition/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                name: searchName.value || undefined,
                typeId: searchType.value,
                status: searchStatus.value
            }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records.map((row: any) => ({
                ...row,
                typeName: types.value.find(t => t.id === row.typeId)?.name || '加载中...'
            }))
            total.value = res.data.total
        }
    } finally { loading.value = false }
}

const fetchTypes = async () => {
    const res = await request.get('/competition-type/list') as any
    if (res.code === 200) types.value = res.data
}

const goToDetail = (row: any) => { router.push('/competition/' + row.id) }

onMounted(async () => {
    if (route.query.typeId) searchType.value = Number(route.query.typeId)
    await fetchTypes()
    load()
})

watch(() => route.query.typeId, (newId) => {
    if (newId) { searchType.value = Number(newId); load() }
})
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

.hall-panel {
    border-radius: 16px;
    border: none;
    padding: 10px;
}

.search-bar {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: 24px;
    flex-wrap: wrap;
}

.data-table {
    cursor: pointer;
    border-radius: 8px;
    overflow: hidden;
}

.data-table :deep(.el-table__header th) {
    background: #f1f5f9;
    color: #334155;
    font-weight: 700;
}

.pagination-bar {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
}

.section-title {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin: 32px 0 16px;
    position: relative;
    padding-left: 12px;
}

.section-title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 15%;
    height: 70%;
    width: 4px;
    background: #3b82f6;
    border-radius: 2px;
}

.featured-card {
    border-radius: 16px;
    border: none;
    cursor: pointer;
    transition: all 0.3s;
    height: 200px;
    padding: 10px;
    position: relative;
}

.featured-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
    border-color: #3b82f6;
}

.featured-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    background: linear-gradient(135deg, #f59e0b, #d97706);
    color: #fff;
    font-size: 10px;
    font-weight: 800;
    padding: 2px 8px;
    border-radius: 4px;
    letter-spacing: 1px;
}

.featured-name {
    color: #1e293b;
    margin: 0 0 12px;
    font-size: 16px;
    font-weight: 700;
}

.featured-desc {
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
    height: 80px;
    overflow: hidden;
    margin-bottom: 12px;
}

.award-label {
    font-size: 12px;
    color: #3b82f6;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 4px;
}

.row-comp-name {
    font-weight: 600;
    color: #334155;
}
</style>

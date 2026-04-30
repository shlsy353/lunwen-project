<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Memo />
                </el-icon>
                <span>竞赛题库</span>
            </div>
        </div>

        <el-card class="premium-card table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchKey" placeholder="搜索题目关键词..." clearable style="width: 200px" />
                <el-select v-model="searchType" placeholder="题目类型" clearable style="width: 150px">
                    <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                </el-select>
                <el-button type="primary" @click="load" :icon="Search" round>检索题目</el-button>
                <el-button @click="reset" round>重置</el-button>
            </div>

            <el-row :gutter="20">
                <el-col v-for="q in tableData" :key="q.id" :xs="24" :sm="12" :md="8">
                    <el-card class="question-card" shadow="hover" @click="viewDetail(q)">
                        <div class="q-type-label">{{ q.typeName }}</div>
                        <h3 class="q-title">{{ q.name }}</h3>
                        <p class="q-content">{{ q.content }}</p>
                        <div class="q-footer">
                            <span class="q-score">分值：{{ q.score }}</span>
                            <el-button link type="primary" @click.stop="viewDetail(q)">
                                查看详情 <el-icon>
                                    <ArrowRight />
                                </el-icon>
                            </el-button>
                        </div>
                    </el-card>
                </el-col>
            </el-row>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" title="题目详情" width="600px" center custom-class="q-dialog">
            <div class="dialog-q-header">
                <el-tag effect="dark">{{ currentQ.typeName }}</el-tag>
                <span class="dialog-q-score">预估分值：{{ currentQ.score }}</span>
            </div>
            <h2 class="dialog-q-title">{{ currentQ.name }}</h2>
            <el-divider />
            <div class="dialog-q-content">
                <div class="label">题目说明：</div>
                <p class="text">{{ currentQ.content }}</p>
            </div>
            <template #footer>
                <el-button type="primary" round @click="dialogVisible = false">了解了</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, Memo, ArrowRight } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(9)
const searchKey = ref('')
const searchType = ref(undefined)
const types = ref<any[]>([])
const dialogVisible = ref(false)
const currentQ = ref<any>({})

const reset = () => { searchKey.value = ''; searchType.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/question-bank/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, name: searchKey.value || undefined, typeId: searchType.value }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const fetchTypes = async () => {
    const res = await request.get('/question-type/list') as any
    if (res.code === 200) types.value = res.data
}

const viewDetail = (q: any) => { currentQ.value = q; dialogVisible.value = true }

onMounted(() => { load(); fetchTypes() })
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
    min-height: 60vh;
    border: none;
}

.search-bar {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: 24px;
}

.question-card {
    margin-bottom: 20px;
    border-radius: 12px;
    border: 1px solid #f1f5f9;
    padding: 16px;
    cursor: pointer;
    transition: all 0.3s;
    height: 180px;
    display: flex;
    flex-direction: column;
    position: relative;
}

.question-card:hover {
    transform: translateY(-5px);
    border-color: #3b82f6;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.q-type-label {
    position: absolute;
    top: 12px;
    right: 12px;
    font-size: 11px;
    background: #eff6ff;
    color: #3b82f6;
    padding: 2px 8px;
    border-radius: 4px;
    font-weight: 600;
}

.q-title {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    margin-top: 10px;
    margin-bottom: 12px;
}

.q-content {
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
}

.q-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding-top: 10px;
    border-top: 1px dashed #e2e8f0;
}

.q-score {
    font-size: 13px;
    font-weight: 600;
    color: #3b82f6;
}

.pagination-bar {
    margin-top: 24px;
    display: flex;
    justify-content: center;
}

.dialog-q-title {
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
    margin: 16px 0;
}

.dialog-q-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.dialog-q-score {
    font-size: 14px;
    color: #64748b;
    font-weight: 500;
}

.dialog-q-content {
    padding: 12px 0;
}

.dialog-q-content .label {
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 8px;
}

.dialog-q-content .text {
    line-height: 1.8;
    color: #475569;
}
</style>

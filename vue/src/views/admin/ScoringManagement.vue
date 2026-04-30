<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Edit />
                </el-icon>
                <span>竞赛评分管理</span>
            </div>
        </div>

        <el-card class="table-card" shadow="never">
            <template v-if="!selectedCompetition">
                <div class="search-bar">
                    <el-input v-model="searchCompName" placeholder="竞赛名称搜索..." clearable style="width: 200px" @clear="loadCompetitions" />
                    <el-button type="primary" @click="loadCompetitions" :icon="Search">查询</el-button>
                </div>
                <el-table :data="competitions" v-loading="loading" stripe border @selection-change="handleSelectionChange">
                    <el-table-column type="selection" width="55" />
                    <el-table-column prop="id" label="ID" width="80" />
                    <el-table-column prop="name" label="竞赛名称" />
                    <el-table-column prop="competitionDate" label="开展日期" width="120" />
                    <el-table-column label="操作" width="120" align="center">
                        <template #default="{ row }">
                            <el-button type="primary" link @click="enterScoring(row)">进入评分</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </template>

            <template v-else>
                <div class="scoring-header">
                    <el-button :icon="ArrowLeft" link @click="backToCompetitions">返回竞赛列表</el-button>
                    <span class="active-comp">正在评分：{{ selectedCompetition.name }}</span>
                </div>
                <div class="search-bar">
                    <el-input v-model="searchStudId" placeholder="学生ID搜索..." clearable style="width: 160px" @clear="loadWorks" />
                    <el-button type="primary" @click="loadWorks" :icon="Search">查询</el-button>
                    <el-button type="success" :icon="Edit" @click="openBatchScore" :disabled="!selectedIdsSub.length">批量评分</el-button>
                </div>
                <el-table :data="tableData" v-loading="loading" stripe border @selection-change="handleSelectionChangeSub">
                    <el-table-column type="selection" width="55" />
                    <el-table-column prop="id" label="ID" width="60" align="center" />
                    <el-table-column prop="studentId" label="学生/参赛者" width="120">
                        <template #default="{ row }">
                            {{ studentMap[row.studentId] || ('ID: ' + row.studentId) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="workFile" label="作品详情">
                        <template #default="{ row }">
                            <el-button v-if="row.workFile" link type="primary" @click="viewWork(row.workFile)">查看成果作品</el-button>
                            <span v-else style="color: #94a3b8">（未提交）</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="评分状态" width="120">
                        <template #default="{ row }">
                            <el-tag v-if="row.score !== null && row.score !== undefined" type="success">{{ row.score }} 分</el-tag>
                            <el-tag v-else type="info">待评分</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120" align="center">
                        <template #default="{ row }">
                            <el-button type="primary" link @click="handleScore(row)">打分评阅</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </template>
        </el-card>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="selectedCompetition ? loadWorks() : loadCompetitions()" />
            </div>


        <el-dialog v-model="dialogVisible" title="作品打分与评价" width="450px">
            <el-form :model="scoreForm" label-width="80px" class="dialog-form">
                <el-form-item label="分数" prop="score">
                    <el-input-number v-model="scoreForm.score" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
                <el-form-item label="评价" prop="comment">
                    <el-input v-model="scoreForm.comment" type="textarea" :rows="3" placeholder="写一点评价反馈..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="confirmScore">提交成绩</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="batchScoreVisible" title="批量打分与评价" width="450px">
            <div style="margin-bottom: 20px; color: #64748b;">
                正在为选中的 <span style="color: #3b82f6; font-weight: 700;">{{ selectedIdsSub.length }}</span> 项作品统一评分
            </div>
            <el-form :model="batchScoreForm" label-width="80px" class="dialog-form">
                <el-form-item label="统一分数">
                    <el-input-number v-model="batchScoreForm.score" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
                <el-form-item label="统一评价">
                    <el-input v-model="batchScoreForm.comment" type="textarea" :rows="3" placeholder="批量评价内容..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="batchScoreVisible = false">取消</el-button>
                <el-button type="success" @click="confirmBatchScore">确 认</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Edit, ArrowLeft } from '@element-plus/icons-vue'

const loading = ref(false)
const competitions = ref([])
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompName = ref('')
const searchStudId = ref('')
const selectedCompetition = ref<any>(null)
const studentMap = ref<Record<number, string>>({})

const user = JSON.parse(localStorage.getItem('user') || '{}')
const dialogVisible = ref(false)
const scoreForm = reactive({ id: undefined, score: 0, comment: '' })

const batchScoreVisible = ref(false)
const batchScoreForm = reactive({ score: 90, comment: '表现优异' })

const loadCompetitions = async () => {
    loading.value = true
    try {
        const res = await request.get('/competition/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, name: searchCompName.value }
        }) as any
        competitions.value = res.data.records
        total.value = res.data.total
    } finally { loading.value = false }
}

const enterScoring = (comp: any) => {
    selectedCompetition.value = comp
    pageNum.value = 1
    loadWorks()
}

const backToCompetitions = () => {
    selectedCompetition.value = null
    pageNum.value = 1
    loadCompetitions()
}

const loadWorks = async () => {
    loading.value = true
    try {
        const res = await request.get('/work-submission/page', {
            params: { 
                competitionId: selectedCompetition.value.id, 
                pageNum: pageNum.value, 
                pageSize: pageSize.value,
                studentId: searchStudId.value || undefined
            }
        }) as any
        tableData.value = res.data.records
        total.value = res.data.total
        resolveStudents(tableData.value)
    } finally { loading.value = false }
}

const resolveStudents = async (list: any[]) => {
    const ids = [...new Set(list.map(i => i.studentId))]
    for (const id of ids) {
        if (!studentMap.value[id]) {
            const res = await request.get(`/user/${id}`) as any
            if (res.data) studentMap.value[id] = res.data.name || res.data.username
        }
    }
}

const handleScore = (row: any) => {
    scoreForm.id = row.id
    scoreForm.score = row.score || 0
    scoreForm.comment = row.comment || ''
    dialogVisible.value = true
}

const viewWork = (url: string) => window.open(url, '_blank')

const confirmScore = async () => {
    await request.put('/work-submission', scoreForm)
    ElMessage.success('评分已提交')
    dialogVisible.value = false
    loadWorks()
}

const selectedIds = ref<number[]>([])
const handleSelectionChange = (val: any[]) => { selectedIds.value = val.map(i => i.id) }
const selectedIdsSub = ref<number[]>([])
const handleSelectionChangeSub = (val: any[]) => { selectedIdsSub.value = val.map(i => i.id) }

const openBatchScore = () => {
    batchScoreVisible.value = true
}

const confirmBatchScore = async () => {
    loading.value = true
    try {
        for (const id of selectedIdsSub.value) {
            await request.put('/work-submission', { id, score: batchScoreForm.score, comment: batchScoreForm.comment })
        }
        ElMessage.success('批量评分成功')
        batchScoreVisible.value = false
        loadWorks()
    } finally { loading.value = false }
}

onMounted(loadCompetitions)
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

.scoring-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f1f5f9;
}

.active-comp {
    font-weight: 700;
    color: #1e293b;
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

.dialog-form {
    padding: 10px 0;
}
</style>

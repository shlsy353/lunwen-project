<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Trophy />
                </el-icon>
                <span>获奖状况管理</span>
            </div>
        </div>

        <el-card class="table-card" shadow="never">
            <!-- Step 1: Competition List -->
            <template v-if="!selectedCompetition">
                <div class="search-bar">
                    <el-input v-model="searchCompName" placeholder="竞赛名称搜索..." clearable style="width: 200px" @clear="loadCompetitions" />
                    <el-button type="primary" @click="loadCompetitions" :icon="Search">查询</el-button>
                </div>
                <el-table :data="competitions" v-loading="loading" stripe border>
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="name" label="竞赛名称" />
                    <el-table-column prop="level" label="竞赛级别" width="100" />
                    <el-table-column label="操作" width="120" align="center">
                        <template #default="{ row }">
                            <el-button type="primary" link @click="enterAwards(row)">管理奖项</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </template>

            <!-- Step 2: Award List for Selected Competition -->
            <template v-else>
                <div class="scoring-header">
                    <el-button :icon="ArrowLeft" link @click="backToCompetitions">返回竞赛列表</el-button>
                    <span class="active-comp">正在管理：{{ selectedCompetition.name }}</span>
                    <el-button type="success" :icon="Plus" size="small" @click="handleAdd">授予奖项</el-button>
                </div>
                <el-table :data="tableData" v-loading="loading" stripe border @selection-change="handleSelectionChange">
                    <el-table-column type="selection" width="55" />
                    <el-table-column label="获奖学生" width="150">
                        <template #default="{ row }">
                            {{ studentMap[row.studentId] || ('ID: ' + row.studentId) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="awardLevel" label="奖项等级" width="120" align="center">
                        <template #default="{ row }">
                            <el-tag :type="awardType(row.awardLevel)">{{ row.awardLevel }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="awardDetails" label="获奖详情" min-width="200" show-overflow-tooltip />
                    <el-table-column prop="createTime" label="授予时间" width="180" align="center" />
                    <el-table-column label="操作" width="120" align="center" fixed="right">
                        <template #default="{ row }">
                            <el-button link type="danger" @click="handleDelete(row)">撤销</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </template>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="selectedCompetition ? loadAwards() : loadCompetitions()" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" title="授予竞赛奖项" width="500px">
            <el-form :model="form" label-width="100px" class="dialog-form">
                <el-form-item label="选择获奖者" required>
                    <el-select v-model="form.studentIds" multiple collapse-tags collapse-tags-tooltip placeholder="选择已评分的参赛者" filterable style="width: 100%">
                        <el-option v-for="s in scoredStudents" :key="s.studentId" 
                            :label="`${studentMap[s.studentId] || s.studentId} - (成绩: ${s.score})`" 
                            :value="s.studentId" />
                    </el-select>
                    <div class="award-utils" style="margin-top: 8px; display: flex; gap: 8px;">
                        <el-button size="small" @click="selectTop(3)">勾选前3名</el-button>
                        <el-button size="small" @click="selectTop(5)">勾选前5名</el-button>
                        <el-button size="small" @click="form.studentIds = []">尝试清除全部</el-button>
                    </div>
                </el-form-item>
                <el-form-item label="奖项等级" required>
                    <el-select v-model="form.awardLevel" placeholder="选择奖项等级" style="width: 100%">
                        <el-option label="特等奖" value="特等奖" />
                        <el-option label="一等奖" value="一等奖" />
                        <el-option label="二等奖" value="二等奖" />
                        <el-option label="三等奖" value="三等奖" />
                        <el-option label="优秀奖" value="优秀奖" />
                    </el-select>
                </el-form-item>
                <el-form-item label="详细说明">
                    <el-input v-model="form.awardDetails" type="textarea" placeholder="填写具体获奖理由或证书编号..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="save">确认授予</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Trophy, ArrowLeft } from '@element-plus/icons-vue'

const loading = ref(false)
const competitions = ref([])
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompName = ref('')
const selectedCompetition = ref<any>(null)
const studentMap = ref<Record<number, string>>({})
const selectedIds = ref<number[]>([])

const dialogVisible = ref(false)
const scoredStudents = ref<any[]>([])
const form = reactive({ competitionId: undefined as any, studentIds: [] as any[], awardLevel: '', awardDetails: '' })

const awardType = (level: string) => {
    const map: any = { '特等奖': 'danger', '一等奖': 'warning', '二等奖': '', '三等奖': 'success', '优秀奖': 'info' }
    return map[level] || 'info'
}

const handleSelectionChange = (val: any[]) => { selectedIds.value = val.map(i => i.id) }

const loadCompetitions = async () => {
    loading.value = true
    try {
        const res = await request.get('/competition/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, name: searchCompName.value }
        }) as any
        if (res.code === 200) { competitions.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const enterAwards = (comp: any) => {
    selectedCompetition.value = comp
    pageNum.value = 1
    loadAwards()
}

const backToCompetitions = () => {
    selectedCompetition.value = null
    pageNum.value = 1
    loadCompetitions()
}

const loadAwards = async () => {
    loading.value = true
    try {
        const res = await request.get('/award/page', {
            params: { competitionId: selectedCompetition.value.id, pageNum: pageNum.value, pageSize: pageSize.value }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
            resolveStudents(tableData.value)
        }
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

const handleAdd = async () => {
    form.competitionId = selectedCompetition.value.id
    form.studentIds = []
    form.awardLevel = ''
    form.awardDetails = ''
    
    // Fetch work submissions to get scores (as in ScoringManagement/Score table)
    // Actually, based on previous code, it fetches from '/score/page'
    const res = await request.get('/score/page', { 
        params: { competitionId: selectedCompetition.value.id, pageSize: 500 } 
    }) as any
    if (res.code === 200) {
        scoredStudents.value = res.data.records.sort((a: any, b: any) => b.score - a.score)
        resolveStudents(scoredStudents.value)
    }
    dialogVisible.value = true
}

const selectTop = (n: number) => {
    form.studentIds = scoredStudents.value.slice(0, n).map(s => s.studentId)
}

const save = async () => {
    if (form.studentIds.length === 0 || !form.awardLevel) return ElMessage.warning('请确保选择了获奖者和等级')
    
    loading.value = true
    try {
        for (const sid of form.studentIds) {
            await request.post('/award', {
                competitionId: form.competitionId,
                studentId: sid,
                awardLevel: form.awardLevel,
                awardDetails: form.awardDetails
            })
        }
        ElMessage.success(`成功为 ${form.studentIds.length} 位学生授予奖项`)
        dialogVisible.value = false
        loadAwards()
    } finally { loading.value = false }
}

const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定撤销该项奖励吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/award/' + row.id)
        ElMessage.success('已撤销')
        loadAwards()
    })
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


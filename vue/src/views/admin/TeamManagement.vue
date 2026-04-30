<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <UserGroup />
                </el-icon>
                <span>参赛名单管理</span>
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
                    <el-table-column prop="competitionDate" label="开展日期" width="120" />
                    <el-table-column label="操作" width="120" align="center">
                        <template #default="{ row }">
                            <el-button type="primary" link @click="enterParticipants(row)">查看名单</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </template>

            <!-- Step 2: Participant List for Selected Competition -->
            <template v-else>
                <div class="scoring-header">
                    <el-button :icon="ArrowLeft" link @click="backToCompetitions">返回竞赛列表</el-button>
                    <span class="active-comp">参赛名单：{{ selectedCompetition.name }}</span>
                </div>
                <div class="search-bar">
                    <el-input v-model="searchStudId" placeholder="学生ID搜索..." clearable style="width: 160px" @clear="loadParticipants" />
                    <el-button type="primary" @click="loadParticipants" :icon="Search">查询</el-button>
                    <el-button type="danger" @click="handleBatchDelete" :disabled="!selectedIds.length">批量解散/移除</el-button>
                </div>
                <el-table :data="tableData" v-loading="loading" stripe border @selection-change="handleSelectionChange">
                    <el-table-column type="selection" width="55" />
                    <el-table-column prop="id" label="报名ID" width="80" align="center" />
                    <el-table-column label="学生姓名" width="150">
                        <template #default="{ row }">
                            {{ studentMap[row.studentId] || ('ID: ' + row.studentId) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="状态" width="120" align="center">
                        <template #default="{ row }">
                            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="报名时间" width="180" align="center" />
                    <el-table-column label="操作" width="120" align="center" fixed="right">
                        <template #default="{ row }">
                            <el-button link type="danger" @click="handleDelete(row)">移除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </template>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="selectedCompetition ? loadParticipants() : loadCompetitions()" />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, UserFilled as UserGroup, ArrowLeft } from '@element-plus/icons-vue'

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
const selectedIds = ref<number[]>([])

const statusText = (s: number) => ['待审核', '已参赛', '已拒绝', '已晋级'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger', 'primary'] as const)[s] ?? 'info'

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

const enterParticipants = (comp: any) => {
    selectedCompetition.value = comp
    pageNum.value = 1
    loadParticipants()
}

const backToCompetitions = () => {
    selectedCompetition.value = null
    pageNum.value = 1
    loadCompetitions()
}

const loadParticipants = async () => {
    loading.value = true
    try {
        const res = await request.get('/registration/page', {
            params: { 
                competitionId: selectedCompetition.value.id, 
                pageNum: pageNum.value, 
                pageSize: pageSize.value,
                studentId: searchStudId.value || undefined
            }
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

const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定将该报名信息从名单中移除吗？', '警告', { type: 'error' }).then(async () => {
        await request.delete('/registration/' + row.id)
        ElMessage.success('已移除')
        loadParticipants()
    })
}

const handleBatchDelete = () => {
    ElMessageBox.confirm(`确定移除选中的 ${selectedIds.value.length} 条报名吗？`, '批量操作', { type: 'error' }).then(async () => {
        for (const id of selectedIds.value) {
            await request.delete('/registration/' + id)
        }
        ElMessage.success('批量移除完成')
        loadParticipants()
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
</style>


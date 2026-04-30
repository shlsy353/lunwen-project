<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <UserGroup />
                </el-icon>
                <span>参赛队伍管理</span>
            </div>
            <el-button type="success" @click="handleAdd" :icon="Plus">创建新队伍</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchKey" placeholder="搜索队伍名称..." clearable style="width: 200px" />
                <el-button type="success" @click="load" :icon="Search">查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="name" label="队伍名称" min-width="150" />
                <el-table-column prop="createTime" label="创建日期" width="180" align="center" />
                <el-table-column label="操作" width="150" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="success" @click="handleManageMembers(row)">成员管理</el-button>
                        <el-divider direction="vertical" />
                        <el-button link type="danger" @click="handleDelete(row)">解散</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <!-- Create Team Dialog -->
        <el-dialog v-model="dialogVisible" title="创建竞赛队伍" width="400px">
            <el-form :model="form" class="dialog-form" label-width="80px">
                <el-form-item label="队伍名称">
                    <el-input v-model="form.name" placeholder="请输入队伍名称" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="save">确定创建</el-button>
            </template>
        </el-dialog>

        <!-- Member Management Dialog -->
        <el-dialog v-model="memberDialogVisible" :title="'成员管理 - ' + currentTeam.name" width="700px">
            <div class="member-action-bar" style="display: flex; gap: 10px; margin-bottom: 20px;">
                <el-select v-model="memberForm.studentId" placeholder="搜索学生..." filterable style="flex: 1">
                    <el-option v-for="u in students" :key="u.id" :label="`${u.name} (${u.username})`" :value="u.id" />
                </el-select>
                <el-input v-model="memberForm.role" placeholder="队伍角色" style="width: 140px" />
                <el-button type="success" @click="addMember" :icon="Plus">添加成员</el-button>
            </div>

            <el-table :data="members" border stripe size="small">
                <el-table-column prop="studentId" label="学号/ID" width="100" align="center" />
                <el-table-column label="姓名" min-width="120">
                    <template #default="{ row }">{{students.find(u => u.id === row.studentId)?.name || '加载中...'
                        }}</template>
                </el-table-column>
                <el-table-column label="角色" width="120" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.role === '队长' ? 'danger' : ''" size="small">{{ row.role }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="100" align="center">
                    <template #default="{ row }">
                        <el-button link type="danger" @click="removeMember(row.id)">移除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, UserFilled as UserGroup } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKey = ref('')
const dialogVisible = ref(false)
const memberDialogVisible = ref(false)
const students = ref<any[]>([])
const members = ref<any[]>([])
const currentTeam = ref<any>({})

const form = reactive({ name: '' })
const memberForm = reactive({ studentId: undefined, role: '成员' })

const reset = () => { searchKey.value = ''; load() }

const load = async () => {
    loading.value = true
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
        const res = await request.get('/team/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, name: searchKey.value || undefined, leaderId: user.id }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const fetchStudents = async () => {
    const res = await request.get('/user/page', { params: { role: 'STUDENT', pageSize: 500 } }) as any
    if (res.code === 200) students.value = res.data.records
}

const handleAdd = () => { form.name = ''; dialogVisible.value = true }

const save = async () => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    await request.post('/team', { name: form.name, leaderId: user.id })
    ElMessage.success('队伍创建成功')
    dialogVisible.value = false
    load()
}

const handleDelete = (row: any) => {
    ElMessageBox.confirm(`确定解散 【${row.name}】？团队成员关联将被移除。`, '警告', { type: 'error' }).then(async () => {
        await request.delete('/team/' + row.id)
        ElMessage.success('队伍已解散')
        load()
    })
}

const handleManageMembers = async (team: any) => {
    currentTeam.value = team
    await fetchTeamMembers(team.id)
    memberDialogVisible.value = true
}

const fetchTeamMembers = async (teamId: number) => {
    const res = await request.get('/team-member/list', { params: { teamId } }) as any
    if (res.code === 200) members.value = res.data
}

const addMember = async () => {
    if (!memberForm.studentId) return ElMessage.warning('请选择学生')
    await request.post('/team-member', { ...memberForm, teamId: currentTeam.value.id })
    ElMessage.success('成员添加成功')
    memberForm.studentId = undefined
    await fetchTeamMembers(currentTeam.value.id)
}

const removeMember = async (id: number) => {
    await request.delete('/team-member/' + id)
    ElMessage.success('已移出队伍')
    await fetchTeamMembers(currentTeam.value.id)
}

onMounted(() => { load(); fetchStudents() })
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

.dialog-form {
    padding: 10px 0;
}
</style>

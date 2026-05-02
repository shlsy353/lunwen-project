<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { Search, User, Edit, Delete, Avatar, Plus } from '@element-plus/icons-vue'
import type { College } from '@/types'

const colleges = ref<College[]>([])
const fetchColleges = async () => {
    const res = await request.get('/college/list') as any
    if (res.code === 200) colleges.value = res.data
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchName = ref('')
const selectedIds = ref<number[]>([])

const mainTab = ref('list') // 'list' or 'approval'
const pendingLeaders = ref([])
const pendingTotal = ref(0)
const pPage = ref(1)

const studentDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentTeacher = ref<any>({})
const currentTeacherStudents = ref<any[]>([])
const searchAllStudentsTerm = ref('')
const searchAllStudentsResult = ref<any[]>([])
const activeTab = ref('students')
const teacherProjects = ref<any[]>([])
const projectsLoading = ref(false)

const editForm = reactive({ id: undefined, name: '', username: '', password: '', specialtyField: '', role: '', teacherId: '', collegeId: undefined, avatar: '' })

const manageStudents = async (row: any) => {
    currentTeacher.value = row
    studentDialogVisible.value = true
    activeTab.value = 'students'
    loadTeacherStudents()
    loadTeacherProjects()
}

const loadTeacherStudents = async () => {
    const res = await request.get('/student-leader/my-students', { params: { leaderId: currentTeacher.value.id } }) as any
    if (res.code === 200) currentTeacherStudents.value = res.data
}

const loadTeacherProjects = async () => {
    projectsLoading.value = true
    try {
        const res = await request.get('/registration/page', { params: { leaderId: currentTeacher.value.id, pageSize: 100 } }) as any
        if (res.code === 200) {
            teacherProjects.value = res.data.records
            for (const p of teacherProjects.value) {
                if (!p.competitionName) {
                    const compRes = await request.get(`/competition/${p.competitionId}`) as any
                    if (compRes.code === 200) p.competitionName = compRes.data.name
                }
                if (!p.studentName) {
                    const stuRes = await request.get(`/user/${p.studentId}`) as any
                    if (stuRes.code === 200) p.studentName = stuRes.data.name || stuRes.data.username
                }
            }
        }
    } finally { projectsLoading.value = false }
}

const searchAllStudentsAdmin = async () => {
    const res = await request.get('/user/page', { params: { name: searchAllStudentsTerm.value, role: 'STUDENT', pageSize: 50 } }) as any
    if (res.code === 200) {
        const associatedIds = currentTeacherStudents.value.map(s => s.id)
        searchAllStudentsResult.value = res.data.records.filter((s: any) => !associatedIds.includes(s.id))
    }
}

const addStudentToTeacher = async (student: any) => {
    await request.post('/student-leader/add', { studentId: student.id, leaderId: currentTeacher.value.id })
    ElMessage.success('关联成功')
    loadTeacherStudents()
    searchAllStudentsResult.value = []
}

const removeStudentFromTeacher = (student: any) => {
    ElMessageBox.confirm(`确定解除教师与学生 ${student.name} 的关联吗？`, '操作确认', { type: 'warning' }).then(async () => {
        await request.delete(`/student-leader/remove?leaderId=${currentTeacher.value.id}&studentId=${student.id}`)
        ElMessage.success('已解除')
        loadTeacherStudents()
    }).catch(() => { })
}

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/user/page', {
            params: {
                pageNum: currentPage.value,
                pageSize: pageSize.value,
                username: searchName.value || undefined,
                name: searchName.value || undefined,
                role: 'TEACHER,LEADER'
            }
        }) as any
        tableData.value = res.data.records
        total.value = res.data.total
    } finally {
        loading.value = false
    }
}

const fetchPending = async () => {
    loading.value = true
    try {
        const res = await request.get('/user/page', {
            params: {
                pageNum: pPage.value,
                pageSize: pageSize.value,
                leaderStatus: 1 // Pending
            }
        }) as any
        if (res.code === 200) {
            pendingLeaders.value = res.data.records
            pendingTotal.value = res.data.total
        }
    } finally { loading.value = false }
}

const handleApproveLeader = async (row: any, status: number) => {
    try {
        // status 2 = Approved (Leader role), 0 = Rejected
        const role = status === 2 ? 'LEADER' : 'TEACHER'
        await request.put('/user', { ...row, leaderStatus: status, role })
        ElMessage.success(status === 2 ? '已批准领队资格' : '已拒绝申请')
        fetchPending()
    } catch (e) { ElMessage.error('操作失败') }
}

const handleAddTeacher = () => {
    Object.assign(editForm, { id: undefined, name: '', username: '', password: '', specialtyField: '', role: 'TEACHER', teacherId: '', collegeId: undefined, avatar: '' })
    editDialogVisible.value = true
}

/**
 * 头像上传成功后的回调
 */
const handleAvatarSuccess = (res: any) => {
    if (res.code === 200) {
        editForm.avatar = res.data
        ElMessage.success('头像上传成功')
    } else {
        ElMessage.error(res.msg || '头像上传失败')
    }
}

/**
 * 头像上传前的校验
 */
const beforeAvatarUpload = (rawFile: any) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('头像必须是 JPG 或 PNG 格式!')
        return false
    } else if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error('头像大小不能超过 2MB!')
        return false
    }
    return true
}

const handleEditTeacher = (row: any) => {
    Object.assign(editForm, row)
    editDialogVisible.value = true
}

const saveTeacherEdit = async () => {
    // 如果角色不是领队，重置领队状态，保持数据一致性
    if (editForm.role !== 'LEADER') {
        editForm.leaderStatus = 0
    }
    if (editForm.id) {
        await request.put('/user', editForm)
        ElMessage.success('保存成功')
    } else {
        await request.post('/user', editForm)
        ElMessage.success('创建成功')
    }
    editDialogVisible.value = false
    fetchData()
}

const handleSelectionChange = (val: any[]) => {
    selectedIds.value = val.map(item => item.id)
}

const handleDelete = (id: number) => {
    ElMessageBox.confirm('确定删除该教师账号吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete(`/user/${id}`)
        ElMessage.success('已删除')
        fetchData()
    })
}

const handleBatchDelete = () => {
    if (selectedIds.value.length === 0) return ElMessage.warning('请选择要删除的项目')
    ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个教师账号吗？`, '批量删除', {
        type: 'warning'
    }).then(async () => {
        for (const id of selectedIds.value) {
            await request.delete(`/user/${id}`)
        }
        ElMessage.success('批量成功')
        fetchData()
    })
}

onMounted(() => {
    fetchData()
    fetchColleges()
})
</script>

<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Avatar />
                </el-icon>
                <span>教师与其资质管理</span>
            </div>
            <div class="actions">
                <el-input v-model="searchName" placeholder="搜索姓名/工号/账号" style="width: 240px" clearable
                    @clear="fetchData" @keyup.enter="fetchData" />
                <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
                <el-button type="success" :icon="Plus" @click="handleAddTeacher">添加教师</el-button>
                <el-button type="danger" :icon="Delete" @click="handleBatchDelete"
                    :disabled="!selectedIds.length">批量删除</el-button>
            </div>
        </div>

        <div class="main-tabs-container">
            <el-tabs v-model="mainTab" type="card"
                @tab-change="(t: any) => t === 'approval' ? fetchPending() : fetchData()">
                <el-tab-pane label="教师列表" name="list">
                    <el-card class="premium-card" shadow="never">
                        <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border
                            stripe>
                            <el-table-column type="selection" width="55" />
                            <el-table-column prop="id" label="ID" width="80" align="center" />
                            <el-table-column label="头像" width="70" align="center">
                                <template #default="{ row }">
                                    <el-avatar :size="30" :src="row.avatar">{{ (row.name || row.username || '?')[0]
                                    }}</el-avatar>
                                </template>
                            </el-table-column>
                            <el-table-column prop="name" label="姓名" width="120" />
                            <el-table-column prop="username" label="工号/账号" width="150" />
                            <el-table-column prop="specialtyField" label="研究方向/特长" show-overflow-tooltip />
                            <el-table-column prop="role" label="职能角色" width="120" align="center">
                                <template #default="{ row }">
                                    <el-tag :type="row.role === 'LEADER' ? 'success' : 'warning'">
                                        {{ row.role === 'LEADER' ? '领队老师' : '普通教师' }}
                                    </el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="220" align="center">
                                <template #default="{ row }">
                                    <el-button type="primary" link @click="handleEditTeacher(row)">编辑</el-button>
                                    <el-button type="success" link @click="manageStudents(row)">关联学生</el-button>
                                    <el-button type="danger" link :icon="Delete"
                                        @click="handleDelete(row.id)">删除</el-button>
                                </template>
                            </el-table-column>
                        </el-table>

                        <div class="pagination">
                            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                                :total="total" layout="total, prev, pager, next" @current-change="fetchData" />
                        </div>
                    </el-card>
                </el-tab-pane>

                <el-tab-pane label="领队资格审核" name="approval">
                    <el-card class="premium-card" shadow="never">
                        <el-table v-loading="loading" :data="pendingLeaders" border stripe>
                            <el-table-column type="index" label="序号" width="60" align="center" />
                            <el-table-column prop="name" label="教师姓名" width="150" />
                            <el-table-column prop="username" label="工号" width="150" />
                            <el-table-column prop="specialtyField" label="研究方向" show-overflow-tooltip />
                            <el-table-column label="操作" width="200" align="center">
                                <template #default="{ row }">
                                    <el-button type="success" @click="handleApproveLeader(row, 2)">批准资格</el-button>
                                    <el-button type="danger" plain @click="handleApproveLeader(row, 0)">拒绝</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div class="pagination">
                            <el-pagination v-model:current-page="pPage" :page-size="pageSize" :total="pendingTotal"
                                layout="total, prev, pager, next" @current-change="fetchPending" />
                        </div>
                    </el-card>
                </el-tab-pane>
            </el-tabs>
        </div>

        <!-- Edit Teacher Dialog -->
        <el-dialog v-model="editDialogVisible" title="修改教师信息" width="500px" class="premium-dialog">
            <el-form :model="editForm" label-width="100px" style="padding-top: 10px;">
                <el-form-item label="头像">
                    <el-upload class="avatar-uploader" action="/api/file/uploadHead" :show-file-list="false"
                        :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
                        <el-avatar v-if="editForm.avatar" :size="80" :src="editForm.avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
                <el-form-item label="姓名">
                    <el-input v-model="editForm.name" />
                </el-form-item>
                <el-form-item label="密码">
                    <el-input v-model="editForm.password" placeholder="不修改请留空" show-password />
                </el-form-item>
                <el-form-item label="工号/账号">
                    <el-input v-model="editForm.username" />
                </el-form-item>
                <el-form-item label="工号(显示用)">
                    <el-input v-model="editForm.teacherId" />
                </el-form-item>
                <el-form-item label="所属学院">
                    <el-select v-model="editForm.collegeId" style="width: 100%">
                        <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="研究方向">
                    <el-input v-model="editForm.specialtyField" type="textarea" :rows="2" />
                </el-form-item>
                <el-form-item label="职能角色">
                    <el-select v-model="editForm.role" style="width: 100%">
                        <el-option label="普通教师" value="TEACHER" />
                        <el-option label="领队老师" value="LEADER" />
                    </el-select>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="editDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="saveTeacherEdit">保存修改</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="studentDialogVisible" :title="'教师管理 - ' + currentTeacher.name" width="750px"
            class="premium-dialog">
            <el-tabs v-model="activeTab">
                <el-tab-pane label="关联学生" name="students">
                    <div style="margin-bottom: 20px; margin-top: 10px;">
                        <el-input v-model="searchAllStudentsTerm" placeholder="搜索并添加学生关联..." style="width: 100%"
                            @keyup.enter="searchAllStudentsAdmin" clearable>
                            <template #append><el-button :icon="Search" @click="searchAllStudentsAdmin" /></template>
                        </el-input>
                        <div v-if="searchAllStudentsResult.length" class="search-all-res">
                            <div v-for="s in searchAllStudentsResult" :key="s.id" class="search-all-item">
                                <span>{{ s.name }} ({{ s.username }})</span>
                                <el-button type="primary" size="small" @click="addStudentToTeacher(s)">添加关联</el-button>
                            </div>
                        </div>
                    </div>

                    <div class="dialog-section-title">当前关联学生：</div>
                    <el-table :data="currentTeacherStudents" max-height="300" stripe border>
                        <el-table-column prop="name" label="姓名" />
                        <el-table-column prop="username" label="学号/账号" />
                        <el-table-column label="操作" width="100" align="center">
                            <template #default="{ row }">
                                <el-button type="danger" link @click="removeStudentFromTeacher(row)">解除关联</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-tab-pane>

                <el-tab-pane label="指导项目/竞赛" name="projects">
                    <el-table v-loading="projectsLoading" :data="teacherProjects" max-height="400" stripe border>
                        <el-table-column prop="competitionName" label="竞赛名称" min-width="180" show-overflow-tooltip />
                        <el-table-column prop="studentName" label="参赛学生" width="120" />
                        <el-table-column prop="status" label="状态" width="100" align="center">
                            <template #default="{ row }">
                                <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                                    {{ ['待审核', '通过', '拒绝', '已取消'][row.status] || '未知' }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="100" align="center">
                            <template #default="{ row }">
                                <el-button type="primary" link @click="$router.push('/admin/scoring')">去评分</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                    <el-empty v-if="!teacherProjects.length" description="暂无指导项目" :image-size="60" />
                </el-tab-pane>
            </el-tabs>
        </el-dialog>
    </div>
</template>

<style scoped>
.page-container {
    padding: 20px;
    background: #f8fafc;
    min-height: calc(100vh - 84px);
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}

.page-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 24px;
    font-weight: 800;
    color: #1e293b;
}

.actions {
    display: flex;
    gap: 12px;
}

.premium-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

.search-all-res {
    margin-top: 12px;
    border: 1px solid #e2e8f0;
    padding: 8px;
    border-radius: 8px;
    max-height: 200px;
    overflow-y: auto;
    background: #fff;
}

.search-all-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px;
    border-bottom: 1px solid #f1f5f9;
}

.dialog-section-title {
    font-weight: 700;
    color: #475569;
    margin-bottom: 10px;
    font-size: 14px;
}

.premium-dialog {
    border-radius: 16px;
}

.avatar-uploader {
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 80px;
    height: 80px;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader:hover {
    border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 80px;
    height: 80px;
    text-align: center;
    line-height: 80px;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>

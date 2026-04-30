<template>
    <div class="page-container">
        <!-- Non-leader apply prompt -->
        <div v-if="!isLeader" class="apply-prompt-container">
            <el-card class="apply-card">
                <div class="apply-content">
                    <el-icon class="apply-icon">
                        <Lock />
                    </el-icon>
                    <h3>{{ user.leaderStatus === 1 ? '领队申请审核中' : '尚未获得领队资格' }}</h3>
                    <p>{{ user.leaderStatus === 1 ? '您的领队教师资格申请已提交，正在等待管理员审核。' : '获奖情况功能仅开放给领队老师，请先申请领队资格。' }}</p>
                    <div v-if="user.leaderStatus === 1" class="status-info">
                        <el-tag type="warning" size="large" effect="plain" round>
                            <el-icon>
                                <InfoFilled />
                            </el-icon> 审核中
                        </el-tag>
                    </div>
                    <el-button v-else type="primary" size="large" round
                        @click="$router.push('/teacher/profile')">前往个人中心申请</el-button>
                    <el-button v-if="user.leaderStatus === 1" plain size="large" round
                        @click="$router.push('/teacher/profile')" style="margin-top: 15px">查看申请进度</el-button>
                </div>
            </el-card>
        </div>

        <template v-else>
            <div class="page-header">
                <div class="page-title">
                    <el-icon class="title-icon">
                        <Trophy />
                    </el-icon>
                    <span>获奖情况总览</span>
                </div>
                <el-button type="success" @click="handleAdd" :icon="Plus">颁发奖励</el-button>
            </div>

            <el-card class="table-card" shadow="never">
                <div class="search-bar">
                    <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 160px" />
                    <el-input v-model="searchStudId" placeholder="学生ID..." clearable style="width: 160px" />
                    <el-button type="success" @click="load" :icon="Search">检索</el-button>
                    <el-button @click="reset">重置</el-button>
                </div>

                <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="competitionId" label="竞赛ID" width="100" align="center" />
                    <el-table-column prop="studentId" label="学生ID" width="100" align="center" />
                    <el-table-column prop="name" label="奖项名称" min-width="160" />
                    <el-table-column label="等级" width="120" align="center">
                        <template #default="{ row }">
                            <el-tag :type="awardType(row.awardLevel)">{{ row.awardLevel }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="description" label="颁奖词/说明" min-width="200" show-overflow-tooltip />
                    <el-table-column prop="createTime" label="颁发时间" width="180" />
                    <el-table-column label="操作" width="120" align="center" fixed="right">
                        <template #default="{ row }">
                            <el-button link type="danger" @click="handleDelete(row)">撤销</el-button>
                        </template>
                    </el-table-column>
                </el-table>

                <div class="pagination-bar">
                    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                        layout="total, prev, pager, next" @change="load" />
                </div>
            </el-card>

            <el-dialog v-model="dialogVisible" title="颁发奖项" width="500px">
                <el-form :model="form" ref="formRef" label-width="90px" class="dialog-form">
                    <el-form-item label="竞赛ID">
                        <el-input-number v-model="form.competitionId" :min="1" style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="学生ID">
                        <el-input-number v-model="form.studentId" :min="1" style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="奖项名称">
                        <el-input v-model="form.name" placeholder="如：最佳创意奖" />
                    </el-form-item>
                    <el-form-item label="等级">
                        <el-select v-model="form.awardLevel" placeholder="选择预设等级" style="width: 100%">
                            <el-option label="特等奖" value="特等奖" />
                            <el-option label="一等奖" value="一等奖" />
                            <el-option label="二等奖" value="二等奖" />
                            <el-option label="三等奖" value="三等奖" />
                            <el-option label="优秀奖" value="优秀奖" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="颁奖词">
                        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="填写说明..." />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="success" @click="save">确认颁发</el-button>
                </template>
            </el-dialog>
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Trophy, Lock, InfoFilled } from '@element-plus/icons-vue'

const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const isLeader = computed(() => user.value.role === 'LEADER' || user.value.leaderStatus === 2)

const fetchUserInfo = async () => {
    const res = await request.get('/user/info') as any
    if (res.code === 200) {
        user.value = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
    }
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')
const searchStudId = ref('')
const dialogVisible = ref(false)

const form = reactive({ competitionId: undefined, studentId: undefined, name: '', awardLevel: '', description: '' })

const awardType = (level: string) => {
    const map: any = { '特等奖': 'danger', '一等奖': 'warning', '二等奖': '', '三等奖': 'success', '优秀奖': 'info' }
    return map[level] || 'info'
}

const reset = () => { searchCompId.value = ''; searchStudId.value = ''; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/award/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, competitionId: searchCompId.value || undefined, studentId: searchStudId.value || undefined }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const handleAdd = () => { Object.assign(form, { competitionId: undefined, studentId: undefined, name: '', awardLevel: '', description: '' }); dialogVisible.value = true }
const handleDelete = (row: any) => {
    ElMessageBox.confirm('撤销此奖项后无法恢复，确定？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/award/' + row.id)
        ElMessage.success('已撤销'); load()
    })
}
const save = async () => {
    await request.post('/award', form)
    ElMessage.success('奖励已颁发'); dialogVisible.value = false; load()
}

onMounted(async () => {
    await fetchUserInfo()
    if (isLeader.value) {
        load()
    }
})
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

.apply-prompt-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 50vh;
}

.apply-card {
    width: 100%;
    max-width: 500px;
    border-radius: 20px;
    text-align: center;
    padding: 40px 20px;
    border: none;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.apply-icon {
    font-size: 64px;
    color: #f59e0b;
    margin-bottom: 20px;
    opacity: 0.8;
}

.apply-content h3 {
    font-size: 22px;
    color: #1e293b;
    margin-bottom: 12px;
}

.apply-content p {
    color: #64748b;
    margin-bottom: 30px;
    line-height: 1.6;
}
</style>

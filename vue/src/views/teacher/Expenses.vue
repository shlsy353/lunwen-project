<template>
    <div class="page-container">
        <div v-if="!isLeader" class="apply-prompt-container">
            <el-card class="apply-card">
                <div class="apply-content">
                    <el-icon class="apply-icon">
                        <Lock />
                    </el-icon>
                    <h3>{{ user.leaderStatus === 1 ? '领队申请审核中' : '尚未获得领队资格' }}</h3>
                    <p>{{ user.leaderStatus === 1 ? '您的领队教师资格申请已提交，正在等待管理员审核。' : '报销管理功能仅开放给领队老师，请先申请领队资格。' }}</p>
                    <div v-if="user.leaderStatus === 1">
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
                        <PriceTag />
                    </el-icon>
                    <span>报销清单管理</span>
                </div>
                <el-button type="primary" @click="handleAdd" :icon="Plus">申请报销</el-button>
            </div>

            <el-card class="table-card" shadow="never">
                <div class="search-bar">
                    <el-input v-model="searchItem" placeholder="报销项目..." clearable style="width: 200px" />
                    <el-select v-model="searchStatus" placeholder="审核状态" clearable style="width: 150px">
                        <el-option label="待审核" :value="0" />
                        <el-option label="已通过" :value="1" />
                        <el-option label="已驳回" :value="2" />
                    </el-select>
                    <el-button type="primary" @click="load" :icon="Search">查询</el-button>
                    <el-button @click="reset">重置</el-button>
                </div>

                <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="itemName" label="报销项" min-width="150" show-overflow-tooltip />
                    <el-table-column prop="amount" label="金额" width="120" align="right">
                        <template #default="{ row }">
                            <span style="color: #f43f5e; font-weight: 600">¥ {{ row.amount?.toFixed(2) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="审核状态" width="110" align="center">
                        <template #default="{ row }">
                            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
                    <el-table-column prop="createTime" label="申请日期" width="180" />
                    <el-table-column label="操作" width="130" align="center" fixed="right">
                        <template #default="{ row }">
                            <template v-if="row.status === 0">
                                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                                <el-divider direction="vertical" />
                                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                            </template>
                            <span v-else class="no-action">不可修改</span>
                        </template>
                    </el-table-column>
                </el-table>

                <div class="pagination-bar">
                    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                        layout="total, prev, pager, next" @change="load" />
                </div>
            </el-card>

            <el-dialog v-model="dialogVisible" :title="form.id ? '编辑报销申请' : '新建报销申请'" width="500px">
                <el-form :model="form" ref="formRef" label-width="80px" class="dialog-form">
                    <el-form-item label="报销项" prop="itemName">
                        <el-input v-model="form.itemName" placeholder="如：交通费、餐饮费" />
                    </el-form-item>
                    <el-form-item label="金额" prop="amount">
                        <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="描述" prop="description">
                        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="报销详细说明..." />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="save">提交报销</el-button>
                </template>
            </el-dialog>
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, PriceTag, Lock, InfoFilled } from '@element-plus/icons-vue'

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
const searchItem = ref('')
const searchStatus = ref(undefined)
const dialogVisible = ref(false)

const form = reactive({ id: undefined, itemName: '', amount: 0, description: '', status: 0 })

const statusText = (s: number) => ['待审核', '已通过', '已驳回'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger'] as const)[s] ?? 'info'

const reset = () => { searchItem.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/expense/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, itemName: searchItem.value || undefined, status: searchStatus.value }
        })
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const handleAdd = () => {
    Object.assign(form, { id: undefined, itemName: '', amount: 0, description: '', status: 0 })
    dialogVisible.value = true
}

const handleEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }

const handleDelete = (row: any) => {
    ElMessageBox.confirm('确认删除该报销申请？', '警告', { type: 'warning' }).then(async () => {
        await request.delete('/expense/' + row.id)
        ElMessage.success('已删除'); load()
    })
}

const save = async () => {
    if (form.id) { await request.put('/expense', form) } else { await request.post('/expense', form) }
    ElMessage.success('提交成功'); dialogVisible.value = false; load()
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
    color: #3b82f6;
}

.table-card {
    border-radius: 12px;
}

.search-bar {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
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

.dialog-form {
    padding: 10px 0;
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

<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Trophy />
                </el-icon>
                <span>竞赛信息管理</span>
            </div>
            <el-button type="success" @click="handleAdd" :icon="Plus">发布竞赛</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchName" placeholder="竞赛名称..." clearable style="width: 200px" />
                <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 140px">
                    <el-option label="报名中" :value="0" />
                    <el-option label="进行中" :value="1" />
                    <el-option label="已结束" :value="2" />
                </el-select>
                <el-button type="success" @click="load" :icon="Search">查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="name" label="竞赛名称" min-width="180" />
                <el-table-column prop="typeName" label="类型" width="120" align="center" />
                <el-table-column label="状态" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="startTime" label="开始时间" width="110" align="center" />
                <el-table-column prop="endTime" label="截止时间" width="110" align="center" />
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="success" @click="handleEdit(row)">编辑</el-button>
                        <el-divider direction="vertical" />
                        <el-button link type="warning" @click="handleStatus(row)">切换状态</el-button>
                        <el-divider direction="vertical" />
                        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑竞赛' : '发布新竞赛'" width="700px">
            <el-form :model="form" label-width="90px" class="dialog-form">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="竞赛名称" prop="name">
                            <el-input v-model="form.name" placeholder="请输入竞赛名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="竞赛类型" prop="typeId">
                            <el-select v-model="form.typeId" placeholder="选择竞赛类型" style="width:100%">
                                <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-form-item label="开始日期">
                            <el-date-picker v-model="form.startTime" type="date" value-format="YYYY-MM-DD"
                                style="width:100%" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="截止日期">
                            <el-date-picker v-model="form.endTime" type="date" value-format="YYYY-MM-DD"
                                style="width:100%" />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="竞赛描述">
                    <el-input v-model="form.description" type="textarea" :rows="4" placeholder="写一点关于竞赛的介绍..." />
                </el-form-item>
                <el-form-item label="奖励说明">
                    <el-input v-model="form.award" type="textarea" :rows="2" placeholder="设置奖项信息..." />
                </el-form-item>
                <el-form-item label="作品命名规范">
                    <el-input v-model="form.namingRule" placeholder="例如：学号_姓名_作品名.zip" />
                    <div style="font-size: 12px; color: #94a3b8; margin-top: 4px;">该规则将展示在学生提呈页面，提示学生按此规范命名。</div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="save">确定发布</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy, Plus, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const types = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchName = ref('')
const searchStatus = ref(undefined)
const dialogVisible = ref(false)

const form = reactive<any>({ id: undefined, name: '', typeId: undefined, description: '', award: '', startTime: '', endTime: '', status: 0 })

const statusText = (s: number) => ['报名中', '进行中', '已结束'][s] ?? '未知'
const statusType = (s: number) => (['success', 'primary', 'info'] as const)[s] ?? 'info'

const reset = () => { searchName.value = ''; searchStatus.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/competition/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, name: searchName.value || undefined, status: searchStatus.value }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const fetchTypes = async () => {
    const res = await request.get('/competition-type/list') as any
    if (res.code === 200) types.value = res.data
}

onMounted(() => { load(); fetchTypes() })

const handleAdd = () => { Object.assign(form, { id: undefined, name: '', typeId: undefined, description: '', award: '', startTime: '', endTime: '', status: 0 }); dialogVisible.value = true }
const handleEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const handleStatus = (row: any) => {
    const nextStatus = (row.status + 1) % 3
    ElMessageBox.confirm(`确定将竞赛状态切换为 ${statusText(nextStatus)}？`, '提示').then(async () => {
        await request.put('/competition', { id: row.id, status: nextStatus })
        ElMessage.success('修改成功'); load()
    })
}
const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定删除此竞赛？该操作不可撤销。', '预警', { type: 'error' }).then(async () => {
        await request.delete('/competition/' + row.id)
        ElMessage.success('已删除'); load()
    })
}
const save = async () => {
    if (form.id) { await request.put('/competition', form) } else { await request.post('/competition', form) }
    ElMessage.success('操作成功'); dialogVisible.value = false; load()
}
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

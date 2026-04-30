<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Trophy />
                </el-icon>
                <span>获奖情况管理</span>
            </div>
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增获奖</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 160px" />
                <el-input v-model="searchStudId" placeholder="学生ID..." clearable style="width: 160px" />
                <el-button type="primary" @click="load" :icon="Search">查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="competitionId" label="竞赛ID" width="90" align="center" />
                <el-table-column prop="studentId" label="学生ID" width="90" align="center" />
                <el-table-column prop="name" label="奖项名称" min-width="160" />
                <el-table-column label="奖项等级" width="120" align="center">
                    <template #default="{ row }">
                        <el-tag :type="awardType(row.awardLevel)">{{ row.awardLevel }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
                <el-table-column prop="createTime" label="颁发时间" width="180" />
                <el-table-column label="操作" width="130" align="center">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑获奖' : '新增获奖'" width="540px" destroy-on-close>
            <el-form :model="form" ref="formRef" label-width="90px" class="dialog-form">
                <el-form-item label="竞赛ID" prop="competitionId">
                    <el-input-number v-model="form.competitionId" :min="1" style="width: 100%" />
                </el-form-item>
                <el-form-item label="学生ID" prop="studentId">
                    <el-input-number v-model="form.studentId" :min="1" style="width: 100%" />
                </el-form-item>
                <el-form-item label="奖项名称" prop="name">
                    <el-input v-model="form.name" placeholder="如：一等奖" />
                </el-form-item>
                <el-form-item label="奖项等级">
                    <el-select v-model="form.awardLevel" placeholder="选择等级" style="width: 100%">
                        <el-option label="特等奖" value="特等奖" />
                        <el-option label="一等奖" value="一等奖" />
                        <el-option label="二等奖" value="二等奖" />
                        <el-option label="三等奖" value="三等奖" />
                        <el-option label="优秀奖" value="优秀奖" />
                    </el-select>
                </el-form-item>
                <el-form-item label="描述">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="获奖描述..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="save">确定保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Trophy } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')
const searchStudId = ref('')
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive<any>({ id: undefined, competitionId: undefined, studentId: undefined, name: '', awardLevel: '', description: '' })

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
        })
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const handleAdd = () => {
    Object.assign(form, { id: undefined, competitionId: undefined, studentId: undefined, name: '', awardLevel: '', description: '' })
    dialogVisible.value = true
}
const handleEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (row: any) => {
    ElMessageBox.confirm('确认删除此获奖记录？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/award/' + row.id)
        ElMessage.success('删除成功'); load()
    })
}
const save = async () => {
    if (form.id) { await request.put('/award', form) } else { await request.post('/award', form) }
    ElMessage.success('保存成功'); dialogVisible.value = false; load()
}

onMounted(load)
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

.dialog-form {
    padding: 10px 0;
}
</style>

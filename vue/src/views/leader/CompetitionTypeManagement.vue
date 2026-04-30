<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <List />
                </el-icon>
                <span>竞赛类型管理</span>
            </div>
            <el-button type="success" @click="handleAdd" :icon="Plus">新增竞赛类型</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchName" placeholder="搜索类型..." clearable style="width: 200px" />
                <el-button type="success" @click="load" :icon="Search">查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="name" label="类型名称" min-width="150" />
                <el-table-column prop="description" label="详细描述" min-width="250" show-overflow-tooltip />
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column label="操作" width="130" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="success" @click="handleEdit(row)">编辑</el-button>
                        <el-divider direction="vertical" />
                        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑竞赛类型' : '新增竞赛类型'" width="450px">
            <el-form :model="form" label-width="80px" class="dialog-form">
                <el-form-item label="类型名称">
                    <el-input v-model="form.name" placeholder="请输入类型名称" />
                </el-form-item>
                <el-form-item label="描述">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入说明..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="save">确定保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, List } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const searchName = ref('')
const dialogVisible = ref(false)
const form = reactive({ id: undefined, name: '', description: '' })

const reset = () => { searchName.value = ''; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/competition-type/list', { params: { name: searchName.value || undefined } }) as any
        if (res.code === 200) tableData.value = res.data
    } finally { loading.value = false }
}

const handleAdd = () => { Object.assign(form, { id: undefined, name: '', description: '' }); dialogVisible.value = true }
const handleEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定删除该竞赛类型？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/competition-type/' + row.id)
        ElMessage.success('已删除'); load()
    })
}
const save = async () => {
    if (form.id) { await request.put('/competition-type', form) } else { await request.post('/competition-type', form) }
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

.dialog-form {
    padding: 10px 0;
}
</style>

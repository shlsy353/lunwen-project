<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <List />
                </el-icon>
                <span>竞赛类型管理</span>
            </div>
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增类型</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="80" align="center" />
                <el-table-column prop="name" label="类型名称" min-width="200" />
                <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
                <el-table-column label="操作" width="160" align="center">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                        <el-divider direction="vertical" />
                        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑竞赛类型' : '新增竞赛类型'" width="480px" destroy-on-close>
            <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="dialog-form">
                <el-form-item label="类型名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入竞赛类型名称" />
                </el-form-item>
                <el-form-item label="描述">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述（可选）" />
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
import { Plus, List } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const form = reactive<any>({ id: undefined, name: '', description: '' })
const rules = {
    name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }]
}

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/competition-type/list')
        if (res.code === 200) tableData.value = res.data
    } finally {
        loading.value = false
    }
}

const handleAdd = () => {
    Object.assign(form, { id: undefined, name: '', description: '' })
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    Object.assign(form, row)
    dialogVisible.value = true
}

const handleDelete = (row: any) => {
    ElMessageBox.confirm('确认删除该竞赛类型？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/competition-type/' + row.id)
        ElMessage.success('删除成功')
        load()
    })
}

const save = async () => {
    await formRef.value?.validate()
    if (form.id) {
        await request.put('/competition-type', form)
    } else {
        await request.post('/competition-type', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
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

.data-table :deep(.el-table__header th) {
    background: #f8fafc;
    color: #475569;
    font-weight: 600;
}

.dialog-form {
    padding: 10px 0;
}
</style>

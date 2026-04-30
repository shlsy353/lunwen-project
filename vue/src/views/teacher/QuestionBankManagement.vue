<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Memo />
                </el-icon>
                <span>竞赛题库管理</span>
            </div>
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增题目</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchContent" placeholder="搜索题目内容..." clearable style="width: 280px"
                    :prefix-icon="Search" @change="load" />
                <el-select v-model="searchTypeId" placeholder="选择题目类型" clearable style="width: 180px" @change="load">
                    <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                </el-select>
                <el-button type="primary" @click="load" :icon="Search">搜索</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="content" label="题目内容" show-overflow-tooltip min-width="300" />
                <el-table-column label="题目类型" width="140">
                    <template #default="{ row }">
                        <el-tag>{{types.find(t => t.id === row.typeId)?.name || '未知'}}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="answer" label="参考答案" show-overflow-tooltip min-width="200" />
                <el-table-column label="操作" width="150" align="center">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                        <el-divider direction="vertical" />
                        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-bar">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
                    :total="total" layout="total, sizes, prev, pager, next, jumper" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="600px" destroy-on-close>
            <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="dialog-form">
                <el-form-item label="题目类型" prop="typeId">
                    <el-select v-model="form.typeId" placeholder="请选择题目类型" style="width: 100%">
                        <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="题目内容" prop="content">
                    <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入题目内容..." />
                </el-form-item>
                <el-form-item label="参考答案">
                    <el-input v-model="form.answer" type="textarea" :rows="3" placeholder="请输入参考答案..." />
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
import { Plus, Search, Memo } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchContent = ref('')
const searchTypeId = ref(undefined)
const dialogVisible = ref(false)
const formRef = ref()
const types = ref<any[]>([])

const form = reactive<any>({
    id: undefined,
    typeId: undefined,
    content: '',
    answer: ''
})

const rules = {
    typeId: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
    content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }]
}

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/question-bank/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                content: searchContent.value || undefined,
                typeId: searchTypeId.value || undefined
            }
        })
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
        }
    } finally {
        loading.value = false
    }
}

const fetchTypes = async () => {
    const res = await request.get('/question-type/list')
    if (res.code === 200) types.value = res.data
}

const handleAdd = () => {
    Object.assign(form, { id: undefined, typeId: undefined, content: '', answer: '' })
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    Object.assign(form, row)
    dialogVisible.value = true
}

const handleDelete = (row: any) => {
    ElMessageBox.confirm('确认删除该题目？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/question-bank/' + row.id)
        ElMessage.success('删除成功')
        load()
    })
}

const save = async () => {
    await formRef.value?.validate()
    if (form.id) {
        await request.put('/question-bank', form)
    } else {
        await request.post('/question-bank', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
}

onMounted(() => {
    load()
    fetchTypes()
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

.dialog-form {
    padding: 10px 0;
}
</style>

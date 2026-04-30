<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Memo />
                </el-icon>
                <span>竞赛题库管理</span>
            </div>
            <el-button type="success" @click="handleAdd" :icon="Plus">新增试题</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchName" placeholder="题名关键词..." clearable style="width: 200px" />
                <el-select v-model="searchType" placeholder="题目类型" clearable style="width: 150px">
                    <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                </el-select>
                <el-button type="success" @click="load" :icon="Search">查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="name" label="试题名称" min-width="180" />
                <el-table-column prop="typeName" label="类型" width="120" align="center" />
                <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip />
                <el-table-column prop="score" label="默认分值" width="100" align="center" />
                <el-table-column label="操作" width="130" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button link type="success" @click="handleEdit(row)">编辑</el-button>
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

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑试题' : '新增试题'" width="600px">
            <el-form :model="form" label-width="90px" class="dialog-form">
                <el-form-item label="题目名称">
                    <el-input v-model="form.name" placeholder="请输入题目简短名称" />
                </el-form-item>
                <el-form-item label="题目类型">
                    <el-select v-model="form.typeId" placeholder="选择所属类型" style="width: 100%">
                        <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="内容说明">
                    <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入题目详细说明或链接..." />
                </el-form-item>
                <el-form-item label="分值">
                    <el-input-number v-model="form.score" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="save" :icon="Check">立即保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Memo, Check } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const types = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchName = ref('')
const searchType = ref(undefined)
const dialogVisible = ref(false)
const form = reactive({ id: undefined, name: '', typeId: undefined, content: '', score: 10 })

const reset = () => { searchName.value = ''; searchType.value = undefined; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/question-bank/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, name: searchName.value || undefined, typeId: searchType.value }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const fetchTypes = async () => {
    const res = await request.get('/question-type/list') as any
    if (res.code === 200) types.value = res.data
}

onMounted(() => { load(); fetchTypes() })

const handleAdd = () => { Object.assign(form, { id: undefined, name: '', typeId: undefined, content: '', score: 10 }); dialogVisible.value = true }
const handleEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定从题库中移除此试题？', '警告', { type: 'warning' }).then(async () => {
        await request.delete('/question-bank/' + row.id)
        ElMessage.success('已移除'); load()
    })
}
const save = async () => {
    if (form.id) { await request.put('/question-bank', form) } else { await request.post('/question-bank', form) }
    ElMessage.success('保存成功'); dialogVisible.value = false; load()
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

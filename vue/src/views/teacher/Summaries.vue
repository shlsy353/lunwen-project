<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Document />
                </el-icon>
                <span>竞赛总结管理</span>
            </div>
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增总结</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchTitle" placeholder="搜索标题..." clearable style="width: 200px" />
                <el-input v-model="searchComp" placeholder="竞赛名称..." clearable style="width: 200px" />
                <el-button type="primary" @click="load" :icon="Search">查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="title" label="总结标题" min-width="180" show-overflow-tooltip />
                <el-table-column prop="competitionName" label="关联竞赛" width="180" show-overflow-tooltip />
                <el-table-column prop="content" label="总结内容" min-width="200" show-overflow-tooltip />
                <el-table-column label="附件" width="100" align="center">
                    <template #default="{ row }">
                        <el-link v-if="row.file" :href="row.file" target="_blank" type="primary" :underline="false">
                            <el-icon>
                                <Download />
                            </el-icon> 下载
                        </el-link>
                        <span v-else style="color: #94a3b8">无</span>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="提交时间" width="180" />
                <el-table-column label="操作" width="130" align="center" fixed="right">
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

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑总结' : '新增总结'" width="600px" destroy-on-close>
            <el-form :model="form" ref="formRef" label-width="100px" class="dialog-form">
                <el-form-item label="总结标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入总结标题" />
                </el-form-item>
                <el-form-item label="关联竞赛" prop="competitionId">
                    <el-select v-model="form.competitionId" placeholder="请选择竞赛" style="width: 100%">
                        <el-option v-for="c in competitions" :key="c.id" :label="c.name" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="总结内容" prop="content">
                    <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入总结内容..." />
                </el-form-item>
                <el-form-item label="上传附件">
                    <el-upload class="upload-demo" action="/api/file/upload" :headers="headers"
                        :on-success="handleUploadSuccess" :limit="1">
                        <el-button type="primary">点击上传</el-button>
                        <template #tip>
                            <div class="el-upload__tip">只能上传1个文件</div>
                        </template>
                    </el-upload>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="save">提交总结</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Document, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchTitle = ref('')
const searchComp = ref('')
const dialogVisible = ref(false)
const competitions = ref<any[]>([])
const formRef = ref()
const headers = { token: localStorage.getItem('token') }

const form = reactive<any>({
    id: undefined,
    title: '',
    competitionId: undefined,
    content: '',
    file: ''
})

const reset = () => {
    searchTitle.value = ''
    searchComp.value = ''
    load()
}

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/summary/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                title: searchTitle.value || undefined,
                competitionName: searchComp.value || undefined
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

const fetchComps = async () => {
    const res = await request.get('/competition/page', { params: { pageNum: 1, pageSize: 100 } })
    if (res.code === 200) competitions.value = res.data.records
}

const handleAdd = () => {
    Object.assign(form, { id: undefined, title: '', competitionId: undefined, content: '', file: '' })
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    Object.assign(form, row)
    dialogVisible.value = true
}

const handleDelete = (row: any) => {
    ElMessageBox.confirm('确认删除此总结记录？', '警告', { type: 'warning' }).then(async () => {
        await request.delete('/summary/' + row.id)
        ElMessage.success('删除成功')
        load()
    })
}

const handleUploadSuccess = (res: any) => {
    if (res.code === 200) {
        form.file = res.data
        ElMessage.success('上传成功')
    }
}

const save = async () => {
    if (form.id) {
        await request.put('/summary', form)
    } else {
        await request.post('/summary', form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
}

onMounted(() => {
    load()
    fetchComps()
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

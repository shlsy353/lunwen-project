<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Document />
                </el-icon>
                <span>竞赛总结管理</span>
            </div>
            <el-button type="success" @click="handleAdd" :icon="Plus">新建总结</el-button>
        </div>

        <el-card class="table-card" shadow="never">
            <div class="search-bar">
                <el-input v-model="searchTitle" placeholder="搜索标题..." clearable style="width: 200px" />
                <el-button type="success" @click="load" :icon="Search">检索查询</el-button>
                <el-button @click="reset">重置</el-button>
            </div>

            <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="title" label="总结标题" min-width="180" show-overflow-tooltip />
                <el-table-column prop="competitionName" label="关联竞赛" width="180" show-overflow-tooltip />
                <el-table-column prop="content" label="内容摘要" min-width="200" show-overflow-tooltip />
                <el-table-column label="附件" width="100" align="center">
                    <template #default="{ row }">
                        <el-link v-if="row.file" :href="row.file" target="_blank" type="success" :underline="false">
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

        <el-dialog v-model="dialogVisible" :title="form.id ? '编辑竞赛总结' : '新建竞赛总结'" width="600px">
            <el-form :model="form" label-width="90px" class="dialog-form">
                <el-form-item label="标题">
                    <el-input v-model="form.title" placeholder="总结标题..." />
                </el-form-item>
                <el-form-item label="竞赛项目">
                    <el-select v-model="form.competitionId" placeholder="关联竞赛项目" style="width:100%">
                        <el-option v-for="c in competitions" :key="c.id" :label="c.name" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="正文内容">
                    <el-input v-model="form.content" type="textarea" :rows="6" placeholder="输入总结详情..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="success" @click="save">保存归档</el-button>
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
const dialogVisible = ref(false)
const competitions = ref<any[]>([])

const form = reactive({ id: undefined, title: '', competitionId: undefined, content: '', file: '' })

const reset = () => { searchTitle.value = ''; load() }

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/summary/page', { params: { pageNum: pageNum.value, pageSize: pageSize.value, title: searchTitle.value || undefined } }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const fetchComps = async () => {
    const res = await request.get('/competition/page', { params: { pageSize: 50 } }) as any
    if (res.code === 200) competitions.value = res.data.records
}

onMounted(() => { load(); fetchComps() })

const handleAdd = () => { Object.assign(form, { id: undefined, title: '', competitionId: undefined, content: '', file: '' }); dialogVisible.value = true }
const handleEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定删除此总结？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/summary/' + row.id)
        ElMessage.success('已删除'); load()
    })
}
const save = async () => {
    if (form.id) { await request.put('/summary', form) } else { await request.post('/summary', form) }
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

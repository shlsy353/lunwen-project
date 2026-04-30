<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Competition, CompetitionType, College } from '@/types'

// ... standard CRUD logic ...
// For brevity, I will implement a simplified version and can expand later if needed.
// This page manages Competitions.

const loading = ref(false)
const tableData = ref<Competition[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchName = ref('')
const token = localStorage.getItem('token') // Add token for upload headers

const typeData = ref<CompetitionType[]>([])

const dialogVisible = ref(false)
const form = reactive<Partial<Competition>>({
  name: '',
  typeId: undefined,
  purpose: '',
  content: '',
  competitionDate: '',
  location: '',
  level: '',
  organizer: '',
  undertaker: '',
  status: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/competition/page', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        name: searchName.value
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const fetchTypes = async () => {
  const res = await request.get('/competition-type/list')
  typeData.value = res.data
}

const handleEdit = (row: Competition) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    name: '',
    typeId: undefined,
    status: 0
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
    if (form.id) {
        await request.put('/competition', form)
    } else {
        await request.post('/competition', form)
    }
    dialogVisible.value = false
    fetchData()
    ElMessage.success('Success')
}

const handleDelete = async (row: Competition) => {
    ElMessageBox.confirm(`确定结束 "${row.name}" 竞赛吗？结束记录将保留但不可再报名。`, '操作确认', {
        confirmButtonText: '确定结束',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        await request.put('/competition', { ...row, status: 3 })
        fetchData()
        ElMessage.success('竞赛已标记为结束')
    })
}

const selectedIds = ref<number[]>([])
const handleSelectionChange = (val: any[]) => {
    selectedIds.value = val.map(item => item.id)
}

const handleBatchDelete = () => {
    ElMessageBox.confirm(`确定要将选中的 ${selectedIds.value.length} 条竞赛全部标记为“已结束”吗？`, '批量操作').then(async () => {
        for (const id of selectedIds.value) {
            const row = tableData.value.find(t => t.id === id)
            if (row) await request.put('/competition', { ...row, status: 3 })
        }
        ElMessage.success('批量结束任务已完成')
        fetchData()
    })
}

onMounted(() => {
    fetchData()
    fetchTypes()
})
</script>

<template>
    <div>
        <div class="search-bar">
            <el-input v-model="searchName" placeholder="按名称搜索" style="width: 200px; margin-right: 10px" clearable @clear="fetchData" />
            <el-button type="primary" @click="fetchData">查询</el-button>
            <el-button type="success" @click="handleAdd">新增竞赛</el-button>
            <el-button type="warning" @click="handleBatchDelete" :disabled="!selectedIds.length">批量结束</el-button>
        </div>
        
        <el-table :data="tableData" v-loading="loading" border @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="竞赛名称" />
            <el-table-column prop="competitionDate" label="日期" width="120" />
            <el-table-column label="类型">
                <template #default="{row}">
                    {{ typeData.find(t => t.id === row.typeId)?.name }}
                </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
                 <template #default="{row}">
                    <el-tag v-if="row.status === 0" type="info">草稿</el-tag>
                    <el-tag v-else-if="row.status === 1" type="success">报名中</el-tag>
                    <el-tag v-else-if="row.status === 2" type="warning">评分中</el-tag>
                    <el-tag v-else-if="row.status === 3" type="danger">已结束</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="150" align="center">
                <template #default="{row}">
                    <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                    <el-button v-if="row.status !== 3" link type="warning" @click="handleDelete(row)">结束</el-button>
                </template>
            </el-table-column>
        </el-table>

        <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
                layout="total, prev, pager, next" @current-change="fetchData" />
        </div>

         <el-dialog v-model="dialogVisible" title="竞赛信息">
            <el-form :model="form" label-width="120px">
                <el-form-item label="名称">
                    <el-input v-model="form.name" />
                </el-form-item>
                <el-form-item label="类型">
                    <el-select v-model="form.typeId">
                        <el-option v-for="t in typeData" :key="t.id" :label="t.name" :value="t.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="日期">
                    <el-date-picker v-model="form.competitionDate" type="date" value-format="YYYY-MM-DD" />
                </el-form-item>
                 <el-form-item label="状态">
                     <el-select v-model="form.status" style="width: 100%">
                         <el-option :value="0" label="草稿" />
                         <el-option :value="1" label="报名中" />
                         <el-option :value="2" label="评分中" />
                         <el-option :value="3" label="已结束" />
                     </el-select>
                </el-form-item>
                <el-form-item label="地点">
                    <el-input v-model="form.location" />
                </el-form-item>
                <el-form-item label="级别">
                    <el-select v-model="form.level">
                         <el-option label="校级" value="校级" />
                         <el-option label="市级" value="市级" />
                         <el-option label="省级" value="省级" />
                         <el-option label="国家级" value="国家级" />
                    </el-select>
                </el-form-item>
                <el-form-item label="主办方">
                    <el-input v-model="form.organizer" />
                </el-form-item>
                <el-form-item label="承办方">
                    <el-input v-model="form.undertaker" />
                </el-form-item>
                <el-form-item label="附件">
                     <el-upload
                        action="/api/file/upload"
                        :limit="1"
                        :on-success="(res: any) => form.file = res.data"
                        :headers="{ token: token }"
                      >
                        <el-button type="primary">点击上传</el-button>
                      </el-upload>
                </el-form-item>
                <el-form-item label="目的">
                    <el-input v-model="form.purpose" type="textarea" />
                </el-form-item>
                <el-form-item label="内容">
                    <el-input v-model="form.content" type="textarea" rows="4" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.search-bar { padding: 20px; background: #fff; margin-bottom: 20px; }
</style>

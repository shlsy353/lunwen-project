<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon">
          <Calendar />
        </el-icon>
        <span>往年成绩管理</span>
      </div>
      <el-button type="primary" @click="handleAdd" :icon="Plus">新增记录</el-button>
    </div>

    <el-card class="table-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="searchComp" placeholder="竞赛名称..." clearable style="width: 200px" />
        <el-input v-model="searchTeam" placeholder="团队名称..." clearable style="width: 200px" />
        <el-button type="primary" @click="load" :icon="Search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="competitionName" label="竞赛名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="competitionType" label="竞赛类型" width="130" align="center" />
        <el-table-column prop="date" label="获奖日期" width="120" align="center" />
        <el-table-column prop="teamName" label="团队/选手" min-width="130" show-overflow-tooltip />
        <el-table-column prop="score" label="获得成绩/奖项" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span style="font-weight: 600; color: #3b82f6">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="leaderTeacher" label="指导教师" width="120" align="center" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑历史记录' : '新增历史记录'" width="600px">
      <el-form :model="form" label-width="90px" class="dialog-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="竞赛名称">
              <el-input v-model="form.competitionName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="竞赛类型">
              <el-input v-model="form.competitionType" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="获奖日期">
              <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队/选手">
              <el-input v-model="form.teamName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="成员名单">
          <el-input v-model="form.members" placeholder="多个成员请用逗号分隔" />
        </el-form-item>
        <el-form-item label="指导教师">
          <el-input v-model="form.leaderTeacher" />
        </el-form-item>
        <el-form-item label="获得成绩">
          <el-input v-model="form.score" placeholder="如：一等奖 / 98分" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchComp = ref('')
const searchTeam = ref('')
const dialogVisible = ref(false)

const form = reactive({
  id: undefined,
  competitionName: '',
  competitionType: '',
  date: '',
  teamName: '',
  members: '',
  leaderTeacher: '',
  score: ''
})

const reset = () => { searchComp.value = ''; searchTeam.value = ''; load() }

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/historical-record/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        competitionName: searchComp.value || undefined,
        teamName: searchTeam.value || undefined
      }
    })
    if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { id: undefined, competitionName: '', competitionType: '', date: '', teamName: '', members: '', leaderTeacher: '', score: '' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => { Object.assign(form, row); dialogVisible.value = true }

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该历史记录？', '提示', { type: 'warning' }).then(async () => {
    await request.delete('/historical-record/' + row.id)
    ElMessage.success('操作成功'); load()
  })
}

const save = async () => {
  if (form.id) { await request.put('/historical-record', form) } else { await request.post('/historical-record', form) }
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

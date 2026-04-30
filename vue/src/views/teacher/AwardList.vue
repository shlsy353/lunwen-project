<template>
  <div>
    <div style="margin-bottom: 20px">
      <el-button type="primary" @click="handleAdd">颁发奖项</el-button>
    </div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="competitionId" label="竞赛ID" />
      <el-table-column prop="studentId" label="学生ID" />
      <el-table-column prop="name" label="奖项名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="颁发奖项">
      <el-form :model="form" label-width="80px">
        <el-form-item label="竞赛ID">
          <el-input v-model="form.competitionId" />
        </el-form-item>
        <el-form-item label="学生ID">
          <el-input v-model="form.studentId" />
        </el-form-item>
        <el-form-item label="奖项名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: undefined,
  competitionId: '',
  studentId: '',
  name: '',
  description: ''
})

const load = () => {
  request.get('/award/page').then((res: any) => {
    if (res.code === 200) {
      tableData.value = res.data.records
    } else {
      ElMessage.error(res.message)
    }
  })
}

const handleAdd = () => {
  form.id = undefined
  form.competitionId = ''
  form.studentId = ''
  form.name = ''
  form.description = ''
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(() => {
    request.delete('/award/' + row.id).then((res: any) => {
      if (res.code === 200) {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.message)
      }
    })
  })
}

const save = () => {
  request.post('/award', form).then((res: any) => {
    if (res.code === 200) {
      ElMessage.success('操作成功')
      dialogVisible.value = false
      load()
    } else {
      ElMessage.error(res.message)
    }
  })
}

onMounted(() => {
  load()
})
</script>

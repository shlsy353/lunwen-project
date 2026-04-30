<template>
  <div>
    <div style="margin-bottom: 20px">
      <el-input
        v-model="competitionId"
        placeholder="请输入竞赛ID"
        style="width: 200px; margin-right: 10px"
      />
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="competitionId" label="竞赛ID" />
      <el-table-column prop="studentId" label="学生ID" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success"
            >已参赛</el-tag
          >
          <el-tag v-else-if="scope.row.status === 2" type="danger"
            >已拒绝</el-tag
          >
          <el-tag v-else-if="scope.row.status === 3" type="warning"
            >已晋级</el-tag
          >
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 1"
            size="small"
            type="primary"
            @click="handlePromote(scope.row)"
            >晋级</el-button
          >
          <span v-else>--</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const competitionId = ref('')

const load = () => {
  request
    .get('/registration/page', {
      params: {
        competitionId: competitionId.value || undefined,
        pageNum: 1,
        pageSize: 100
      }
    })
    .then((res: any) => {
      if (res.code === 200) {
        tableData.value = res.data.records
      } else {
        ElMessage.error(res.message)
      }
    })
}

const handlePromote = (row: any) => {
  ElMessageBox.confirm('确认设置该学生为晋级状态?', '提示', {
    type: 'warning'
  }).then(() => {
    // Correctly passing JSON body
    request
      .put('/registration', {
        id: row.id,
        status: 3
      })
      .then((res: any) => {
        if (res.code === 200) {
          ElMessage.success('操作成功')
          load()
        } else {
          ElMessage.error(res.message)
        }
      })
  })
}

onMounted(() => {
  load()
})
</script>

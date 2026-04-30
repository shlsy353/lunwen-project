<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import type { User } from '@/types'

const route = useRoute()
const competitionId = route.query.competitionId

// We need to list items to grade. Could be Teams or Works.
// Assuming we grade Works. 
// Step 1: Get works for this competition.
// Step 2: Show work link, current score, input to update score.

interface WorkSubmission {
    id: number
    title: string
    fileUrl: string
    studentId: number
    teamId?: number
    studentName?: string // Need to join
}

const works = ref<WorkSubmission[]>([])
const scoreDialog = ref(false)
const scoreForm = reactive({
    competitionId: competitionId,
    teamId: undefined as number | undefined,
    studentId: undefined as number | undefined,
    score: 0,
    comment: ''
})

const fetchWorks = async () => {
    const res = await request.get('/work-submission/page', { params: { competitionId, pageSize: 100 } })
    works.value = res.data.records
    // Currently works don't carry student/team names directly, we might see IDs only properly.
    // Ideally backend VOs handle this.
    // FRONTEND HACK: Fetch users to map names if I have access. Admin can, Teacher?
    // Let's assume teacher can see IDs or we just show "Submission ID" for MVP.
}

const handleScore = (row: WorkSubmission) => {
    scoreForm.teamId = row.teamId
    scoreForm.studentId = row.studentId
    scoreForm.score = 0
    scoreForm.comment = ''
    scoreDialog.value = true
}

const submitScore = async () => {
    await request.post('/score', scoreForm)
    ElMessage.success('评分成功')
    scoreDialog.value = false
}

onMounted(() => {
    fetchWorks()
})
</script>

<template>
  <div>
      <el-page-header content="评分详情" @back="$router.back()" style="margin-bottom: 20px" />
      
      <el-table :data="works" border>
          <el-table-column prop="title" label="作品标题" />
          <el-table-column label="附件">
              <template #default="{row}">
                   <el-link :href="row.fileUrl" target="_blank">下载查看</el-link>
              </template>
          </el-table-column>
          <el-table-column label="操作">
              <template #default="{row}">
                  <el-button type="primary" size="small" @click="handleScore(row)">评分</el-button>
              </template>
          </el-table-column>
      </el-table>

      <el-dialog v-model="scoreDialog" title="打分">
          <el-form :model="scoreForm">
              <el-form-item label="分数"><el-input-number v-model="scoreForm.score" :min="0" :max="100" /></el-form-item>
              <el-form-item label="评语"><el-input v-model="scoreForm.comment" type="textarea" /></el-form-item>
          </el-form>
          <template #footer><el-button type="primary" @click="submitScore">提交</el-button></template>
      </el-dialog>
  </div>
</template>

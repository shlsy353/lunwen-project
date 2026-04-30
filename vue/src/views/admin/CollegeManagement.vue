<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { College, Major } from '@/types'

const activeTab = ref('college')

// --- College Logic ---
const collegeData = ref<College[]>([])
const collegeDialogVisible = ref(false)
const collegeForm = reactive<Partial<College>>({ name: '', introduction: '' })

const fetchColleges = async () => {
  const res = await request.get('/college/list')
  collegeData.value = res.data
}

const handleAddCollege = () => {
    Object.assign(collegeForm, { id: undefined, name: '', introduction: '' })
    collegeDialogVisible.value = true
}

const handleEditCollege = (row: College) => {
    Object.assign(collegeForm, row)
    collegeDialogVisible.value = true
}

const handleDeleteCollege = async (id: number) => {
     await request.delete(`/college/${id}`)
     fetchColleges()
}

const submitCollege = async () => {
    if (collegeForm.id) await request.put('/college', collegeForm)
    else await request.post('/college', collegeForm)
    collegeDialogVisible.value = false
    fetchColleges()
    fetchMajors() // Refresh majors as college names might change
}

// --- Major Logic ---
const majorData = ref<Major[]>([])
const majorDialogVisible = ref(false)
const majorForm = reactive<Partial<Major>>({ name: '', collegeId: undefined, introduction: '' })

const fetchMajors = async () => {
    const res = await request.get('/major/list')
    majorData.value = res.data
}

const handleAddMajor = () => {
    Object.assign(majorForm, { id: undefined, name: '', collegeId: undefined, introduction: '' })
    majorDialogVisible.value = true
}

const handleEditMajor = (row: Major) => {
    Object.assign(majorForm, row)
    majorDialogVisible.value = true
}

const handleDeleteMajor = async (id: number) => {
    await request.delete(`/major/${id}`)
    fetchMajors()
}

const submitMajor = async () => {
    if (majorForm.id) await request.put('/major', majorForm)
    else await request.post('/major', majorForm)
    majorDialogVisible.value = false
    fetchMajors()
}

onMounted(() => {
    fetchColleges()
    fetchMajors()
})
</script>

<template>
  <div class="college-management">
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="学院管理" name="college">
          <div class="action-bar">
              <el-button type="primary" @click="handleAddCollege">新增学院</el-button>
          </div>
          <el-table :data="collegeData" border>
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="introduction" label="简介" />
              <el-table-column label="操作" width="150">
                  <template #default="{ row }">
                      <el-button link type="primary" @click="handleEditCollege(row)">编辑</el-button>
                      <el-button link type="danger" @click="handleDeleteCollege(row.id)">删除</el-button>
                  </template>
              </el-table-column>
          </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="专业管理" name="major">
           <div class="action-bar">
              <el-button type="primary" @click="handleAddMajor">新增专业</el-button>
          </div>
          <el-table :data="majorData" border>
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="name" label="名称" />
              <el-table-column label="所属学院">
                  <template #default="{ row }">
                      {{ collegeData.find(c => c.id === row.collegeId)?.name || row.collegeId }}
                  </template>
              </el-table-column>
              <el-table-column prop="introduction" label="简介" />
              <el-table-column label="操作" width="150">
                  <template #default="{ row }">
                      <el-button link type="primary" @click="handleEditMajor(row)">编辑</el-button>
                      <el-button link type="danger" @click="handleDeleteMajor(row.id)">删除</el-button>
                  </template>
              </el-table-column>
          </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Dialogs -->
    <el-dialog v-model="collegeDialogVisible" title="学院信息">
        <el-form :model="collegeForm" label-width="100px">
            <el-form-item label="名称"><el-input v-model="collegeForm.name" /></el-form-item>
            <el-form-item label="简介"><el-input v-model="collegeForm.introduction" type="textarea" /></el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="collegeDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitCollege">确定</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="majorDialogVisible" title="专业信息">
        <el-form :model="majorForm" label-width="100px">
            <el-form-item label="名称"><el-input v-model="majorForm.name" /></el-form-item>
            <el-form-item label="所属学院">
                <el-select v-model="majorForm.collegeId">
                    <el-option v-for="c in collegeData" :key="c.id" :label="c.name" :value="c.id" />
                </el-select>
            </el-form-item>
            <el-form-item label="简介"><el-input v-model="majorForm.introduction" type="textarea" /></el-form-item>
        </el-form>
        <template #footer>
             <el-button @click="majorDialogVisible = false">取消</el-button>
             <el-button type="primary" @click="submitMajor">确定</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.action-bar { margin-bottom: 20px; }
</style>

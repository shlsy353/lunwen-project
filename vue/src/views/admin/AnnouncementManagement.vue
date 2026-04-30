<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import type { Announcement, CompetitionType } from '@/types'

const activeTab = ref('announcement')

// --- Announcement ---
const announcementList = ref<Announcement[]>([])
const annDialogVisible = ref(false)
const annForm = reactive<Partial<Announcement>>({ title: '', content: '' })

const fetchAnnouncements = async () => {
    const res = await request.get('/announcement/page', { params: { pageSize: 100 } })
    announcementList.value = res.data.records
}
const handleSaveAnn = async () => {
    if (annForm.id) await request.put('/announcement', annForm)
    else await request.post('/announcement', annForm)
    annDialogVisible.value = false
    fetchAnnouncements()
}
const handleDelAnn = async (id: number) => {
    await request.delete(`/announcement/${id}`)
    fetchAnnouncements()
}

// --- Type ---
const typeList = ref<CompetitionType[]>([])
const typeDialogVisible = ref(false)
const typeForm = reactive<Partial<CompetitionType>>({ name: '' })

const fetchTypes = async () => {
    const res = await request.get('/competition-type/list')
    typeList.value = res.data
}
const handleSaveType = async () => {
    if (typeForm.id) await request.put('/competition-type', typeForm)
    else await request.post('/competition-type', typeForm)
    typeDialogVisible.value = false
    fetchTypes()
}
const handleDelType = async (id: number) => {
    await request.delete(`/competition-type/${id}`)
    fetchTypes()
}

onMounted(() => {
    fetchAnnouncements()
    fetchTypes()
})
</script>

<template>
  <div>
      <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="公告列表" name="announcement">
              <el-button type="primary" @click="() => { Object.assign(annForm, {id: undefined, title: '', content: ''}); annDialogVisible = true }">新增公告</el-button>
              <el-table :data="announcementList" style="margin-top:20px" border>
                  <el-table-column prop="title" label="标题" />
                  <el-table-column prop="createTime" label="发布时间" width="180" />
                  <el-table-column label="操作" width="150">
                      <template #default="{row}">
                          <el-button link type="primary" @click="() => { Object.assign(annForm, row); annDialogVisible = true }">编辑</el-button>
                           <el-button link type="danger" @click="handleDelAnn(row.id)">删除</el-button>
                      </template>
                  </el-table-column>
              </el-table>
          </el-tab-pane>
          <el-tab-pane label="竞赛类型" name="type">
              <el-button type="primary" @click="() => { Object.assign(typeForm, {id: undefined, name: ''}); typeDialogVisible = true }">新增类型</el-button>
              <el-table :data="typeList" style="margin-top:20px" border>
                  <el-table-column prop="id" label="ID" width="60" />
                  <el-table-column prop="name" label="名称" />
                  <el-table-column label="操作" width="150">
                       <template #default="{row}">
                          <el-button link type="primary" @click="() => { Object.assign(typeForm, row); typeDialogVisible = true }">编辑</el-button>
                           <el-button link type="danger" @click="handleDelType(row.id)">删除</el-button>
                      </template>
                  </el-table-column>
              </el-table>
          </el-tab-pane>
      </el-tabs>

      <!-- Ann Dialog -->
      <el-dialog v-model="annDialogVisible" title="公告信息">
          <el-form :model="annForm">
              <el-form-item label="标题"><el-input v-model="annForm.title" /></el-form-item>
              <el-form-item label="内容"><el-input v-model="annForm.content" type="textarea" /></el-form-item>
          </el-form>
          <template #footer><el-button type="primary" @click="handleSaveAnn">确定</el-button></template>
      </el-dialog>
      
       <!-- Type Dialog -->
      <el-dialog v-model="typeDialogVisible" title="竞赛类型">
          <el-form :model="typeForm">
              <el-form-item label="名称"><el-input v-model="typeForm.name" /></el-form-item>
          </el-form>
          <template #footer><el-button type="primary" @click="handleSaveType">确定</el-button></template>
      </el-dialog>
  </div>
</template>

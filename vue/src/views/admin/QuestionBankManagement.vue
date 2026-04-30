<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

interface QuestionType { id: number; name: string }
interface Question { id: number; content: string; typeId: number; answer?: string }

const activeTab = ref('bank')
const types = ref<QuestionType[]>([])
const questions = ref<Question[]>([])

const qDialog = ref(false)
const qForm = reactive<Partial<Question>>({ content: '', typeId: undefined, answer: '' })

const tDialog = ref(false)
const tForm = reactive<Partial<QuestionType>>({ name: '' })

// ... Fetch logic similar to others ...
const fetchTypes = async () => {
    const res = await request.get('/question-type/list')
    types.value = res.data
}
const fetchQuestions = async () => {
    const res = await request.get('/question-bank/page', { params: { pageSize: 100 } })
    questions.value = res.data.records
}

const saveQuestion = async () => {
    if (qForm.id) await request.put('/question-bank', qForm)
    else await request.post('/question-bank', qForm)
    qDialog.value = false
    fetchQuestions()
}

const saveType = async () => {
    await request.post('/question-type', tForm)
    tDialog.value = false
    fetchTypes()
}

onMounted(() => {
    fetchTypes()
    fetchQuestions()
})
</script>

<template>
  <div>
      <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="题库列表" name="bank">
               <el-button type="primary" @click="() => { Object.assign(qForm, {id:undefined}); qDialog = true }">新增题目</el-button>
               <el-table :data="questions" border style="margin-top:20px">
                   <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
                   <el-table-column label="类型">
                       <template #default="{row}">{{ types.find(t=>t.id===row.typeId)?.name }}</template>
                   </el-table-column>
                   <el-table-column label="操作">
                       <template #default="{row}">
                           <el-button link type="primary" @click="Object.assign(qForm, row); qDialog = true">编辑</el-button>
                       </template>
                   </el-table-column>
               </el-table>
          </el-tab-pane>
          <el-tab-pane label="题目类型" name="type">
               <el-button type="primary" @click="() => { Object.assign(tForm, {id:undefined}); tDialog = true }">新增类型</el-button>
               <el-table :data="types" border style="margin-top:20px">
                   <el-table-column prop="id" label="ID" />
                   <el-table-column prop="name" label="类型名称" />
               </el-table>
          </el-tab-pane>
      </el-tabs>

      <el-dialog v-model="qDialog" title="题目信息">
          <el-form :model="qForm" label-width="80px">
              <el-form-item label="类型">
                  <el-select v-model="qForm.typeId">
                      <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
                  </el-select>
              </el-form-item>
              <el-form-item label="内容"><el-input v-model="qForm.content" type="textarea" /></el-form-item>
              <el-form-item label="答案"><el-input v-model="qForm.answer" type="textarea" /></el-form-item>
          </el-form>
          <template #footer><el-button type="primary" @click="saveQuestion">确定</el-button></template>
      </el-dialog>

      <el-dialog v-model="tDialog" title="类型信息">
          <el-form :model="tForm"><el-form-item label="名称"><el-input v-model="tForm.name" /></el-form-item></el-form>
          <template #footer><el-button type="primary" @click="saveType">确定</el-button></template>
      </el-dialog>
  </div>
</template>

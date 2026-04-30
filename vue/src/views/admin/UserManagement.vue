<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'
import type { User, College, Major } from '@/types'

const loading = ref(false)
const tableData = ref<User[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  username: '',
  role: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const userForm = reactive<Partial<User>>({
  username: '',
  password: '',
  name: '',
  role: 'STUDENT',
  gender: 1,
  phone: '',
  email: '',
  studentId: '',
  teacherId: '',
  collegeId: undefined,
  majorId: undefined
})

const colleges = ref<College[]>([])
const majors = ref<Major[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/page', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        username: searchForm.username || undefined,
        role: searchForm.role || undefined
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchColleges = async () => {
  const res = await request.get('/college/list')
  colleges.value = res.data
}

// Watch fetch majors when college changes? 
// For simplicity, just fetch all majors or fetch dynamically.
const fetchMajors = async (collegeId?: number) => {
  if (!collegeId) {
    majors.value = []
    return
  }
  const res = await request.get('/major/list', { params: { collegeId } })
  majors.value = res.data
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.role = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(userForm, {
    id: undefined,
    username: '',
    password: '123456',
    name: '',
    role: 'STUDENT',
    gender: 1,
    phone: '',
    email: '',
    studentId: '',
    teacherId: '',
    collegeId: undefined,
    majorId: undefined
  })
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, row)
  // Clear password for security, dont send back hash
  userForm.password = ''
  if (row.collegeId) {
    fetchMajors(row.collegeId)
  }
  dialogVisible.value = true
}

const handleStatusChange = (row: User) => {
  const newStatus = row.isDeleted === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '禁用/锁定' : '激活/解锁'
  ElMessageBox.confirm(`确认将用户 ${row.username} ${actionText} 吗？`, '状态确认', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await request.put('/user', { id: row.id, isDeleted: newStatus }) as any
      if (res.code === 200) {
        ElMessage.success(`用户已成功${actionText}`)
        fetchData()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (err: any) {
      console.error(err)
      ElMessage.error('服务异常')
    }
  })
}

const handleSubmit = async () => {
  try {
    if (userForm.id) {
      await request.put('/user', userForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/user', userForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

const handleApproveLeader = (row: User) => {
  ElMessageBox.confirm(`确认通过 ${row.name || row.username} 的领队申请吗？`, '领队审核', {
    confirmButtonText: '通过',
    cancelButtonText: '拒绝',
    distinguishCancelAndClose: true,
    type: 'info'
  }).then(async () => {
    await request.put('/user', { ...row, leaderStatus: 2, role: 'LEADER' })
    ElMessage.success('已批准领队资格')
    fetchData()
  }).catch(async (action) => {
    if (action === 'cancel') {
      await request.put('/user', { ...row, leaderStatus: 0 })
      ElMessage.info('已拒绝申请')
      fetchData()
    }
  })
}

const selectedIds = ref<number[]>([])
const handleSelectionChange = (val: any[]) => {
  selectedIds.value = val.map(item => item.id)
}

const handleBatchStatus = (status: number) => {
  if (selectedIds.value.length === 0) return ElMessage.warning('请选择要操作的用户')
  const actionText = status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定批量${actionText}选中的 ${selectedIds.value.length} 个用户吗？`, '批量操作', {
    type: 'warning'
  }).then(async () => {
    for (const id of selectedIds.value) {
      await request.put('/user', { id, isDeleted: status })
    }
    ElMessage.success(`批量${actionText}完成`)
    fetchData()
  })
}

/**
 * 头像上传成功后的回调
 */
const handleAvatarSuccess = (res: any) => {
  if (res.code === 200) {
    userForm.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '头像上传失败')
  }
}

/**
 * 头像上传前的校验
 */
const beforeAvatarUpload = (rawFile: any) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像必须是 JPG 或 PNG 格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

onMounted(() => {
  fetchData()
  fetchColleges()
})
</script>

<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="选择角色" clearable style="width: 140px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="领队" value="LEADER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增用户</el-button>
          <el-button type="warning" @click="handleBatchStatus(1)" :disabled="!selectedIds.length">批量禁用</el-button>
          <el-button type="info" @click="handleBatchStatus(0)" :disabled="!selectedIds.length">批量启用</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="头像" width="70">
        <template #default="{ row }">
          <el-avatar :size="30" :src="row.avatar">{{ (row.name || row.username || '?')[0] }}</el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          <el-tag v-if="row.role === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else-if="row.role === 'TEACHER'" type="warning">教师</el-tag>
          <el-tag v-else-if="row.role === 'LEADER'" type="success">领队</el-tag>
          <el-tag v-else>学生</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="领队状态" width="120">
        <template #default="{ row }">
          <div v-if="row.role === 'TEACHER'">
            <el-tag v-if="row.leaderStatus === 1" type="warning" class="status-click"
              @click="handleApproveLeader(row)">待审核</el-tag>
            <el-tag v-else-if="row.leaderStatus === 2" type="success">已通过</el-tag>
            <span v-else class="text-secondary">-</span>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isDeleted === 1" type="danger">已锁定</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link :type="row.isDeleted === 1 ? 'success' : 'danger'" @click="handleStatusChange(row)">
            {{ row.isDeleted === 1 ? '解除锁定' : '禁用用户' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="头像">
          <el-upload class="avatar-uploader" action="/api/file/uploadHead" :show-file-list="false"
            :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
            <el-avatar v-if="userForm.avatar" :size="80" :src="userForm.avatar" />
            <el-icon v-else class="avatar-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="userForm.password" placeholder="留空则不修改(新增默认为123456)" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.name" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="领队" value="LEADER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>

        <el-form-item label="学院" v-if="userForm.role !== 'ADMIN'">
          <el-select v-model="userForm.collegeId" @change="fetchMajors" clearable>
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="专业" v-if="userForm.role === 'STUDENT'">
          <el-select v-model="userForm.majorId" clearable>
            <el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="学号/工号">
          <el-input v-if="userForm.role === 'STUDENT'" v-model="userForm.studentId" placeholder="学号" />
          <el-input v-else v-model="userForm.teacherId" placeholder="工号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-bar {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.status-click {
  cursor: pointer;
}

.text-secondary {
  color: #94a3b8;
  font-size: 12px;
}

.avatar-uploader {
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 80px;
  height: 80px;
  transition: var(--el-transition-duration-fast);
  margin-bottom: 10px;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 80px;
  height: 80px;
  text-align: center;
  line-height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>

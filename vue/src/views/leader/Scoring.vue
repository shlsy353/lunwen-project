<template>
    <div class="page-container">
        <!-- 非领队教师的申请引导提示区域 -->
        <div v-if="!isLeader" class="apply-prompt-container">
            <el-card class="apply-card">
                <div class="apply-content">
                    <el-icon class="apply-icon">
                        <Lock />
                    </el-icon>
                    <h3>{{ user.leaderStatus === 1 ? '领队申请审核中' : '尚未获得领队资格' }}</h3>
                    <p>{{ user.leaderStatus === 1 ? '您的领队教师资格申请已提交，正在等待管理员审核。' : '竞赛评分功能仅开放给领队老师，请先申请领队资格。' }}</p>
                    <div v-if="user.leaderStatus === 1">
                        <el-tag type="warning" size="large" effect="plain" round>
                            <el-icon>
                                <InfoFilled />
                            </el-icon> 审核中
                        </el-tag>
                    </div>
                    <el-button v-else type="primary" size="large" round
                        @click="$router.push('/teacher/profile')">前往个人中心申请</el-button>
                    <el-button v-if="user.leaderStatus === 1" plain size="large" round
                        @click="$router.push('/teacher/profile')" style="margin-top: 15px">查看申请进度</el-button>
                </div>
            </el-card>
        </div>

        <!-- 正式领队可操作的评分管理界面 -->
        <template v-else>
            <div class="page-header">
                <div class="page-title">
                    <el-icon class="title-icon">
                        <Edit />
                    </el-icon>
                    <span>竞赛评分管理</span>
                </div>
            </div>

            <el-card class="table-card" shadow="never">
                <!-- 多条件搜索栏 -->
                <div class="search-bar">
                    <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 160px" />
                    <el-input v-model="searchStudId" placeholder="学生ID..." clearable style="width: 160px" />
                    <el-button type="success" @click="load" :icon="Search">查询</el-button>
                    <el-button @click="reset">重置</el-button>
                </div>

                <!-- 作品提交列表表格 -->
                <el-table :data="tableData" v-loading="loading" stripe class="data-table">
                    <el-table-column type="index" label="序号" width="60" align="center" />
                    <el-table-column prop="competitionId" label="竞赛ID" width="100" align="center" />
                    <el-table-column prop="studentId" label="学生ID" width="100" align="center" />
                    <el-table-column prop="workFile" label="作品/备注" min-width="150" show-overflow-tooltip>
                        <template #default="{ row }">
                            <el-link v-if="row.workFile" :href="row.workFile" target="_blank"
                                type="success">查看作品</el-link>
                            <span v-else style="color: #94a3b8">未提交作品</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="目前得分" width="120" align="center">
                        <template #default="{ row }">
                            <span v-if="row.score" style="font-weight: 700; color: #16a34a">{{ row.score }}</span>
                            <span v-else style="color: #94a3b8">未评分</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="comment" label="评价意见" min-width="200" show-overflow-tooltip />
                    <el-table-column label="操作" width="120" align="center" fixed="right">
                        <template #default="{ row }">
                            <el-button link type="success" @click="handleScore(row)">打分</el-button>
                        </template>
                    </el-table-column>
                </el-table>

                <!-- 分页控制 -->
                <div class="pagination-bar">
                    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                        layout="total, prev, pager, next" @change="load" />
                </div>
            </el-card>

            <!-- 打分/评价编辑弹窗 -->
            <el-dialog v-model="dialogVisible" title="作品打分与评价" width="450px">
                <el-form :model="scoreForm" label-width="80px" class="dialog-form">
                    <el-form-item label="分数" prop="score">
                        <el-input-number v-model="scoreForm.score" :min="0" :max="100" style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="评价" prop="comment">
                        <el-input v-model="scoreForm.comment" type="textarea" :rows="3" placeholder="写一点评价反馈..." />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="success" @click="confirmScore">提交成绩</el-button>
                </template>
            </el-dialog>
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, Edit, Lock, InfoFilled } from '@element-plus/icons-vue'

// 从本地存储同步用户信息与角色权限判断
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
// 权限判断计算属性：具有领队角色或审核通过状态为 2
const isLeader = computed(() => user.value.role === 'LEADER' || user.value.leaderStatus === 2)

/**
 * 后端拉取用户最新信息，确保领队资格状态实时同步
 */
const fetchUserInfo = async () => {
    const res = await request.get('/user/info') as any
    if (res.code === 200) {
        user.value = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
    }
}

// 页面表格交互相关状态
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')
const searchStudId = ref('')
const dialogVisible = ref(false)
const scoreForm = reactive({ id: undefined, score: 0, comment: '' })

// 重置搜索条件
const reset = () => { searchCompId.value = ''; searchStudId.value = ''; load() }

/**
 * 分页加载作品提交列表
 */
const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/work-submission/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                competitionId: searchCompId.value || undefined,
                studentId: searchStudId.value || undefined
            }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
        }
    } finally { loading.value = false }
}

/**
 * 打开打分弹窗并初始化表单数据
 */
const handleScore = (row: any) => {
    scoreForm.id = row.id
    scoreForm.score = row.score || 0
    scoreForm.comment = row.comment || ''
    dialogVisible.value = true
}

/**
 * 提交打分结果给服务器
 */
const confirmScore = async () => {
    await request.put('/work-submission', scoreForm)
    ElMessage.success('评分已提交')
    dialogVisible.value = false
    load() // 刷新列表查看最新分数
}

// 挂载初期化逻辑
onMounted(async () => {
    await fetchUserInfo()
    if (isLeader.value) {
        load() // 仅领队加载数据
    }
})
</script>

<style scoped>
/* 样式部分 */
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

.apply-prompt-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 50vh;
}

.apply-card {
    width: 100%;
    max-width: 500px;
    border-radius: 20px;
    text-align: center;
    padding: 40px 20px;
    border: none;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.apply-icon {
    font-size: 64px;
    color: #f59e0b;
    margin-bottom: 20px;
    opacity: 0.8;
}

.apply-content h3 {
    font-size: 22px;
    color: #1e293b;
    margin-bottom: 12px;
}

.apply-content p {
    color: #64748b;
    margin-bottom: 30px;
    line-height: 1.6;
}
</style>

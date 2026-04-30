<template>
    <div class="page-container">
        <!-- 非领队教师权限受限提示 -->
        <div v-if="!isLeader" class="apply-prompt-container">
            <el-card class="apply-card">
                <div class="apply-content">
                    <el-icon class="apply-icon">
                        <Lock />
                    </el-icon>
                    <h3>{{ user.leaderStatus === 1 ? '领队申请审核中' : '尚未获得领队资格' }}</h3>
                    <p>{{ user.leaderStatus === 1 ? '您的领队教师资格申请已提交，正在等待管理员审核。' : '报名管理功能仅开放给领队老师，请先申请领队资格。' }}</p>
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

        <!-- 领队老师专属管理界面 -->
        <template v-else>
            <div class="page-header">
                <div class="page-title">
                    <el-icon class="title-icon">
                        <Tickets />
                    </el-icon>
                    <span>带领学生管理中心</span>
                </div>
                <el-button type="success" :icon="Plus" @click="handleAdd">代学生报名</el-button>
            </div>

            <el-tabs v-model="activeTab" class="custom-tabs">
                <!-- 选项卡 1：报名记录管理 -->
                <el-tab-pane label="报名管治" name="regs">
                    <el-card class="table-card" shadow="never">
                        <!-- 筛选查询区域 -->
                        <div class="search-bar">
                            <el-input v-model="searchCompId" placeholder="竞赛ID..." clearable style="width: 140px"
                                @clear="load" />
                            <el-input v-model="searchStudId" placeholder="学生ID..." clearable style="width: 140px"
                                @clear="load" />
                            <el-select v-model="searchStatus" placeholder="报名状态" clearable style="width: 130px"
                                @change="load">
                                <el-option label="待审核" :value="0" />
                                <el-option label="已参赛" :value="1" />
                                <el-option label="已拒绝" :value="2" />
                                <el-option label="已晋级" :value="3" />
                            </el-select>
                            <el-button type="primary" @click="load" :icon="Search">查询</el-button>
                            <el-button type="danger" @click="handleBatchDelete"
                                :disabled="!selectedIds.length">批量撤销</el-button>
                        </div>

                        <!-- 报名详情表格 -->
                        <el-table :data="tableData" v-loading="loading" stripe border
                            @selection-change="handleSelectionChange">
                            <el-table-column type="selection" width="55" />
                            <el-table-column label="竞赛名称" min-width="180">
                                <template #default="{ row }">
                                    {{ competitionMap[row.competitionId] || ('ID: ' + row.competitionId) }}
                                </template>
                            </el-table-column>
                            <el-table-column label="参赛学生" width="150">
                                <template #default="{ row }">
                                    {{ studentMap[row.studentId] || ('ID: ' + row.studentId) }}
                                </template>
                            </el-table-column>
                            <el-table-column label="报名状态" width="100" align="center">
                                <template #default="{ row }">
                                    <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
                                </template>
                            </el-table-column>
                            <el-table-column prop="createTime" label="报名时间" width="180" align="center" />
                            <el-table-column label="操作" width="120" align="center" fixed="right">
                                <template #default="{ row }">
                                    <el-button link type="danger" @click="handleDelete(row)">撤销</el-button>
                                </template>
                            </el-table-column>
                        </el-table>

                        <!-- 分页条 -->
                        <div class="pagination-bar">
                            <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                                layout="total, prev, pager, next" @current-change="load" />
                        </div>
                    </el-card>
                </el-tab-pane>

                <!-- 选项卡 2：成绩与获奖统计汇总 -->
                <el-tab-pane label="获得奖项与得分" name="results">
                    <el-card class="table-card" shadow="never">
                        <div class="search-bar">
                            <el-checkbox v-model="onlyAwarded" @change="loadResults">仅看获奖</el-checkbox>
                            <el-button :icon="Refresh" circle @click="loadResults" style="margin-left: 20px" />
                        </div>
                        <el-table :data="resultList" v-loading="loadingResults" stripe border>
                            <el-table-column label="竞赛名称" min-width="180">
                                <template #default="{ row }">
                                    {{ competitionMap[row.competitionId] || ('ID: ' + row.competitionId) }}
                                </template>
                            </el-table-column>
                            <el-table-column label="学生姓名" width="150">
                                <template #default="{ row }">
                                    {{ studentMap[row.studentId] || ('ID: ' + row.studentId) }}
                                </template>
                            </el-table-column>
                            <el-table-column label="最终得分" width="120" align="center">
                                <template #default="{ row }">
                                    <span v-if="scoreMap[`${row.competitionId}-${row.studentId}`] !== undefined"
                                        class="score-val">
                                        {{ scoreMap[`${row.competitionId}-${row.studentId}`] }}
                                    </span>
                                    <span v-else class="text-muted">-</span>
                                </template>
                            </el-table-column>
                            <el-table-column label="荣誉奖项" width="150" align="center">
                                <template #default="{ row }">
                                    <el-tag v-if="awardMap[`${row.competitionId}-${row.studentId}`]" type="warning"
                                        effect="dark">
                                        {{ awardMap[`${row.competitionId}-${row.studentId}`] }}
                                    </el-tag>
                                    <span v-else class="text-muted">未获奖项</span>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-card>
                </el-tab-pane>
            </el-tabs>

            <!-- 弹窗：代学生报名竞赛 -->
            <el-dialog v-model="dialogVisible" title="代学生报名竞赛" width="500px">
                <el-form :model="form" label-width="100px" class="dialog-form">
                    <el-form-item label="选择竞赛" required>
                        <el-select v-model="form.competitionId" placeholder="请选择竞赛" filterable style="width: 100%">
                            <el-option v-for="c in activeCompetitions" :key="c.id" :label="c.name" :value="c.id" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="选择学生" required>
                        <el-select v-model="form.studentId" placeholder="请选择我的学生" filterable style="width: 100%">
                            <el-option v-for="s in myStudents" :key="s.id" :label="`${s.name} (${s.username})`"
                                :value="s.id" />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="备注说明">
                        <el-input v-model="form.ledStudents" type="textarea" placeholder="填写团队成员或其他说明..." />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="success" @click="save">确认报名</el-button>
                </template>
            </el-dialog>
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Tickets, Plus, Refresh, Lock, InfoFilled } from '@element-plus/icons-vue'

const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const isLeader = computed(() => user.value.role === 'LEADER' || user.value.leaderStatus === 2)

/**
 * 实时同步教师最新身份状态
 */
const fetchUserInfo = async () => {
    const res = await request.get('/user/info') as any
    if (res.code === 200) {
        user.value = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
    }
}

// 页面基础响应式数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchCompId = ref('')
const searchStudId = ref('')
const searchStatus = ref(undefined)
const selectedIds = ref<number[]>([])

// 选项卡与统计相关数据
const activeTab = ref('regs')
const loadingResults = ref(false)
const resultList = ref([])
const onlyAwarded = ref(false)
const scoreMap = ref<Record<string, any>>({})
const awardMap = ref<Record<string, any>>({})

// 缓存映射表，避免重复请求
const studentMap = ref<Record<number, string>>({})
const competitionMap = ref<Record<number, string>>({})

// 表单控制
const dialogVisible = ref(false)
const myStudents = ref<any[]>([])
const activeCompetitions = ref<any[]>([])
const form = reactive({ competitionId: undefined, studentId: undefined, ledStudents: '' })

// 辅助转换函数
const statusText = (s: number) => ['待审核', '已参赛', '已拒绝', '已晋级'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger', 'primary'] as const)[s] ?? 'info'

// 记录多选 ID
const handleSelectionChange = (val: any[]) => { selectedIds.value = val.map(i => i.id) }

/**
 * 加载报名记录主表数据
 */
const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/registration/page', {
            params: {
                pageNum: pageNum.value,
                pageSize: pageSize.value,
                leaderId: user.value.id, // 核心逻辑：只查该老师名下的报名
                status: searchStatus.value,
                competitionId: searchCompId.value || undefined,
                studentId: searchStudId.value || undefined
            }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total
            resolveNames(tableData.value) // 转换 ID 为名称
        }
    } finally { loading.value = false }
}

/**
 * 加载成绩与奖项汇总（聚合查询逻辑）
 */
const loadResults = async () => {
    loadingResults.value = true
    try {
        const res = await request.get('/registration/page', {
            params: { pageNum: 1, pageSize: 100, leaderId: user.value.id, status: 1 } // 仅统计已确认参赛的数据
        }) as any
        if (res.code === 200) {
            let list = res.data.records
            await resolveNames(list)

            // 循环获取关联的得分和奖项详情
            for (const row of list) {
                const key = `${row.competitionId}-${row.studentId}`
                // 获取对应学生的赛事得分
                const sRes = await request.get('/score/page', {
                    params: { competitionId: row.competitionId, studentId: row.studentId, pageSize: 1 }
                }) as any
                if (sRes.data.records.length > 0) scoreMap.value[key] = sRes.data.records[0].score

                // 获取对应学生的赛事奖项
                const aRes = await request.get('/award/page', {
                    params: { competitionId: row.competitionId, studentId: row.studentId, pageSize: 1 }
                }) as any
                if (aRes.data.records.length > 0) awardMap.value[key] = aRes.data.records[0].awardLevel
            }

            // 本地根据获奖情况过滤展示
            if (onlyAwarded.value) {
                resultList.value = list.filter((r: any) => awardMap.value[`${r.competitionId}-${r.studentId}`])
            } else {
                resultList.value = list
            }
        }
    } finally { loadingResults.value = false }
}

/**
 * 名称解析函数：将学生/竞赛 ID 统一转换为可读文字
 */
const resolveNames = async (list: any[]) => {
    const sIds = [...new Set(list.map(i => i.studentId))]
    const cIds = [...new Set(list.map(i => i.competitionId))]

    for (const id of sIds) {
        if (!studentMap.value[id]) {
            const res = await request.get(`/user/${id}`) as any
            if (res.data) studentMap.value[id] = res.data.name || res.data.username
        }
    }
    for (const id of cIds) {
        if (!competitionMap.value[id]) {
            const res = await request.get(`/competition/${id}`) as any
            if (res.data) competitionMap.value[id] = res.data.name
        }
    }
}

/**
 * 代报名入口：加载必要的可选数据（自己的学生、开放中的赛事）
 */
const handleAdd = async () => {
    dialogVisible.value = true
    const sRes = await request.get('/student-leader/my-students', { params: { leaderId: user.value.id } }) as any
    if (sRes.code === 200) myStudents.value = sRes.data
    const cRes = await request.get('/competition/page', { params: { status: 1, pageSize: 100 } }) as any
    if (cRes.code === 200) activeCompetitions.value = cRes.data.records
}

/**
 * 确认代学生报名操作
 */
const save = async () => {
    if (!form.competitionId || !form.studentId) return ElMessage.warning('请选择竞赛和学生')
    await request.post('/registration', { ...form, leaderId: user.value.id })
    ElMessage.success('代报名成功')
    dialogVisible.value = false
    load()
}

/**
 * 撤销单条报名记录
 */
const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定要撤销该报名吗？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/registration/' + row.id)
        ElMessage.success('已撤销')
        load()
    })
}

/**
 * 批量处理：撤销多项报名
 */
const handleBatchDelete = () => {
    ElMessageBox.confirm(`确定撤销选中的 ${selectedIds.value.length} 条报名吗？`, '批量操作').then(async () => {
        for (const id of selectedIds.value) {
            await request.delete('/registration/' + id)
        }
        ElMessage.success('批量处理完成')
        load()
    })
}

// 标签切换联动加载
watch(activeTab, (val) => {
    if (val === 'results' && resultList.value.length === 0) loadResults()
})

onMounted(async () => {
    await fetchUserInfo()
    if (isLeader.value) {
        load()
    }
})
</script>

<style scoped>
/* 样式模块 */
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
    border: none;
}

.search-bar {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: 20px;
}

.pagination-bar {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: #f1f5f9;
}

.score-val {
    font-weight: 700;
    color: #2563eb;
}

.text-muted {
    color: #94a3b8;
    font-size: 13px;
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

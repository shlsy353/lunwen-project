<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Tickets />
                </el-icon>
                <span>我的报名记录</span>
            </div>
        </div>

        <el-card class="premium-card" shadow="never">
            <div v-if="loading" v-loading="loading" style="height: 300px"></div>
            <div v-else-if="tableData.length === 0" class="empty-state">
                <el-empty description="暂无报名记录，快去探索竞赛吧！">
                    <el-button type="primary" round @click="$router.push('/student/competitions')">探索竞赛</el-button>
                </el-empty>
            </div>
            <div v-else class="reg-list">
                <div v-for="item in tableData" :key="item.id" class="reg-card">
                    <div class="reg-info">
                        <div class="reg-comp-title">{{ competitionMap[item.competitionId] || ('加载中... #' +
                            item.competitionId) }}</div>
                        <div class="reg-status">
                            <el-tag :type="statusType(item.status)" effect="dark" size="small">{{
                                statusText(item.status) }}</el-tag>
                            <span class="reg-time"><el-icon>
                                    <Calendar />
                                </el-icon> {{ item.createTime?.substring(0, 10) }}</span>
                        </div>
                        <div v-if="item.reviewComment" class="reg-comment">
                            <el-icon>
                                <ChatDotRound />
                            </el-icon> 反馈：{{ item.reviewComment }}
                        </div>
                    </div>
                    <div class="reg-actions">
                        <el-button type="primary" round v-if="item.status === 1"
                            @click="$router.push('/student/works')">提交作品</el-button>
                        <el-button link type="danger" v-if="item.status === 0"
                            @click="handleCancel(item)">取消报名</el-button>
                        <el-button link type="info"
                            @click="$router.push('/competition/' + item.competitionId)">竞赛详情</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Tickets, Calendar, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const statusText = (s: number) => ['待审核', '已通过', '被驳回', '已晋级'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger', 'primary'] as const)[s] ?? 'info'

const competitionMap = ref<Record<number, string>>({})

const load = async () => {
    loading.value = true
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
        // Fetch registrations
        const res = await request.get('/registration/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, studentId: user.id }
        }) as any

        if (res.code === 200) {
            tableData.value = res.data.records;
            total.value = res.data.total

            // Collect unique competition IDs to fetch names
            const compIds = [...new Set(tableData.value.map((item: any) => item.competitionId))]
            for (const cid of compIds) {
                if (!competitionMap.value[cid]) {
                    const cRes = await request.get(`/competition/${cid}`) as any
                    if (cRes.code === 200) {
                        competitionMap.value[cid] = cRes.data.name
                    }
                }
            }
        }
    } finally { loading.value = false }
}

const handleCancel = (row: any) => {
    ElMessageBox.confirm('确定取消该项目的报名申请？', '提示', { type: 'warning' }).then(async () => {
        await request.delete('/registration/' + row.id)
        ElMessage.success('已取消')
        load()
    })
}

onMounted(load)
</script>

<style scoped>
.page-container {
    padding: 0;
}

.page-header {
    margin-bottom: 24px;
}

.page-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
}

.title-icon {
    font-size: 24px;
    color: #3b82f6;
}

.premium-card {
    border-radius: 16px;
    border: none;
    min-height: 50vh;
}

.reg-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.reg-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px;
    border-radius: 12px;
    border: 1px solid #f1f5f9;
    transition: all 0.3s;
}

.reg-card:hover {
    border-color: #3b82f6;
    background: #f8fafc;
}

.reg-info {
    flex: 1;
}

.reg-comp-title {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 8px;
}

.reg-status {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 8px;
}

.reg-time {
    font-size: 12px;
    color: #64748b;
    display: flex;
    align-items: center;
    gap: 4px;
}

.reg-comment {
    font-size: 13px;
    color: #ef4444;
    background: #fef2f2;
    padding: 8px 12px;
    border-radius: 6px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    margin-top: 4px;
    font-weight: 500;
}

.reg-actions {
    padding-left: 24px;
    display: flex;
    gap: 8px;
    align-items: center;
}

.pagination-bar {
    margin-top: 24px;
    display: flex;
    justify-content: center;
}
</style>

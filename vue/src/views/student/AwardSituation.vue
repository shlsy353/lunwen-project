<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Trophy />
                </el-icon>
                <span>我的获奖成就</span>
            </div>
        </div>

        <!-- Stats Summary -->
        <el-row :gutter="20" style="margin-bottom: 24px;">
            <el-col :span="8">
                <div class="achievement-stat primary">
                    <div class="stat-num">{{ tableData.length }}</div>
                    <div class="stat-label">总计获奖</div>
                </div>
            </el-col>
            <el-col :span="8">
                <div class="achievement-stat danger">
                    <div class="stat-num">{{ highAwardsCount }}</div>
                    <div class="stat-label">一等奖及以上</div>
                </div>
            </el-col>
            <el-col :span="8">
                <div class="achievement-stat success">
                    <div class="stat-num">{{ tableData.length > 0 ? '达标' : '加油' }}</div>
                    <div class="stat-label">综合评估</div>
                </div>
            </el-col>
        </el-row>

        <el-card class="premium-card archive-card" shadow="never">
            <div v-if="loading" v-loading="loading" style="height: 300px"></div>
            <div v-else-if="tableData.length === 0" class="empty-state">
                <el-empty description="目前还没有获得的奖项，继续努力！">
                    <el-button type="primary" round @click="$router.push('/student/competitions')">探索竞赛</el-button>
                </el-empty>
            </div>
            <div v-else class="award-grid">
                <div v-for="item in tableData" :key="item.id" class="award-item">
                    <div class="award-badge">{{ item.awardLevel }}</div>
                    <div class="award-info">
                        <div class="award-name">{{ competitionMap[item.competitionId] || '竞赛成就' }}</div>
                        <div class="award-level-text">荣获：{{ item.awardLevel }}</div>
                        <div class="award-desc">{{ item.awardDetails || '无详细说明' }}</div>
                    </div>
                    <div class="award-footer">
                        <span class="award-date"><el-icon>
                                <Calendar />
                            </el-icon> {{ item.createTime?.substring(0, 10) }}</span>
                        <el-link type="primary" :underline="false">查看证书预览 <el-icon>
                                <Right />
                            </el-icon></el-link>
                    </div>
                </div>
            </div>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <div class="achievement-footer">
            <el-icon>
                <Medal />
            </el-icon>
            <span>每一次获奖都是成长的见证，继续砥砺前行！</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { Trophy, Calendar, Medal, Right } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

const highAwardsCount = computed(() => {
    return tableData.value.filter(a => a.awardLevel === '特等奖' || a.awardLevel === '一等奖').length
})

const competitionMap = ref<Record<number, string>>({})

const load = async () => {
    loading.value = true
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
        const res = await request.get('/award/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, studentId: user.id }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total

            // Resolve competition names
            const compIds = [...new Set(tableData.value.map((item: any) => item.competitionId))]
            await Promise.all(compIds.map(async (cid) => {
                if (!competitionMap.value[cid]) {
                    const cRes = await request.get(`/competition/${cid}`) as any
                    if (cRes.code === 200) competitionMap.value[cid] = cRes.data.name
                }
            }))
        }
    } finally { loading.value = false }
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

.achievement-stat {
    padding: 24px;
    border-radius: 16px;
    text-align: center;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    color: #fff;
}

.achievement-stat.primary {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.achievement-stat.danger {
    background: linear-gradient(135deg, #f87171, #ef4444);
}

.achievement-stat.success {
    background: linear-gradient(135deg, #34d399, #10b981);
}

.stat-num {
    font-size: 32px;
    font-weight: 800;
    margin-bottom: 4px;
}

.stat-label {
    font-size: 13px;
    font-weight: 500;
    opacity: 0.9;
}

.premium-card {
    border-radius: 16px;
    border: none;
    min-height: 50vh;
}

.award-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
}

.award-item {
    border-radius: 16px;
    padding: 24px;
    border: 1px solid #f1f5f9;
    background: linear-gradient(135deg, #fefce8 0%, #fff 100%);
    transition: all 0.3s;
    position: relative;
    overflow: hidden;
}

.award-item:hover {
    transform: translateY(-5px);
    border-color: #fbbf24;
    box-shadow: 0 8px 24px rgba(251, 191, 36, 0.1);
}

.award-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    background: #fbbf24;
    color: #fff;
    font-size: 11px;
    font-weight: 800;
    padding: 4px 10px;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(251, 191, 36, 0.4);
}

.award-info {
    margin: 16px 0;
    min-height: 100px;
}

.award-name {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 8px;
}

.award-level-text {
    font-size: 14px;
    color: #3b82f6;
    margin-bottom: 12px;
    font-weight: 600;
}

.award-desc {
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
    font-style: italic;
}

.award-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #f1f5f9;
    padding-top: 16px;
}

.award-date {
    font-size: 12px;
    color: #94a3b8;
    display: flex;
    align-items: center;
    gap: 4px;
}

.pagination-bar {
    margin-top: 32px;
    display: flex;
    justify-content: center;
}

.achievement-footer {
    margin-top: 40px;
    text-align: center;
    color: #94a3b8;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}
</style>

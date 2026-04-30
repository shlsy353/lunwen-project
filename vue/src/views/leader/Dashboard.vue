<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <DataBoard />
                </el-icon>
                <span>首页</span>
            </div>
        </div>

        <el-row :gutter="20" class="stats-row">
            <el-col :xs="12" :sm="6">
                <div class="stat-card stat-green">
                    <div class="stat-icon"><el-icon>
                            <Trophy />
                        </el-icon></div>
                    <div class="stat-body">
                        <div class="stat-value">{{ stats.competitions }}</div>
                        <div class="stat-label">竞赛总数</div>
                    </div>
                </div>
            </el-col>
            <el-col :xs="12" :sm="6">
                <div class="stat-card stat-blue">
                    <div class="stat-icon"><el-icon>
                            <User />
                        </el-icon></div>
                    <div class="stat-body">
                        <div class="stat-value">{{ stats.teams }}</div>
                        <div class="stat-label">参赛队伍</div>
                    </div>
                </div>
            </el-col>
            <el-col :xs="12" :sm="6">
                <div class="stat-card stat-orange">
                    <div class="stat-icon"><el-icon>
                            <Medal />
                        </el-icon></div>
                    <div class="stat-body">
                        <div class="stat-value">{{ stats.awards }}</div>
                        <div class="stat-label">获奖总数</div>
                    </div>
                </div>
            </el-col>
            <el-col :xs="12" :sm="6">
                <div class="stat-card stat-purple">
                    <div class="stat-icon"><el-icon>
                            <Document />
                        </el-icon></div>
                    <div class="stat-body">
                        <div class="stat-value">{{ stats.summaries }}</div>
                        <div class="stat-label">总结报告</div>
                    </div>
                </div>
            </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :md="14">
                <el-card class="content-card" shadow="never">
                    <template #header>
                        <span class="card-title"><el-icon>
                                <Bell />
                            </el-icon> 通知公告</span>
                    </template>
                    <el-empty v-if="announcements.length === 0" description="暂无公告" />
                    <div v-else class="announcement-list">
                        <div v-for="item in announcements" :key="item.id" class="announcement-item">
                            <div class="anno-dot"></div>
                            <div class="anno-content">
                                <div class="anno-title">{{ item.title }}</div>
                                <div class="anno-time">{{ item.createTime?.substring(0, 10) }}</div>
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :md="10">
                <el-card class="welcome-card" shadow="never">
                    <div class="welcome-body">
                        <div class="welcome-avatar">{{ (user.name || user.username || '领')[0] }}</div>
                        <h3 class="welcome-name">{{ user.name || user.username }}</h3>
                        <p class="welcome-role">领队老师</p>
                        <div class="welcome-date">
                            <el-icon>
                                <Calendar />
                            </el-icon>
                            <span>{{ new Date().toLocaleDateString('zh-CN', {
                                year: 'numeric', month: 'long', day:
                                'numeric' })
                                }}</span>
                        </div>
                        <el-divider />
                        <div class="quick-actions">
                            <div class="qa-title">快捷操作</div>
                            <div class="qa-buttons">
                                <el-button plain @click="$router.push('/leader/teams')">队伍管理</el-button>
                                <el-button plain @click="$router.push('/leader/applications')">报名管理</el-button>
                                <el-button plain @click="$router.push('/leader/awards')">获奖情况</el-button>
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Trophy, User, Medal, Document, Bell, Calendar, DataBoard } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const announcements = ref<any[]>([])
const stats = ref({ competitions: 0, teams: 0, awards: 0, summaries: 0 })

onMounted(async () => {
    try {
        const [aRes, cRes, tRes, awRes] = await Promise.allSettled([
            request.get('/announcement/page', { params: { pageNum: 1, pageSize: 6 } }),
            request.get('/competition/page', { params: { pageSize: 1 } }),
            request.get('/team/page', { params: { pageSize: 1 } }),
            request.get('/award/page', { params: { pageSize: 1 } }),
        ])
        if (aRes.status === 'fulfilled') announcements.value = aRes.value.data?.records || []
        if (cRes.status === 'fulfilled') stats.value.competitions = cRes.value.data?.total || 0
        if (tRes.status === 'fulfilled') stats.value.teams = tRes.value.data?.total || 0
        if (awRes.status === 'fulfilled') stats.value.awards = awRes.value.data?.total || 0
    } catch (e) { console.error(e) }
})
</script>

<style scoped>
.page-container {
    padding: 0;
}

.page-header {
    display: flex;
    align-items: center;
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

.stats-row {
    margin-bottom: 4px;
}

.stat-card {
    border-radius: 12px;
    padding: 20px 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    color: #fff;
    margin-bottom: 16px;
    transition: transform 0.2s;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
    transform: translateY(-2px);
}

.stat-green {
    background: linear-gradient(135deg, #10b981, #059669);
}

.stat-blue {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.stat-orange {
    background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-purple {
    background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.stat-icon .el-icon {
    font-size: 36px;
}

.stat-value {
    font-size: 28px;
    font-weight: 700;
    line-height: 1;
}

.stat-label {
    font-size: 13px;
    opacity: 0.85;
    margin-top: 4px;
}

.content-card,
.welcome-card {
    border-radius: 12px;
}

.card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #1e293b;
    font-size: 15px;
}

.announcement-list {
    display: flex;
    flex-direction: column;
}

.announcement-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px 0;
    border-bottom: 1px solid #f1f5f9;
}

.announcement-item:last-child {
    border-bottom: none;
}

.anno-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #16a34a;
    flex-shrink: 0;
    margin-left: 4px;
}

.anno-content {
    flex: 1;
}

.anno-title {
    font-size: 14px;
    color: #374151;
    font-weight: 500;
}

.anno-time {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 2px;
}

.welcome-body {
    text-align: center;
    padding: 10px 0;
}

.welcome-avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: linear-gradient(135deg, #10b981, #059669);
    color: #fff;
    font-size: 28px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 14px;
}

.welcome-name {
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 4px;
}

.welcome-role {
    font-size: 13px;
    color: #64748b;
    margin: 0 0 14px;
}

.welcome-date {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 13px;
    color: #64748b;
}

.quick-actions {
    text-align: left;
}

.qa-title {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 10px;
    font-weight: 500;
}

.qa-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
}
</style>

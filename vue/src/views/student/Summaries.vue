<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Document />
                </el-icon>
                <span>竞赛公告与总结</span>
            </div>
        </div>

        <el-card class="premium-card" shadow="never">
            <div v-if="loading" v-loading="loading" style="height: 300px"></div>
            <div v-else-if="tableData.length === 0" class="empty-state">
                <el-empty description="暂无竞赛总结或公告信息" />
            </div>
            <div v-else class="summary-list">
                <el-row :gutter="20">
                    <el-col v-for="item in tableData" :key="item.id" :xs="24" :sm="12" :md="8">
                        <div class="summary-card" @click="viewDetail(item)">
                            <div class="s-comp-tag">{{ item.competitionName || '官方公告' }}</div>
                            <h3 class="s-title">{{ item.title }}</h3>
                            <p class="s-content">{{ item.content }}</p>
                            <div class="s-footer">
                                <div class="s-date"><el-icon>
                                        <Calendar />
                                    </el-icon> {{ item.createTime?.substring(0, 10) }}</div>
                                <el-link v-if="item.file" :href="item.file" target="_blank" type="primary"
                                    :underline="false" stop-click>
                                    <el-icon>
                                        <Download />
                                    </el-icon> 附件
                                </el-link>
                            </div>
                        </div>
                    </el-col>
                </el-row>
            </div>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="currentSum.title" width="650px" center>
            <div class="dialog-s-header">
                <el-tag effect="plain">{{ currentSum.competitionName || '系统公告' }}</el-tag>
                <div class="dialog-s-date">发布于：{{ currentSum.createTime }}</div>
            </div>
            <el-divider />
            <div class="dialog-s-body">
                <p class="dialog-s-text">{{ currentSum.content }}</p>
            </div>
            <div v-if="currentSum.file" class="dialog-s-file">
                <el-button type="primary" plain :icon="Download" @click="download(currentSum.file)">立即下载附件文档</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Document, Calendar, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const dialogVisible = ref(false)
const currentSum = ref<any>({})

const load = async () => {
    loading.value = true
    try {
        const res = await request.get('/summary/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value }
        }) as any
        if (res.code === 200) { tableData.value = res.data.records; total.value = res.data.total }
    } finally { loading.value = false }
}

const viewDetail = (item: any) => { currentSum.value = item; dialogVisible.value = true }
const download = (url: string) => window.open(url, '_blank')

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

.summary-card {
    border-radius: 16px;
    border: 1px solid #f1f5f9;
    padding: 20px;
    margin-bottom: 20px;
    background: #fff;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    flex-direction: column;
    height: 180px;
    position: relative;
}

.summary-card:hover {
    transform: translateY(-5px);
    border-color: #3b82f6;
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.s-comp-tag {
    font-size: 11px;
    color: #64748b;
    font-weight: 600;
    background: #f1f5f9;
    padding: 2px 8px;
    border-radius: 4px;
    display: inline-block;
    margin-bottom: 12px;
}

.s-title {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 12px;
}

.s-content {
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
    height: 60px;
    overflow: hidden;
    margin-bottom: 16px;
}

.s-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px dashed #f1f5f9;
    padding-top: 12px;
    margin-top: auto;
}

.s-date {
    font-size: 12px;
    color: #94a3b8;
    display: flex;
    align-items: center;
    gap: 4px;
}

.dialog-s-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.dialog-s-date {
    font-size: 12px;
    color: #94a3b8;
}

.dialog-s-text {
    line-height: 1.8;
    color: #334155;
    white-space: pre-wrap;
    margin: 20px 0;
}

.dialog-s-file {
    text-align: center;
    margin-top: 24px;
}

.pagination-bar {
    margin-top: 32px;
    display: flex;
    justify-content: center;
}
</style>

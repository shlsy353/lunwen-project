<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <List />
                </el-icon>
                <span>竞赛分类浏览</span>
            </div>
        </div>

        <el-row :gutter="20">
            <el-col v-for="t in types" :key="t.id" :xs="24" :sm="12" :md="8" :lg="6">
                <el-card class="type-card" shadow="hover">
                    <div class="type-icon-wrapper">
                        <el-icon>
                            <Collection />
                        </el-icon>
                    </div>
                    <h3 class="type-name">{{ t.name }}</h3>
                    <p class="type-desc">{{ t.description || '暂无详细描述...' }}</p>
                    <div class="type-footer">
                        <el-button type="primary" round
                            @click="$router.push({ path: '/student/competitions', query: { typeId: t.id } })">
                            查看相关竞赛
                        </el-button>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <div v-if="types.length === 0" class="empty-state">
            <el-empty description="暂无分类信息" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { List, Collection } from '@element-plus/icons-vue'

const types = ref<any[]>([])

const loadTypes = async () => {
    const res = await request.get('/competition-type/list') as any
    if (res.code === 200) types.value = res.data
}

onMounted(loadTypes)
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

.type-card {
    margin-bottom: 24px;
    border-radius: 16px;
    border: none;
    text-align: center;
    padding: 20px;
    transition: all 0.3s;
    background: #fff;
}

.type-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.type-icon-wrapper {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: #eff6ff;
    color: #3b82f6;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 32px;
}

.type-name {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 12px;
}

.type-desc {
    font-size: 14px;
    color: #64748b;
    line-height: 1.6;
    height: 66px;
    overflow: hidden;
    margin-bottom: 20px;
}

.type-footer {
    margin-top: auto;
}
</style>

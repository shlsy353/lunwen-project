<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Money />
                </el-icon>
                <span>我的报销申请</span>
            </div>
            <el-button type="primary" @click="handleAdd" :icon="Plus" round>提交报销项</el-button>
        </div>

        <el-card class="premium-card" shadow="never">
            <div v-if="loading" v-loading="loading" style="height: 300px"></div>
            <div v-else-if="tableData.length === 0" class="empty-state">
                <el-empty description="暂无报销记录" />
            </div>
            <div v-else class="expense-list">
                <div v-for="item in tableData" :key="item.id" class="expense-item">
                    <div class="e-header">
                        <div class="e-competition">{{ competitionMap[item.competitionId] || ('加载中... #' +
                            item.competitionId) }}</div>
                        <span class="e-name">{{ item.itemName }}</span>
                        <el-tag :type="statusType(item.status)" effect="dark" size="small">{{ statusText(item.status)
                        }}</el-tag>
                    </div>
                    <div class="e-body">
                        <div class="e-amount">¥ {{ item.amount?.toFixed(2) }}</div>
                        <p class="e-desc">{{ item.description || '无具体说明...' }}</p>
                    </div>
                    <div class="e-footer">
                        <span class="e-date"><el-icon>
                                <Calendar />
                            </el-icon> {{ item.createTime?.substring(0, 10) }}</span>
                        <el-button link type="danger" v-if="item.status === 0"
                            @click="handleDelete(item)">停办/撤回</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" title="提交报销申请" width="450px">
            <el-form :model="form" label-width="80px" class="dialog-form">
                <el-form-item label="关联竞赛" prop="competitionId">
                    <el-select v-model="form.competitionId" placeholder="选择关联的竞赛" style="width:100%">
                        <el-option v-for="c in registeredCompetitions" :key="c.id" :label="c.name" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="报销项" prop="itemName">
                    <el-input v-model="form.itemName" placeholder="如：往返车费、耗材费..." />
                </el-form-item>
                <el-form-item label="金额 (¥)" prop="amount">
                    <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>
                <el-form-item label="详情描述" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="填写用途或其他补充说明..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="save" round>立即提交</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { Plus, Money, Calendar } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const dialogVisible = ref(false)
const registeredCompetitions = ref<any[]>([])
const competitionMap = ref<Record<number, string>>({})
const form = reactive({ competitionId: undefined, itemName: '', amount: 0, description: '' })

const statusText = (s: number) => ['待审核', '已报回/通过', '已驳回', '已撤回'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'danger', 'info'] as const)[s] ?? 'info'

const load = async () => {
    loading.value = true
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
        const res = await request.get('/expense/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, studentId: user.id }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total

            // Resolve competition names
            const compIds = [...new Set(tableData.value.map((item: any) => item.competitionId))]
            for (const cid of compIds) {
                if (!competitionMap.value[cid]) {
                    const cRes = await request.get(`/competition/${cid}`) as any
                    if (cRes.code === 200) competitionMap.value[cid] = cRes.data.name
                }
            }
        }
    } finally { loading.value = false }
}

const fetchRegistrations = async () => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const res = await request.get('/registration/page', {
        params: { studentId: user.id, status: 1, pageSize: 100 }
    }) as any
    if (res.code === 200) {
        const regs = res.data.records
        const comps = await Promise.all(regs.map(async (r: any) => {
            const cRes = await request.get(`/competition/${r.competitionId}`) as any
            return cRes.data
        }))
        registeredCompetitions.value = comps
    }
}

const handleAdd = () => {
    Object.assign(form, { competitionId: undefined, itemName: '', amount: 0, description: '' });
    dialogVisible.value = true
    fetchRegistrations()
}
const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定撤回此项报销申请？', '提示', { type: 'warning' }).then(async () => {
        await request.put(`/expense/${row.id}/withdraw`)
        ElMessage.success('已撤回'); load()
    })
}
const save = async () => {
    if (!form.competitionId || !form.itemName || form.amount <= 0) return ElMessage.warning('请填写完整报销信息')
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    await request.post('/expense', { ...form, studentId: user.id, status: 0 })
    ElMessage.success('报销申请提交成功'); dialogVisible.value = false; load()
}

onMounted(load)
</script>

<style scoped>
.page-container {
    padding: 0;
}

.page-header {
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
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

.expense-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.expense-item {
    border-radius: 16px;
    padding: 20px;
    border: 1px solid #f1f5f9;
    background: #fff;
    transition: all 0.3s;
}

.expense-item:hover {
    border-color: #3b82f6;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    transform: translateY(-3px);
}

.e-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.e-competition {
    font-size: 12px;
    color: #3b82f6;
    margin-bottom: 4px;
    font-weight: 600;
}

.e-name {
    font-weight: 700;
    color: #1e293b;
    font-size: 15px;
}

.e-body {
    margin-bottom: 16px;
    min-height: 80px;
}

.e-amount {
    font-size: 24px;
    font-weight: 800;
    color: #16a34a;
    margin-bottom: 8px;
}

.e-desc {
    font-size: 13px;
    color: #64748b;
    line-height: 1.5;
    margin: 0;
}

.e-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px dashed #f1f5f9;
    padding-top: 12px;
}

.e-date {
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
</style>

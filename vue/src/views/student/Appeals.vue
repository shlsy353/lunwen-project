<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <WarningFilled />
                </el-icon>
                <span>我的成绩申诉</span>
            </div>
            <el-button type="primary" @click="handleAdd" :icon="Plus" round>发起新申诉</el-button>
        </div>

        <el-card class="premium-card" shadow="never">
            <div v-if="loading" v-loading="loading" style="height: 300px"></div>
            <div v-else-if="tableData.length === 0" class="empty-state">
                <el-empty description="若你对个人评分有异议，可在此提交申诉。目前暂无申诉记录。" />
            </div>
            <div v-else class="appeal-grid">
                <div v-for="item in tableData" :key="item.id" class="appeal-card">
                    <div class="appeal-header">
                        <span class="appeal-title">{{ competitionMap[item.competitionId] || ('竞赛加载中... #' +
                            item.competitionId) }}</span>
                        <el-tag :type="statusType(item.status)" effect="dark">{{ statusText(item.status) }}</el-tag>
                    </div>
                    <div class="appeal-body">
                        <div class="appeal-info-row">
                            <span class="label">申诉理由:</span>
                            <span class="value">{{ item.reason }}</span>
                        </div>
                        <div class="appeal-info-row">
                            <span class="label">详情简述:</span>
                            <span class="value">{{ item.content }}</span>
                        </div>
                        <div class="appeal-result" v-if="item.response">
                            <div class="result-label">
                                <el-icon>
                                    <SuccessFilled />
                                </el-icon> 处理结果反馈：
                            </div>
                            <div class="result-text">{{ item.response }}</div>
                        </div>
                    </div>
                    <div class="appeal-footer">
                        <span class="appeal-time"><el-icon>
                                <Calendar />
                            </el-icon> {{ item.createTime }}</span>
                        <el-button link type="danger" v-if="item.status === 0"
                            @click="handleCancel(item)">撤回</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" title="发起成绩异议申诉" width="500px">
            <el-form :model="form" ref="formRef" label-width="80px" class="dialog-form">
                <el-form-item label="关联竞赛" prop="competitionId">
                    <el-select v-model="form.competitionId" placeholder="请选择要申诉的竞赛项目" style="width: 100%">
                        <el-option v-for="c in registeredCompetitions" :key="c.id" :label="c.name" :value="c.id" />
                    </el-select>
                </el-form-item>
                <el-form-item label="申诉理由" prop="reason">
                    <el-select v-model="form.reason" placeholder="请选择申诉主要原因" style="width: 100%">
                        <el-option label="评分严重过低/不公" value="评分过低" />
                        <el-option label="提交文件无法查看/解析" value="文件解析异常" />
                        <el-option label="规则解读存疑" value="规则存疑" />
                        <el-option label="其他技术性问题" value="其他问题" />
                    </el-select>
                </el-form-item>
                <el-form-item label="详细说明" prop="content">
                    <el-input v-model="form.content" type="textarea" :rows="5"
                        placeholder="请详细提供分数存在异议的佐证或说明，有助于老师快速核实..." />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, WarningFilled, Calendar, SuccessFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const registeredCompetitions = ref<any[]>([])
const competitionMap = ref<Record<number, string>>({})
const form = reactive({ competitionId: undefined, reason: '', content: '' })

const statusText = (s: number) => ['待研判', '已受理', '已撤回', '已驳回', '已关闭'][s] ?? '未知'
const statusType = (s: number) => (['warning', 'success', 'info', 'danger', 'info'] as const)[s] ?? 'info'

const load = async () => {
    loading.value = true
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
        const res = await request.get('/score-appeal/page', {
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
    form.competitionId = undefined
    form.reason = ''
    form.content = ''
    dialogVisible.value = true
    fetchRegistrations()
}

const handleCancel = (row: any) => {
    ElMessageBox.confirm('确定撤回此项申诉？', '提示', { type: 'warning' }).then(async () => {
        await request.put(`/score-appeal/${row.id}/withdraw`)
        ElMessage.success('申诉已撤回')
        load()
    })
}

const save = async () => {
    if (!form.competitionId || !form.reason || !form.content) return ElMessage.warning('请填写完整信息')
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    await request.post('/score-appeal', { ...form, studentId: user.id, status: 0 })
    ElMessage.success('申诉提交成功，正在等待老师研判')
    dialogVisible.value = false
    load()
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

.appeal-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
    gap: 24px;
}

.appeal-card {
    border-radius: 16px;
    border: 1px solid #f1f5f9;
    padding: 20px;
    background: #fff;
    transition: all 0.3s;
}

.appeal-card:hover {
    transform: translateY(-5px);
    border-color: #3b82f6;
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
}

.appeal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.appeal-title {
    font-weight: 700;
    color: #1e293b;
    font-size: 15px;
}

.appeal-body {
    margin-bottom: 20px;
}

.appeal-info-row {
    display: flex;
    margin-bottom: 12px;
    gap: 12px;
    font-size: 14px;
    line-height: 1.5;
}

.appeal-info-row .label {
    color: #64748b;
    font-weight: 600;
    width: 70px;
    flex-shrink: 0;
}

.appeal-info-row .value {
    color: #334155;
}

.appeal-result {
    margin-top: 16px;
    padding: 16px;
    background: #f8fafc;
    border-radius: 12px;
    border-left: 4px solid #10b981;
}

.result-label {
    color: #059669;
    font-weight: 700;
    font-size: 13px;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.result-text {
    font-size: 13px;
    color: #334155;
    line-height: 1.6;
    font-style: italic;
}

.appeal-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #f1f5f9;
    padding-top: 16px;
}

.appeal-time {
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

@media (max-width: 480px) {
    .appeal-grid {
        grid-template-columns: 1fr;
    }
}
</style>

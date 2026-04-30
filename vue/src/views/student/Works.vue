<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Collection />
                </el-icon>
                <span>成果提呈中心</span>
            </div>
            <el-button type="primary" @click="handleAdd" :icon="Plus" round>提呈新作品</el-button>
        </div>

        <el-card class="premium-card archive-card" shadow="never">
            <div v-if="loading" v-loading="loading" style="height: 300px"></div>
            <div v-else-if="tableData.length === 0" class="empty-state">
                <el-empty description="你还没有提交过作品成果，开启你的竞赛之旅吧！">
                    <el-button type="primary" round @click="$router.push('/student/competitions')">探索竞赛</el-button>
                </el-empty>
            </div>
            <div v-else class="work-list">
                <div v-for="item in tableData" :key="item.id" class="work-item">
                    <div class="w-comp-info">项目 ID: #{{ item.competitionId }}</div>
                    <div class="w-main-info">
                        <div class="w-file-box">
                            <el-icon class="file-icon">
                                <Files />
                            </el-icon>
                            <div class="file-meta">
                                <div class="file-link" @click="download(item.workFile)">
                                    {{ getFileName(item.workFile) }}
                                    <el-icon>
                                        <Download />
                                    </el-icon>
                                </div>
                                <div class="file-date">最后更新：{{ item.createTime || '同步中...' }}</div>
                            </div>
                        </div>
                        <div class="w-comment-box" v-if="item.comment">
                            <p class="w-label">教师评价：</p>
                            <p class="w-text">{{ item.comment }}</p>
                        </div>
                    </div>
                    <div class="w-score-badge" v-if="item.score">
                        <div class="score-num">{{ item.score }}</div>
                        <div class="score-label">成就分值</div>
                    </div>
                    <div class="w-footer">
                        <el-tooltip :content="checkLockStatus(item).reason" placement="top"
                            :disabled="!checkLockStatus(item).lock">
                            <el-button link type="primary" @click="handleEdit(item)"
                                :disabled="checkLockStatus(item).lock">修改/覆盖提呈</el-button>
                        </el-tooltip>
                        <el-tooltip :content="checkLockStatus(item).reason" placement="top"
                            :disabled="!checkLockStatus(item).lock">
                            <el-button link type="danger" @click="handleDelete(item)"
                                :disabled="checkLockStatus(item).lock">撤回提呈</el-button>
                        </el-tooltip>
                    </div>
                </div>
            </div>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="form.id ? '覆盖提呈作品' : '提呈新成果作品'" width="550px" class="premium-dialog">
            <el-form :model="form" label-width="90px" class="dialog-form" label-position="top">
                <el-form-item label="关联竞赛项目" prop="competitionId">
                    <el-select v-model="form.competitionId" placeholder="选择已报名的竞赛" style="width: 100%"
                        @change="onCompetitionChange">
                        <el-option v-for="c in regs" :key="c.competitionId" :label="'项目 ' + c.competitionId"
                            :value="c.competitionId" />
                    </el-select>
                </el-form-item>

                <!-- Naming Rule Alert -->
                <div v-if="selectedCompRule" class="naming-rule-alert">
                    <el-icon>
                        <InfoFilled />
                    </el-icon>
                    <div class="rule-content">
                        <div class="rule-label">命名规范要求：</div>
                        <div class="rule-text">{{ selectedCompRule }}</div>
                    </div>
                </div>

                <el-form-item label="作品成果文件 (支持覆盖更新)" prop="workFile">
                    <div class="upload-wrapper">
                        <el-upload class="premium-upload" drag action="/api/file/upload"
                            :data="{ competitionId: form.competitionId }" :show-file-list="false"
                            :on-success="onUploadSuccess" :disabled="!form.competitionId">
                            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                            <div class="el-upload__text" v-if="!form.workFile">
                                将文件拖到此处，或 <em>点击上传</em>
                            </div>
                            <div class="el-upload__text success" v-else>
                                已选：{{ getFileName(form.workFile) }} (点击可更换)
                            </div>
                            <template #tip>
                                <div class="el-upload__tip">
                                    请确保文件名符合上述规范。相同文件名的上传将覆盖旧版本。
                                </div>
                            </template>
                        </el-upload>
                    </div>
                </el-form-item>

                <el-form-item label="成果说明 / 备注" prop="content">
                    <el-input v-model="form.content" type="textarea" :rows="3" placeholder="写一点关于你作品的说明..." />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="save" round :disabled="!form.workFile">立即提呈</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Collection, Files, Download, Upload, InfoFilled, UploadFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const competitionInfoMap = ref<Record<number, any>>({})
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const regs = ref<any[]>([])
const selectedCompRule = ref('')

const form = reactive({ id: undefined, competitionId: undefined, workFile: '', content: '' })

const load = async () => {
    loading.value = true
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
        const res = await request.get('/work-submission/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, studentId: user.id }
        }) as any
        if (res.code === 200) {
            tableData.value = res.data.records
            total.value = res.data.total

            // 批量拉取竞赛信息以判断日期限制
            const compIds = [...new Set(tableData.value.map(item => item.competitionId))]
            for (const cid of compIds) {
                if (!competitionInfoMap.value[cid]) {
                    const cRes = await request.get(`/competition/${cid}`) as any
                    if (cRes.code === 200) competitionInfoMap.value[cid] = cRes.data
                }
            }
        }
    } finally { loading.value = false }
}

const checkLockStatus = (item: any) => {
    // 1. 检查是否评分
    if (item.score !== null) return { lock: true, reason: '作品已评分，不可操作' }

    // 2. 检查提交是否超过24小时
    if (item.createTime) {
        const createTime = new Date(item.createTime).getTime()
        const now = new Date().getTime()
        if (now - createTime > 24 * 60 * 60 * 1000) {
            return { lock: true, reason: '提交已超24小时，已锁定' }
        }
    }

    // 3. 检查竞赛日期
    const comp = competitionInfoMap.value[item.competitionId]
    if (comp && comp.competitionDate) {
        const compDate = new Date(comp.competitionDate + ' 23:59:59').getTime()
        if (Date.now() > compDate) {
            return { lock: true, reason: '竞赛已结束（截止：' + comp.competitionDate + ')' }
        }
    }

    return { lock: false, reason: '' }
}

const fetchRegs = async () => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const res = await request.get('/registration/page', {
        params: { studentId: user.id, status: 1, pageSize: 100 }
    }) as any
    if (res.code === 200) regs.value = res.data.records
}

const onCompetitionChange = async (compId: number) => {
    selectedCompRule.value = '正在加载规则...'
    try {
        const res = await request.get(`/competition/${compId}`) as any
        if (res.code === 200) {
            selectedCompRule.value = res.data.namingRule || '请按“学号_姓名_作品名”格式命名'
        }
    } catch {
        selectedCompRule.value = '请按标准格式命名'
    }
}

const getFileName = (path: string) => {
    if (!path) return ''
    const parts = path.split('/')
    return parts[parts.length - 1]
}

const handleAdd = () => {
    Object.assign(form, { id: undefined, competitionId: undefined, workFile: '', content: '' })
    selectedCompRule.value = ''
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    Object.assign(form, row)
    onCompetitionChange(row.competitionId)
    dialogVisible.value = true
}

const handleDelete = (row: any) => {
    ElMessageBox.confirm('确定撤回并从系统中移除此作品吗？', '重要提示', {
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
    }).then(async () => {
        await request.delete('/work-submission/' + row.id)
        ElMessage.success('提呈已撤销')
        load()
    })
}

const onUploadSuccess = (res: any) => {
    if (res.code === 200) {
        form.workFile = res.data
        ElMessage.success('成果文件已就绪（支持覆盖）')
    } else {
        ElMessage.error(res.msg || '上传失败')
    }
}

const download = (url: string) => {
    if (url.startsWith('/')) {
        window.open(url, '_blank')
    } else {
        window.open(url, '_blank')
    }
}

const save = async () => {
    if (!form.workFile || !form.competitionId) return ElMessage.warning('请提供作品文件并选择竞赛项目')
    const user = JSON.parse(localStorage.getItem('user') || '{}')

    try {
        // 提交前的最后日期校验
        const comp = competitionInfoMap.value[form.competitionId]
        if (comp && comp.competitionDate) {
            const compDate = new Date(comp.competitionDate + ' 23:59:59').getTime()
            if (Date.now() > compDate) {
                return ElMessage.error('提呈失败：该竞赛项目已截止')
            }
        }

        if (form.id) {
            await request.put('/work-submission', form)
        } else {
            await request.post('/work-submission', { ...form, studentId: user.id })
        }
        ElMessage.success('作品成果提呈/更新成功！')
        dialogVisible.value = false
        load()
    } catch (e: any) {
        ElMessage.error(e.response?.data?.msg || '保存失败')
    }
}

onMounted(() => { load(); fetchRegs() })
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

.naming-rule-alert {
    background: #f0f9ff;
    border: 1px solid #bae6fd;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 24px;
    display: flex;
    gap: 12px;
    align-items: flex-start;
}

.naming-rule-alert .el-icon {
    font-size: 20px;
    color: #0ea5e9;
    margin-top: 2px;
}

.rule-label {
    font-size: 13px;
    font-weight: 800;
    color: #0369a1;
    margin-bottom: 4px;
}

.rule-text {
    font-size: 14px;
    color: #0c4a6e;
    line-height: 1.5;
}

.upload-wrapper {
    width: 100%;
}

.premium-upload :deep(.el-upload-dragger) {
    border-radius: 16px;
    border: 2px dashed #e2e8f0;
    background: #f8fafc;
    transition: all 0.3s;
}

.premium-upload :deep(.el-upload-dragger:hover) {
    border-color: #3b82f6;
    background: #eff6ff;
}

.success {
    color: #10b981;
    font-weight: 700;
}

.work-item {
    border-radius: 16px;
    padding: 24px;
    border: 1px solid #f1f5f9;
    background: #fff;
    transition: all 0.3s;
    position: relative;
    display: flex;
    gap: 24px;
    align-items: flex-start;
    margin-bottom: 16px;
}

.work-item:hover {
    border-color: #3b82f6;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
    transform: translateY(-3px);
}

.w-score-badge {
    position: absolute;
    top: 24px;
    right: 24px;
    background: #ecfdf5;
    border: 1px solid #10b981;
    border-radius: 12px;
    padding: 12px;
    text-align: center;
    min-width: 80px;
}

.score-num {
    font-size: 28px;
    font-weight: 800;
    color: #059669;
}

.score-label {
    font-size: 11px;
    color: #059669;
    font-weight: 700;
}

.w-comp-info {
    position: absolute;
    top: 0;
    left: 0;
    background: #f1f5f9;
    color: #64748b;
    font-size: 11px;
    font-weight: 700;
    padding: 4px 12px;
    border-bottom-right-radius: 12px;
}

.w-main-info {
    flex: 1;
    margin-top: 10px;
}

.w-file-box {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
}

.file-icon {
    font-size: 40px;
    color: #3b82f6;
    background: #eff6ff;
    padding: 10px;
    border-radius: 12px;
}

.file-link {
    font-size: 15px;
    font-weight: 700;
    color: #1e293b;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
}

.file-date {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 4px;
}

.w-comment-box {
    background: #f8fafc;
    padding: 16px;
    border-radius: 12px;
    border-left: 4px solid #e2e8f0;
}

.w-footer {
    border-top: 1px solid #f1f5f9;
    padding-top: 16px;
    margin-top: auto;
    display: flex;
    gap: 16px;
}
</style>

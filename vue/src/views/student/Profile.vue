<template>
    <div class="page-container">
        <!-- 页面标题区域 -->
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Avatar />
                </el-icon>
                <span>个人中心</span>
            </div>
        </div>

        <el-row :gutter="24">
            <!-- 左侧个人资料卡片列 -->
            <el-col :md="8">
                <el-card class="premium-card profile-card" shadow="hover">
                    <div class="profile-header">
                        <el-upload class="avatar-uploader" action="/api/file/uploadHead" :show-file-list="false"
                            :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
                            <el-avatar :size="100" :src="form.avatar" class="profile-avatar">
                                {{ (form.name || form.username || 'S')[0] }}
                            </el-avatar>
                            <div class="avatar-edit-overlay">
                                <el-icon>
                                    <Plus />
                                </el-icon>
                            </div>
                        </el-upload>
                        <h2 class="profile-name">{{ form.name || form.username || '学子' }}</h2>
                        <div class="role-badge">{{ form.role }}</div>
                        <p class="profile-bio">{{ form.bio || '追求卓越，止于至善。' }}</p>
                    </div>
                    <el-divider />
                    <!-- 个人参赛统计 -->
                    <div class="profile-stats">
                        <div class="stat-item">
                            <div class="num">{{ stats.count }}</div>
                            <div class="label">参赛场数</div>
                        </div>
                        <div class="stat-item">
                            <div class="num">{{ stats.awards }}</div>
                            <div class="label">所获荣誉</div>
                        </div>
                    </div>
                    <!-- 联系方式展示 -->
                    <div class="profile-contact">
                        <div class="contact-item">
                            <el-icon>
                                <School />
                            </el-icon> {{ form.school || '未填写学校' }}
                        </div>
                        <div class="contact-item">
                            <el-icon>
                                <Message />
                            </el-icon> {{ form.email || '未绑定邮箱' }}
                        </div>
                        <div class="contact-item">
                            <el-icon>
                                <Phone />
                            </el-icon> {{ form.phone || '未绑定电话' }}
                        </div>
                    </div>
                </el-card>

                <!-- 学生专用快捷入口 -->
                <el-card v-if="form.role === 'STUDENT'" class="premium-card quick-actions" shadow="hover"
                    style="margin-top: 24px">
                    <template #header>
                        <span class="card-title">快捷入口</span>
                    </template>
                    <div class="action-list">
                        <el-button link type="primary"
                            @click="$router.push('/student/registrations')">查阅我的报名</el-button>
                        <el-button link type="primary"
                            @click="$router.push('/student/award-situation')">查阅获奖档案</el-button>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧资料修改表单列 -->
            <el-col :md="16">
                <el-card class="premium-card edit-card" shadow="never">
                    <template #header>
                        <span class="card-title"><el-icon>
                                <Edit />
                            </el-icon> 修改个人资料</span>
                    </template>
                    <el-form :model="form" ref="formRef" label-width="100px" label-position="top" class="edit-form">
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <el-form-item label="登录账号">
                                    <el-input v-model="form.username" disabled />
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="学号/识别码">
                                    <el-input v-model="form.studentId" placeholder="填写学号..." />
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="学校名称">
                                    <el-input v-model="form.school" placeholder="输入学校..." />
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <el-form-item label="真实姓名">
                                    <el-input v-model="form.name" placeholder="填写实名..." />
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="性别">
                                    <el-radio-group v-model="form.gender">
                                        <el-radio :label="1">男</el-radio>
                                        <el-radio :label="0">女</el-radio>
                                    </el-radio-group>
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <el-form-item label="电子邮箱">
                                    <el-input v-model="form.email" placeholder="xxx@email.com" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="联系电话">
                                    <el-input v-model="form.phone" placeholder="1XXXXXXXXXX" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <el-form-item label="个性签名/主页座右铭">
                            <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="写一句座右铭激励自己吧..." />
                        </el-form-item>

                        <div class="form-footer">
                            <el-button type="primary" round class="save-btn" @click="handleUpdate"
                                :loading="updating">保存更改</el-button>
                            <el-button round plain @click="fetchInfo">重置资料</el-button>
                        </div>
                    </el-form>
                </el-card>


                <!-- 领队资格申请区域 (教师角色可见) -->
                <el-card v-if="form.role === 'TEACHER' || form.role === 'LEADER'" class="premium-card security-card"
                    shadow="never" style="margin-top: 24px">
                    <template #header>
                        <span class="card-title">
                            <el-icon>
                                <Star />
                            </el-icon> 领队资格角色
                        </span>
                    </template>
                    <div class="security-info">
                        <div class="info-text">
                            <h4>{{ form.leaderStatus === 2 || form.role === 'LEADER' ? '已获得领队资格' : (form.leaderStatus
                                === 1 ?
                                '领队申请审核中' : '申请领队角色') }}</h4>
                            <p>领队老师可指导更多学生项目。{{ form.leaderStatus === 0 && form.role !== 'LEADER' ? '提交申请后由管理员审核。' : ''
                            }}</p>
                        </div>
                        <!-- 资格状态标签 -->
                        <div v-if="form.leaderStatus === 2 || form.role === 'LEADER'"
                            style="display: flex; align-items: center; gap: 8px">
                            <el-tag type="success" size="large" effect="dark" round>通过</el-tag>
                            <el-icon color="#67C23A" style="font-size: 24px">
                                <CircleCheckFilled />
                            </el-icon>
                        </div>
                        <el-button v-else-if="form.leaderStatus === 0" type="success" plain round
                            @click="handleApplyLeader">立即申请</el-button>
                        <el-tag v-else-if="form.leaderStatus === 1" type="warning" size="large" round
                            effect="plain">审核中</el-tag>
                    </div>
                </el-card>

                <!-- 账号安全设置区域 -->
                <el-card class="premium-card security-card" shadow="never" style="margin-top: 24px">
                    <template #header>
                        <span class="card-title"><el-icon>
                                <Lock />
                            </el-icon> 账户安全</span>
                    </template>
                    <div class="security-info">
                        <div class="info-text">
                            <h4>账号密码安全</h4>
                            <p>定期更换密码可提高账户安全性，保障竞赛资料安全。</p>
                        </div>
                        <el-button type="primary" plain round @click="handleChangePassword">立即修改核心密码</el-button>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, Message, Phone, Edit, Lock, School, Star, CircleCheckFilled, Plus } from '@element-plus/icons-vue'

const updating = ref(false)
const stats = ref({ count: 0, awards: 0 })
const form = reactive({
    id: undefined,
    username: '',
    name: '',
    avatar: '',
    gender: 1,
    phone: '',
    email: '',
    studentId: '',
    bio: '',
    school: '',
    role: '',
    leaderStatus: 0
})

/**
 * 头像上传成功后的回调
 */
const handleAvatarSuccess = (res: any) => {
    if (res.code === 200) {
        form.avatar = res.data
        ElMessage.success('头像上传成功')
        // 如果是当前用户，同步更新本地缓存
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        if (user.id === form.id) {
            localStorage.setItem('user', JSON.stringify({ ...user, avatar: res.data }))
        }
        // 立即保存到数据库
        handleUpdate()
    } else {
        ElMessage.error(res.msg || '头像上传失败')
    }
}

/**
 * 头像上传前的校验
 */
const beforeAvatarUpload = (rawFile: any) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('头像必须是 JPG 或 PNG 格式!')
        return false
    } else if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error('头像大小不能超过 2MB!')
        return false
    }
    return true
}

/**
 * 从后端拉取当前登录用户的完整详细信息
 */
const fetchInfo = async () => {
    const res = await request.get('/user/info') as any
    if (res.code === 200) Object.assign(form, res.data)
}

/**
 * 获取个人的参赛统计数据（如参赛场数、所获荣誉数）
 */
const fetchStats = async () => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const [regRes, awRes] = await Promise.all([
        request.get('/registration/page', { params: { pageSize: 1, studentId: user.id } }),
        request.get('/award/page', { params: { pageSize: 1, studentId: user.id } })
    ]) as any[]
    if (regRes.code === 200) stats.value.count = regRes.data.total
    if (awRes.code === 200) stats.value.awards = awRes.data.total
}

/**
 * 处理用户资料更新提交
 */
const handleUpdate = async () => {
    updating.value = true
    try {
        await request.put('/user', form)
        ElMessage.success('资料资料更新成功！')
        // 同步更新本地缓存的离线用户信息
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        localStorage.setItem('user', JSON.stringify({ ...user, ...form }))
    } finally { updating.value = false }
}

/**
 * 教师申请领队资格的操作处理
 */
const handleApplyLeader = async () => {
    ElMessageBox.confirm('确认申请领队老师资格吗？', '申请提示').then(async () => {
        const res = await request.put('/user/apply-leader', { id: form.id }) as any
        if (res.code === 200) {
            ElMessage.success('申请已提交，请等待管理员审核')
            fetchInfo() // 提交后刷新状态
        }
    })
}

/**
 * 修改账户登录密码
 */
const handleChangePassword = () => {
    ElMessageBox.prompt('请输入新密码', '修改密码', {
        inputPattern: /^.{6,20}$/,
        inputErrorMessage: '密码长度需在6-20位之间'
    }).then(({ value }) => {
        request.put('/user', { ...form, password: value }).then(() => {
            ElMessage.success('密码重置成功，请妥善保管。')
        })
    })
}

// 页面加载时初始化数据
onMounted(() => { fetchInfo(); fetchStats() })
</script>

<style scoped>
/* 样式部分保持原样，无需翻译选择器 */
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
    overflow: hidden;
}

.profile-card {
    text-align: center;
    padding: 10px;
    border: 1px solid #f1f5f9;
}

.profile-header {
    padding: 20px 0;
}

.profile-avatar {
    background: linear-gradient(135deg, #3b82f6, #1d4ed8);
    box-shadow: 0 8px 16px rgba(59, 130, 246, 0.3);
    border: 4px solid #fff;
}

.avatar-uploader {
    position: relative;
    display: inline-block;
    cursor: pointer;
    margin-bottom: 20px;
}

.avatar-edit-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    color: #fff;
    font-size: 24px;
    opacity: 0;
    transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-edit-overlay {
    opacity: 1;
}

.profile-name {
    font-size: 22px;
    font-weight: 700;
    color: #1e293b;
    margin: 10px 0;
}

.role-badge {
    display: inline-block;
    padding: 2px 12px;
    border-radius: 20px;
    background: #eff6ff;
    color: #3b82f6;
    font-size: 11px;
    font-weight: 800;
    margin-bottom: 20px;
    letter-spacing: 1px;
}

.profile-bio {
    font-size: 14px;
    color: #64748b;
    font-style: italic;
    line-height: 1.6;
}

.profile-stats {
    display: flex;
    justify-content: center;
    gap: 40px;
    margin: 20px 0;
}

.stat-item .num {
    font-size: 20px;
    font-weight: 800;
    color: #1e293b;
}

.stat-item .label {
    font-size: 12px;
    color: #94a3b8;
    font-weight: 600;
    margin-top: 4px;
}

.profile-contact {
    padding: 10px 0;
}

.contact-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 13px;
    color: #475569;
    margin-bottom: 12px;
}

.contact-item .el-icon {
    color: #94a3b8;
}

.quick-actions {
    padding: 10px;
}

.action-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.card-title {
    font-weight: 700;
    color: #334155;
    font-size: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.edit-card {
    padding: 10px;
}

.edit-form {
    padding-top: 10px;
}

.form-footer {
    margin-top: 32px;
    display: flex;
    gap: 16px;
}

.save-btn {
    padding: 12px 32px;
    font-weight: 600;
}

.security-card {
    padding: 10px;
}

.security-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #f8fafc;
    padding: 20px;
    border-radius: 12px;
}

.info-text h4 {
    margin: 0 0 8px;
    font-size: 15px;
    color: #1e293b;
}

.info-text p {
    margin: 0;
    font-size: 13px;
    color: #64748b;
}
</style>

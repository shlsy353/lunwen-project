<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Avatar />
                </el-icon>
                <span>个人中心</span>
            </div>
        </div>

        <el-row :gutter="24">
            <!-- Left Profile Column -->
            <el-col :md="8">
                <el-card class="premium-card profile-card" shadow="hover">
                    <div class="profile-header">
                        <el-avatar :size="100" class="profile-avatar">{{ (form.name || form.username || 'L')[0]
                            }}</el-avatar>
                        <h2 class="profile-name">{{ form.name || form.username || '领队老师' }}</h2>
                        <div class="role-badge">LEADER TEACHER</div>
                        <p class="profile-bio">{{ form.bio || '为人师表，厚德载物。' }}</p>
                    </div>
                    <el-divider />
                    <div class="profile-contact">
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

                <el-card class="premium-card quick-actions" shadow="hover" style="margin-top: 24px">
                    <template #header>
                        <span class="card-title">管理入口</span>
                    </template>
                    <div class="action-list">
                        <el-button link type="success" @click="$router.push('/leader/competitions')">竞赛信息管理</el-button>
                        <el-button link type="success" @click="$router.push('/leader/applications')">报名审批管理</el-button>
                        <el-button link type="success" @click="$router.push('/leader/scoring')">竞赛在线阅卷</el-button>
                    </div>
                </el-card>
            </el-col>

            <!-- Right Form Column -->
            <el-col :md="16">
                <el-card class="premium-card edit-card" shadow="never">
                    <template #header>
                        <span class="card-title"><el-icon>
                                <Edit />
                            </el-icon> 账户资料更新</span>
                    </template>
                    <el-form :model="form" ref="formRef" label-width="100px" label-position="top" class="edit-form">
                        <el-row :gutter="20">
                            <el-col :span="12">
                                <el-form-item label="教工号/工号">
                                    <el-input v-model="form.teacherId" placeholder="工号..." />
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="登录入口码">
                                    <el-input v-model="form.username" disabled />
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
                                <el-form-item label="学术邮箱">
                                    <el-input v-model="form.email" placeholder="xxx@edu.cn" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="联系电话">
                                    <el-input v-model="form.phone" placeholder="1XXXXXXXXXX" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <el-form-item label="个人简介/座右铭">
                            <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="写一句座右铭激励自己与学子吧..." />
                        </el-form-item>

                        <div class="form-footer">
                            <el-button type="success" round class="save-btn" @click="handleUpdate"
                                :loading="updating">保存更改</el-button>
                        </div>
                    </el-form>
                </el-card>

                <el-card class="premium-card security-card" shadow="never" style="margin-top: 24px">
                    <template #header>
                        <span class="card-title security"><el-icon>
                                <Lock />
                            </el-icon> 安全与隐私</span>
                    </template>
                    <div class="security-info">
                        <div class="info-text">
                            <h4>修改登录密码</h4>
                            <p>为保障教职工账户及竞赛评审公开公正透明，请注意定期维护密码安全性。</p>
                        </div>
                        <el-button type="success" plain round @click="handleChangePassword">修改核心密码</el-button>
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
import { Avatar, Message, Phone, Edit, Lock } from '@element-plus/icons-vue'

const updating = ref(false)
const form = reactive({
    id: undefined, username: '', name: '', gender: 1, phone: '', email: '', teacherId: '', bio: ''
})

const fetchInfo = async () => {
    const res = await request.get('/user/info') as any
    if (res.code === 200) Object.assign(form, res.data)
}

const handleUpdate = async () => {
    updating.value = true
    try {
        await request.put('/user', form)
        ElMessage.success('教师资料资料更新成功！')
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        localStorage.setItem('user', JSON.stringify({ ...user, ...form }))
    } finally { updating.value = false }
}

const handleChangePassword = () => {
    ElMessageBox.prompt('请输入新密码', '修改密码', {
        inputPattern: /^.{6,20}$/,
        inputErrorMessage: '密码长度需在6-20位之间'
    }).then(({ value }) => {
        request.put('/user', { ...form, password: value }).then(() => {
            ElMessage.success('教师账户密码已重置成功。')
        })
    })
}

onMounted(() => { fetchInfo() })
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
    color: #16a34a;
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
    background: linear-gradient(135deg, #16a34a, #166534);
    box-shadow: 0 8px 16px rgba(22, 163, 74, 0.3);
    margin-bottom: 20px;
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
    background: #f0fdf4;
    color: #16a34a;
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
    box-shadow: 0 4px 12px rgba(22, 163, 74, 0.2);
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

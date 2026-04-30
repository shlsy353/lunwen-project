import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { title: '学生注册' }
    },
    {
        path: '/competition/:id',
        redirect: (to: any) => `/student/competition/${to.params.id}`
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/layout/AdminLayout.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' },
        children: [
            {
                path: '',
                redirect: '/admin/dashboard'
            },
            {
                path: 'dashboard',
                name: 'AdminDashboard',
                component: () => import('@/views/admin/Dashboard.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'users',
                name: 'UserManagement',
                component: () => import('@/views/admin/UserManagement.vue'),
                meta: { title: '用户管理' }
            },
            {
                path: 'competitions',
                name: 'CompetitionManagement',
                component: () => import('@/views/admin/CompetitionManagement.vue'),
                meta: { title: '竞赛管理' }
            },
            {
                path: 'announcements',
                name: 'AnnouncementManagement',
                component: () => import('@/views/admin/AnnouncementManagement.vue'),
                meta: { title: '公告管理' }
            },
            {
                path: 'competition-types',
                name: 'AdminCompetitionTypeManagement',
                component: () => import('@/views/admin/CompetitionTypeManagement.vue'),
                meta: { title: '竞赛类型管理' }
            },
            {
                path: 'scoring',
                name: 'AdminScoringManagement',
                component: () => import('@/views/admin/ScoringManagement.vue'),
                meta: { title: '评分管理' }
            },
            {
                path: 'teams',
                name: 'AdminTeamManagement',
                component: () => import('@/views/admin/TeamManagement.vue'),
                meta: { title: '参赛名单管理' }
            },
            {
                path: 'awards',
                name: 'AdminAwardManagement',
                component: () => import('@/views/admin/AwardManagement.vue'),
                meta: { title: '获奖名单管理' }
            },
            {
                path: 'promotions',
                name: 'AdminPromotionManagement',
                component: () => import('@/views/admin/PromotionManagement.vue'),
                meta: { title: '晋级名单管理' }
            },
            {
                path: 'teachers',
                name: 'AdminTeacherManagement',
                component: () => import('@/views/admin/TeacherManagement.vue'),
                meta: { title: '教师管理' }
            },
            {
                path: 'appeals',
                name: 'AdminAppealManagement',
                component: () => import('@/views/admin/AppealManagement.vue'),
                meta: { title: '申诉管理' }
            },
            {
                path: 'profile',
                name: 'AdminProfile',
                component: () => import('@/views/student/Profile.vue'),
                meta: { title: '个人中心' }
            },
            {
                path: 'teacher-center',
                name: 'AdminTeacherCenter',
                component: () => import('@/views/teacher/Dashboard.vue'),
                meta: { title: '教师端概览' }
            }
        ]
    },
    {
        path: '/student',
        name: 'Student',
        component: () => import('@/layout/StudentLayout.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' },
        children: [
            {
                path: '',
                redirect: '/student/dashboard'
            },
            {
                path: 'dashboard',
                name: 'StudentDashboard',
                component: () => import('@/views/student/Dashboard.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'competition/:id',
                name: 'StudentCompetitionDetail',
                component: () => import('@/views/student/CompetitionDetail.vue'),
                meta: { title: '竞赛详情' }
            },
            {
                path: 'competition-types',
                name: 'StudentCompetitionTypes',
                component: () => import('@/views/student/CompetitionTypes.vue'),
                meta: { title: '竞赛类型' }
            },
            {
                path: 'competitions',
                name: 'StudentCompetitions',
                component: () => import('@/views/student/Competitions.vue'),
                meta: { title: '竞赛大厅' }
            },
            {
                path: 'registrations',
                name: 'StudentRegistrations',
                component: () => import('@/views/student/Registrations.vue'),
                meta: { title: '我的报名' }
            },
            {
                path: 'works',
                name: 'StudentWorks',
                component: () => import('@/views/student/Works.vue'),
                meta: { title: '作品提交' }
            },
            {
                path: 'scores',
                name: 'StudentScores',
                component: () => import('@/views/student/Scores.vue'),
                meta: { title: '我的成绩' }
            },
            {
                path: 'profile',
                name: 'StudentProfile',
                component: () => import('@/views/student/Profile.vue'),
                meta: { title: '个人中心' }
            },
            {
                path: 'expenses',
                name: 'StudentExpenses',
                component: () => import('@/views/student/Expenses.vue'),
                meta: { title: '报销清单' }
            },
            {
                path: 'appeals',
                name: 'StudentAppeals',
                component: () => import('@/views/student/Appeals.vue'),
                meta: { title: '成绩申诉' }
            },
            {
                path: 'award-situation',
                name: 'StudentAwardSituation',
                component: () => import('@/views/student/AwardSituation.vue'),
                meta: { title: '获奖成就' }
            }
        ]
    },
    {
        path: '/teacher',
        name: 'Teacher',
        component: () => import('@/layout/TeacherLayout.vue'),
        meta: { requiresAuth: true, roles: ['TEACHER', 'LEADER'] },
        children: [
            {
                path: '',
                redirect: '/teacher/dashboard'
            },
            {
                path: 'dashboard',
                name: 'TeacherDashboard',
                component: () => import('@/views/teacher/Dashboard.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'info-dashboard',
                name: 'TeacherInfoDashboard',
                component: () => import('@/views/teacher/InfoDashboard.vue'),
                meta: { title: '竞赛门户外网' }
            },
            {
                path: 'competitions',
                name: 'TeacherCompetitions',
                component: () => import('@/views/teacher/Competitions.vue'),
                meta: { title: '竞赛大厅' }
            },
            {
                path: 'lead-students',
                name: 'TeacherLeadStudents',
                component: () => import('@/views/teacher/LeadStudentManagement.vue'),
                meta: { title: '带领学生管理' }
            },
            {
                path: 'profile',
                name: 'TeacherProfile',
                component: () => import('@/views/student/Profile.vue'),
                meta: { title: '个人中心' }
            },
            {
                path: 'expenses',
                name: 'TeacherExpenses',
                component: () => import('@/views/teacher/Expenses.vue'),
                meta: { title: '报销管理' }
            },
            {
                path: 'appeals',
                name: 'TeacherAppeals',
                component: () => import('@/views/teacher/Appeals.vue'),
                meta: { title: '成绩申诉' }
            },
            {
                path: 'registrations',
                name: 'TeacherRegistrations',
                component: () => import('@/views/teacher/RegistrationManagement.vue'),
                meta: { title: '我的报名' }
            },
            {
                path: 'scoring',
                name: 'TeacherScoring',
                component: () => import('@/views/leader/Scoring.vue'),
                meta: { title: '竞赛评分' }
            },
            {
                path: 'award-situation',
                name: 'TeacherAwardSituation',
                component: () => import('@/views/leader/AwardSituation.vue'),
                meta: { title: '获奖情况' }
            }
        ]
    },
    {
        path: '/leader',
        name: 'Leader',
        component: () => import('@/layout/TeacherLayout.vue'),
        meta: { requiresAuth: true, role: 'LEADER' },
        children: [
            {
                path: '',
                redirect: '/leader/dashboard'
            },
            {
                path: 'dashboard',
                name: 'LeaderDashboard',
                component: () => import('@/views/leader/Dashboard.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'info-dashboard',
                name: 'LeaderInfoDashboard',
                component: () => import('@/views/teacher/InfoDashboard.vue'),
                meta: { title: '竞赛门户外网' }
            },
            {
                path: 'competitions-hall',
                name: 'LeaderCompetitionsHall',
                component: () => import('@/views/student/Competitions.vue'),
                meta: { title: '竞赛大厅' }
            },
            {
                path: 'competition-types',
                name: 'LeaderCompetitionTypes',
                component: () => import('@/views/leader/CompetitionTypeManagement.vue'),
                meta: { title: '竞赛类型管理' }
            },
            {
                path: 'competitions',
                name: 'LeaderCompetitions',
                component: () => import('@/views/leader/Competitions.vue'),
                meta: { title: '竞赛信息管理' }
            },
            {
                path: 'applications',
                name: 'LeaderApplications',
                component: () => import('@/views/leader/Applications.vue'),
                meta: { title: '报名信息管理' }
            },
            {
                path: 'scoring',
                name: 'LeaderScoring',
                component: () => import('@/views/leader/Scoring.vue'),
                meta: { title: '竞赛评分管理' }
            },
            {
                path: 'appeals',
                name: 'LeaderAppeals',
                component: () => import('@/views/leader/Appeals.vue'),
                meta: { title: '成绩申诉管理' }
            },
            {
                path: 'award-situation',
                name: 'LeaderAwardSituation',
                component: () => import('@/views/leader/AwardSituation.vue'),
                meta: { title: '获奖情况管理' }
            },
            {
                path: 'teams',
                name: 'LeaderTeams',
                component: () => import('@/views/leader/Teams.vue'),
                meta: { title: '参赛名单管理' }
            },
            {
                path: 'expenses',
                name: 'LeaderExpenses',
                component: () => import('@/views/leader/Expenses.vue'),
                meta: { title: '报销清单管理' }
            },
            {
                path: 'expenses',
                name: 'LeaderExpenses',
                component: () => import('@/views/leader/Expenses.vue'),
                meta: { title: '报销清单管理' }
            },
            {
                path: 'profile',
                name: 'LeaderProfile',
                component: () => import('@/views/student/Profile.vue'),
                meta: { title: '个人中心' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('role')

    if (to.meta.title) {
        document.title = to.meta.title as string + ' - 高校学科竞赛管理系统'
    } else {
        document.title = '高校学科竞赛管理系统'
    }

    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.meta.roles) {
        // Multi-role check
        const allowed = to.meta.roles as string[]
        if (!userRole || !allowed.includes(userRole)) {
            next('/login')
        } else {
            next()
        }
    } else if (to.meta.role && to.meta.role !== userRole) {
        next('/login')
    } else {
        next()
    }
})

export default router

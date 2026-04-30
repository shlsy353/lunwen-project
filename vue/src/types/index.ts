export interface User {
    id: number
    username: string
    password?: string
    name: string
    avatar?: string
    gender: number
    phone?: string
    email?: string
    role: 'ADMIN' | 'TEACHER' | 'LEADER' | 'STUDENT'
    studentId?: string
    teacherId?: string
    grade?: string
    collegeId?: number
    majorId?: number
    specialtyField?: string
    createTime?: string
    isDeleted?: number
    leaderStatus?: number
    updateTime?: string
}

export interface Registration {
    id: number
    competitionId: number
    studentId: number
    status: number
    feedback: string
    createTime: string
}

export interface College {
    id: number
    name: string
    introduction?: string
}

export interface Major {
    id: number
    collegeId: number
    name: string
    introduction?: string
}

export interface CompetitionType {
    id: number
    name: string
    introduction?: string
}

export interface Competition {
    id: number
    name: string
    typeId: number
    content?: string
    purpose?: string
    location?: string
    level?: string
    organizer?: string
    undertaker?: string
    competitionDate?: string
    status: number
    file?: string
    minPlayers?: number
    maxPlayers?: number
}

export interface Team {
    id: number
    name: string
    leaderId: number
    teacherIds?: string
    status: number
    createTime: string
}

export interface TeamMember {
    id: number
    teamId: number
    studentId: number
    role: string
}

export interface WorkSubmission {
    id: number
    studentId: number
    competitionId: number
    teamId?: number
    school: string
    workFile: string
    workFeatures: string
    createTime: string
}

export interface Announcement {
    id: number
    title: string
    content?: string
    createTime?: string
}

export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
}

export interface Result<T> {
    code: number
    message: string
    data: T
}

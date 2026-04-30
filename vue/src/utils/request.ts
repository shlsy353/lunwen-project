import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const service: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// Request interceptor
service.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        console.error('Request error:', error)
        return Promise.reject(error)
    }
)

// Response interceptor
service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || 'Error')
            if (res.code === 401) {
                localStorage.removeItem('token')
                localStorage.removeItem('role')
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.message || 'Error'))
        }
        return res
    },
    (error) => {
        console.error('Response error:', error)
        ElMessage.error(error.message || 'Network error')
        return Promise.reject(error)
    }
)

export default service

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['token'] = token
  }
  return config
})
service.interceptors.response.use(
  res => {
    const { code, msg, data } = res.data
    if (code === 201) {
      ElMessage.error(msg || '登录已失效，请重新登录')
      router.push('/login')
      return Promise.reject('登录失效')
    }
    if (code === 202) {
      ElMessage.error(msg || '权限不足，禁止访问')
      return Promise.reject('权限不足')
    }
    return {data,code,msg}
  },
  err => {
    return Promise.reject(err)
  }
)

export default service
import axios from 'axios'
import router from "../router";

// 清理旧的冗余token数据
function cleanupOldTokens() {
    const keepToken = localStorage.getItem("token")
    localStorage.removeItem("jwt_token")
    localStorage.removeItem("vlive-auth-token")
    localStorage.removeItem("vlive-current-user")
    if (keepToken) {
        localStorage.setItem("token", keepToken)
    }
}

// 在应用启动时清理一次
cleanupOldTokens()

const request = axios.create({
    baseURL: '/api',
    timeout: 5000
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';

    // 取出 localStorage or sessionStorage 里面缓存的用户信息
    let userJson = localStorage.getItem("user") || sessionStorage.getItem("user")
    // do not force redirect when calling login/register endpoints
    const url = config.url || ''
    if (!userJson && !(url.includes('/login') || url.includes('/register'))) {
        router.push("/login")
    }

    // attach token if present
    // attach token if present in sessionStorage or localStorage
        const token = sessionStorage.getItem("token") || sessionStorage.getItem("jwt_token") || localStorage.getItem("token") || localStorage.getItem("jwt_token")
    if (token) {
      config.headers['token'] = token  // 后端期望的请求头名称是 'token'
      console.log('Attaching token to request:', token)
    } else {
      console.warn('No token found in storage')
    }
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        console.log('err' + error) // for debug
        return Promise.reject(error)
    }
)


export default request


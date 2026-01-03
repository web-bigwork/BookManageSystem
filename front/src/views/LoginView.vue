<template>
<div  class="login-container"  >
    <el-form ref="form" :model="form"   :rules="rules" class="login-page">
      <h2 class="title" style="margin-bottom: 20px">系统登陆</h2>
      <el-form-item prop="username" >
        <el-input v-model="form.username"  clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><User /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password"  clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
      </el-form-item>
      <el-form-item>
        <el-button type="primary"  style=" width: 100%" @click="login">登 录</el-button>
      </el-form-item>
      <el-form-item><el-button type="text" @click="$router.push('/register')">前往注册 >> </el-button></el-form-item>
    </el-form>
</div>

</template>

<script>
import request from "../utils/request";
import {ElMessage} from "element-plus";

// (mock users removed — using real backend)

export default {
  name: "LoginView",
  components:{},
  data() {
    return {
      form: {},
      // 当前是否使用本地模拟登录，后端写好后改成 false 即可
      useMock: false,
      rules: {
        username: [
          {
            required: true,
            message: '请输入用户名',
            trigger: 'blur',
          }
        ],
        password: [
          {
            required: true,
            message: '请输入密码',
            trigger: 'blur',
          }
        ]

      }

    }
  },
  methods: {
    login(){
      this.$refs['form'].validate((valid) => {
        if (valid) {
          // // 本地模拟登录，不访问后端
          // if (this.useMock) {
          //   const user = mockLoginUsers.find(
          //     u => u.username === this.form.username && u.password === this.form.password
          //   )
          //   if (!user) {
          //     ElMessage.error("用户名或密码错误（本地模拟）")
          //     return
          //   }
          //   // 模拟后端返回的用户信息
          //   const userInfo = {
          //     id: user.id,
          //     username: user.username,
          //     role: user.role
          //   }
          //   sessionStorage.setItem("user", JSON.stringify(userInfo))
          //   ElMessage.success("登录成功（本地模拟）")
          //   this.$router.push("/dashboard")
          //   return
          // }

          // 真实后端请求（接好后端再使用）
          request.post("/login", this.form).then(res => {
            // 后端 Result.success: code=1, data={token,userRole,...}
            if (res.code === 1 || res.code === 0) {
              const resp = res.data || {}
              // 兜底从多处取 token
              const token = resp.token || resp.jwt_token || res.token || res.jwt_token || (typeof resp === 'string' ? resp : null)
              if (!token) {
                console.warn('No token found in response:', resp)
                ElMessage.error('登录失败：未收到 token，后端可能未正确返回')
                return
              }
              // 持久化 token 到 sessionStorage + localStorage，便于刷新后依然可用
              try {
                sessionStorage.setItem('token', token)
                sessionStorage.setItem('jwt_token', token)
                localStorage.setItem('token', token)
                localStorage.setItem('jwt_token', token)
              } catch (e) {
                console.error('Error storing token:', e)
              }
              // 组装用户信息
              const role = resp.userRole != null ? Number(resp.userRole) : (resp.roleName === 'admin' ? 1 : (resp.role || 0))
              const userObj = {
                id: resp.id || resp.userId || null,
                username: resp.username || this.form.username,
                role
              }
              sessionStorage.setItem("user", JSON.stringify(userObj))
              localStorage.setItem("user", JSON.stringify(userObj))
              if (userObj.username) {
                sessionStorage.setItem("username", userObj.username)
                localStorage.setItem("username", userObj.username)
              }
              ElMessage.success(res.msg || "登录成功")
              this.$router.push("/dashboard")
            } else {
              ElMessage.error(res.msg || "登录失败")
            }
          }).catch(err => {
            console.error('login error', err)
            ElMessage.error('登录请求失败')
          })
        }
      })

    }
  }
}

</script>

<style scoped>
.login-container {
  position: fixed;
  width: 100%;
  height: 100vh;
  background: url('../img/bg2.svg');
  background-size: contain;
  overflow: hidden;
}
.login-page {
  border-radius: 5px;
  margin: 180px auto;
  width: 350px;
  padding: 35px 35px 15px;
  background: #fff;
  border: 1px solid #eaeaea;
  box-shadow: 0 0 25px #cac6c6;
}
</style>
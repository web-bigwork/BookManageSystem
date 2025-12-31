<template>
<div  class="login-container"  >
    <el-form ref="form" :model="form"    :rules="rules" class="login-page">
      <h2 class="title" style="margin-bottom: 20px">用户注册</h2>
      <el-form-item prop="username" >
        <el-input v-model="form.username" placeholder="请输入用户名" clearable >
          <template #prefix>
            <el-icon class="el-input__icon"><User/></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password"  placeholder="请输入密码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="confirm">
        <el-input v-model="form.confirm" placeholder="请再次确认密码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><Phone /></el-icon>
          </template>
        </el-input>
      </el-form-item>



      <el-form-item >
        <el-button type="primary" style=" width: 100%" @click="register">注 册</el-button>
      </el-form-item>
      <el-form-item><el-button type="text" @click="$router.push('/login')">前往登录>> </el-button></el-form-item>
    </el-form>
</div>

</template>

<script>
import request from "../utils/request";
import {ElMessage} from "element-plus";

// 本地模拟用户列表已移除，注册时直接调用后端接口
// const mockRegisterUsers = []

export default {
  name: "RegisterView",
  components:{},
  data(){
    return{
      form:{
        role: '2',  // 注册页面默认并且只能注册普通用户
        phone: ''
      },
      // 关闭本地模拟注册，直接调用后端 /register 写入数据库
      useMock: false,
      rules: {
        username: [
          {
            required: true,
            message: '请输入用户名',
            trigger: 'blur',
          },
          {
            min: 2,
            max: 13,
            message: '长度要求为2到13位',
            trigger: 'blur',
          },
        ],
        password: [
          {
            required: true,
            message: '请输入密码',
            trigger: 'blur',
          }
        ],
        phone: [
          {
            required: true,
            message: '请输入手机号',
            trigger: 'blur',
          }
        ],
        confirm:[
          {
            required:true,
            message:"请确认密码",
            trigger:"blur"
          }
        ],
      }
    }
    },

  methods:{
    register(){
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if(this.form.password != this.form.confirm)
          {
            ElMessage.error("两次密码输入不一致")
            return
          }
          // 本地模拟注册，不访问后端
          // if (this.useMock) {
          //   const exist = mockRegisterUsers.find(u => u.username === this.form.username)
          //   if (exist) {
          //     ElMessage.error("用户名已存在（本地模拟）")
          //     return
          //   }
          //   const maxId = mockRegisterUsers.reduce((max, item) => Math.max(max, item.id), 0)
          //   const newUser = {
          //     id: maxId + 1,
          //     username: this.form.username,
          //     password: this.form.password,
          //     nickName: this.form.username,
          //     role: 2   // 注册页面只能生成普通用户
          //   }
          //   mockRegisterUsers.push(newUser)
          //   ElMessage.success("注册成功（本地模拟）")
          //   this.$router.push("/login")
          //   return
          // }

          // 真实后端请求：字段名与后端 UserController 对齐
          request.post("/register",{
            username: this.form.username,
            password: this.form.password,
            checkpassword: this.form.confirm,
            phone: this.form.phone,
            userRole: 0  // 注册用户为普通用户，后端只接受 0(普通用户) 或 1(管理员)
          }).then(res=>{
            if(res.code === 1 || res.code === 0)
            {
              ElMessage.success(res.msg || "注册成功")
              this.$router.push("/login")
            }
            else {ElMessage.error(res.msg || "注册失败")}
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
  height: 100%;
  background: url('../img/bg2.svg');
  background-size: contain;
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
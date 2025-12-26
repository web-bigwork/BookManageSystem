<template>
  <div>
    <el-card style="width: 40%; margin-left: 120px; margin-top: 40px" >
        <h2 style="padding: 30px">个人信息</h2>
      <el-form :model="form" ref="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input style="width: 80%" v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="权限">
          <el-select v-model="form.role" placeholder="选择角色" style="width: 80%">
            <el-option label="管理员" :value="1"></el-option>
            <el-option label="读者" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="电话号码">
          <el-input style="width: 80%" v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input style="width: 80%" v-model="form.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input style="width: 80%" v-model="form.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-button type="primary" @click="update">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: "PersonView",
  data() {
    return {
      form: {}
    }
  },
  created() {
    let str = localStorage.getItem("user") || "{}"
    this.form = {
      ...JSON.parse(str),
      newPassword: '',
      confirmPassword: ''
    }
  },
  methods: {
    update() {
      const payload = {
        id: this.form.id,
        username: this.form.username,
        phone: this.form.phone,
        role: this.form.role,
        newPassword: this.form.newPassword,
        confirmPassword: this.form.confirmPassword
      }

      request.post("/updateUser", payload).then(res => {
        console.log(res)
        if (res.code === 1) {
          ElMessage.success("更新成功")
          const newUser = {
            id: this.form.id,
            username: this.form.username,
            role: this.form.role,
            phone: this.form.phone
          }
          localStorage.setItem("user", JSON.stringify(newUser))
          localStorage.setItem("username", this.form.username)
          if (this.form.newPassword) {
            // 密码变更后让用户重新登录
            localStorage.removeItem("token")
            this.$router.push("/login")
            ElMessage.info("密码已修改，请重新登录")
          }
          // 清空密码输入
          this.form.newPassword = ''
          this.form.confirmPassword = ''
          this.$emit("userInfo")
        } else {
          ElMessage.error(res.msg)
        }
      })

    }
  }
}
</script>

<style>
.avatar-uploader  {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.box-card {
  width: 60%;
  margin: auto;
  padding: 20px;
}
</style>

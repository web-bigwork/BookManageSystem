<template>
 <div style="height: 50px; line-height:50px; border-bottom: 1px solid #ccc; display: flex">
   <div style="width: 200px; padding-left:30px; font-weight: bold; color:dodgerblue">
     <img :src="imgUrl" class="icon" >
     图书管理系统</div>
   <div style="flex: 1"></div>
   <div style="width: 100px">
     <el-dropdown>
      <span class="el-dropdown-link">
        {{user.nickName}} <el-icon class="el-icon--right">
          <arrow-down />
          </el-icon>
      </span>
       <template #dropdown>
         <el-dropdown-menu>
           <el-dropdown-item @click="exit">退出系统</el-dropdown-item>
         </el-dropdown-menu>
       </template>
     </el-dropdown>
   </div>
 </div>
</template>

<script>
import {ElMessage} from "element-plus";

export default {
  name: "AppHeader",
  data(){
    return{
      user:{},
      imgUrl:require("../assets/icon/login.png")
    }
  },
  created(){
    let userStr = localStorage.getItem("user")||"{}"
    this.user = JSON.parse(userStr)
  },
  methods:{
    exit(){
      const baseKey = 'lib_hasVisited_v1'
      const userId = this.user && this.user.id
      localStorage.removeItem(baseKey)
      if (userId) localStorage.removeItem(`${baseKey}_user_${userId}`)
      localStorage.removeItem("user")
      this.$router.push("/login")
      ElMessage.success("退出系统成功")
    }
  }

}
</script>

<style scoped>
.icon {
  width: 40px;
  height: 40px;
  padding-top: 5px;
  padding-right: 10px;
}
</style>
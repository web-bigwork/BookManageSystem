<template>
<div>


  <el-menu
      style="width: 200px; min-height: calc(100vh - 50px);"
      :default-active="path"
      class="el-menu-vertical-demo"
      router
      background-color="#30333c" text-color="#fff"
  >
    <el-menu-item index="/dashboard" >
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#icondashboard "></use>
      </svg>
      <span>展示板</span>
    </el-menu-item>
    <el-menu-item index="/user" v-if="user.role == 1">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconreader "></use>
      </svg>
      <span>用户管理</span>
    </el-menu-item>
    <el-menu-item index="/book" v-if="user.role == 1" >
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconbook "></use>
      </svg>
      <span>书籍管理</span>
    </el-menu-item>
    <el-menu-item index="/booksearch" v-if="user.role === 0">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconbook "></use>
      </svg>
      <span>图书查询</span>
    </el-menu-item>
    <el-menu-item index="/borrow-manage" v-if="user.role == 1">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconlend-record "></use>
      </svg>
      <span>借阅管理</span>
    </el-menu-item>
    <el-menu-item index="/borrow-search" v-if="user.role === 0">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconlend-record"></use>
      </svg>
      <span>借阅信息</span>
    </el-menu-item>
    
  </el-menu>

</div>
</template>

<script>



export default {
  name: "AppAside",
  components:{},
  created(){
    const raw = sessionStorage.getItem("user") || localStorage.getItem("user") || "{}"
    this.user = JSON.parse(raw)
    const role = this.user.role == null ? 0 : Number(this.user.role)
    this.user.role = Number.isNaN(role) ? 0 : role
  },
  data(){
    return {
      user:{},
      path: this.$route.path
    }
  }
}
</script>

<style scoped>
.icon {
  width: 30px;
  height: 30px;
  padding-top: 5px;
  padding-right: 10px;
}
</style>
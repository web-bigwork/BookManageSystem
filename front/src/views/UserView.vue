<template>
  <div class="home" style ="padding: 10px">
<!-- 按钮-->
<!-- 搜索-->
    <div style="margin: 10px 0;">
      <el-form inline="true" size="small">
        <el-form-item label="用户编号" >
      <el-input v-model="search1" placeholder="请输入用户编号"  clearable>
        <template #prefix><el-icon class="el-input__icon"><search/></el-icon></template>
      </el-input>
          </el-form-item >
        <el-form-item label="用户名" >
          <el-input v-model="search2" placeholder="请输入用户名"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item label="电话号码" >
          <el-input v-model="search3" placeholder="请输入电话号码"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item>
      <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini"  type="danger" @click="clear">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right" v-if="user.userRole == 1">
          <el-button type="primary" size="mini" @click="add">添加用户</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- 按钮-->
    <div style="margin: 10px 0;" >
      <el-button type="primary" size="mini" @click="add" v-if="(user.userRole == 1 || user.role == 1)" style="margin-right: 10px">
        添加用户
      </el-button>
      <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.userRole == 1">
        <template #reference>
          <el-button type="danger" size="mini" >批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
<!-- 数据字段-->
    <el-table :data="tableData" stripe border="true"  @selection-change="handleSelectionChange" >
      <el-table-column v-if="(user.userRole ==1 || user.role == 1)"
                       type="selection"
                       width="55">
      </el-table-column>
      <el-table-column prop="id" label="用户编号" sortable />
      <el-table-column prop="userName" label="用户名" />
      <el-table-column prop="phone" label="电话号码" />
      <el-table-column label="角色">
        <template v-slot="scope">
          <el-tag type="success" v-if="scope.row.userRole == 1">管理员</el-tag>
          <el-tag type="info" v-else>用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" >
        <template v-slot="scope">
          <el-button  size="mini" @click ="handleEdit(scope.row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button type="danger" size="mini" >删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
<!--    分页-->
    <div style="margin: 10px 0">
      <el-pagination
          v-model:currentPage="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next, jumper"
          :total="total"
          @current-change="handleCurrentChange"
      >
      </el-pagination>

      <el-dialog v-model="dialogVisible" title="编辑用户信息" width="30%">
        <el-form :model="form" label-width="120px">
          <el-form-item label="用户名">
            <el-input style="width: 80%" v-model="form.userName"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input style="width: 80%" v-model="form.password" show-password></el-input>
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input style="width: 80%" v-model="form.checkpassword" show-password></el-input>
          </el-form-item>
          <el-form-item label="电话号码">
            <el-input style="width: 80%" v-model="form.phone"></el-input>
          </el-form-item>
          <el-form-item label="角色">
            <div>
              <el-radio v-model.number="form.userRole" :label="1">管理员</el-radio>
              <el-radio v-model.number="form.userRole" :label="0">用户</el-radio>
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import request from "../utils/request";
import {ElMessage} from "element-plus";

export default {
  created(){
    this.load()
    let userStr = sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(userStr)
  },
  name: 'UserView',
  methods: {
    handleSelectionChange(val){
      this.ids = val.map(v => v.id)
    },
    deleteBatch(){
      if (!this.ids.length) {
        ElMessage.warning("请选择数据！")
        return
      }
      // 本地模拟删除
      // if (this.useMock) {
      //   this.ids.forEach(id => {
      //     const index = mockUsers.findIndex(u => u.id === id)
      //     if (index !== -1) {
      //       mockUsers.splice(index, 1)
      //     }
      //   })
      //   ElMessage.success("批量删除成功（本地模拟）")
      //   this.load()
      //   return
      // }
      // 真实后端请求（接好后端再打开）
      request.post("/user/deleteBatch", this.ids).then(res => {
        if (res.code === '0') {
          ElMessage.success("批量删除成功")
          this.load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    },
    load(){
      // 优先执行单项精确查询：按 id / username / phone
      if (this.search1) {
        // search by id -> GET /user/{id}
        const id = Number(this.search1)
        if (!Number.isNaN(id)) {
        const urlById = `/user/${id}`
        console.log('Requesting by id:', urlById)
        request.get(urlById).then(res => {
            if (res.code === 1) {
              this.tableData = [res.data]
              this.total = 1
            } else {
              this.tableData = []
              this.total = 0
              ElMessage.warning(res.msg || '未找到用户')
            }
          }).catch(err => {
            console.error('Error fetching by id', err, err?.response)
            const status = err?.response?.status
            ElMessage.error(`查询用户失败${status ? '（HTTP ' + status + ')' : ''}`)
          })
          return
        }
      }

      if (this.search2) {
        // search by username -> GET /user/by-username?username=
        const urlByName = '/user/by-username'
        console.log('Requesting by username:', urlByName, this.search2)
        request.get(urlByName, { params: { username: this.search2 } }).then(res => {
          if (res.code === 1 && res.data) {
            this.tableData = [res.data]
            this.total = 1
          } else {
            this.tableData = []
            this.total = 0
            ElMessage.warning(res.msg || '未找到用户')
          }
        }).catch(err => {
          console.error('Error fetching by username', err, err?.response)
          const status = err?.response?.status
          ElMessage.error(`查询用户失败${status ? '（HTTP ' + status + ')' : ''}`)
        })
        return
      }

      if (this.search3) {
        // search by phone -> GET /user/by-phone?phone=
        const urlByPhone = '/user/by-phone'
        console.log('Requesting by phone:', urlByPhone, this.search3)
        request.get(urlByPhone, { params: { phone: this.search3 } }).then(res => {
          if (res.code === 1 && res.data) {
            this.tableData = [res.data]
            this.total = 1
          } else {
            this.tableData = []
            this.total = 0
            ElMessage.warning(res.msg || '未找到用户')
          }
        }).catch(err => {
          console.error('Error fetching by phone', err, err?.response)
          const status = err?.response?.status
          ElMessage.error(`查询用户失败${status ? '（HTTP ' + status + ')' : ''}`)
        })
        return
      }

      // 默认分页查询 /users
      request.get("/users",{
        params:{
          page: this.currentPage,
          pageSize: this.pageSize,
        }
      }).then(res =>{
        console.log(res)
        // 兼容后端 PageBean 命名 rows/total 或 records/total
        this.tableData = res.data.rows || res.data.records || []
        this.total = res.data.total || 0
      }).catch(err => {
        console.error(err)
        ElMessage.error('获取用户列表失败')
      })
    },
    clear(){
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },

    handleDelete(id){
      // 真实后端请求
      request.get("/deleteById", {
        params: { id: id }
      }).then(res =>{
        console.log(res)
        if(res.code == 1 ){
          ElMessage.success("删除成功")
        }
        else
          ElMessage.error(res.msg)
        this.load()
      })
    },


    add(){
      this.dialogVisible= true
      this.form ={
        userRole: 0,  // 新增用户默认普通用户
      }
    },
    save(){
      // 本地模拟增删改
      // if (this.useMock) {
      //   if (this.form.id) {
      //     const index = mockUsers.findIndex(u => u.id === this.form.id)
      //     if (index !== -1) {
      //       mockUsers.splice(index, 1, { ...mockUsers[index], ...this.form })
      //       ElMessage.success('更新成功（本地模拟）')
      //     } else {
      //       ElMessage.error('未找到该用户（本地模拟）')
      //     }
      //   } else {
      //     const maxId = mockUsers.reduce((max, item) => Math.max(max, item.id), 0)
      //     const newItem = { ...this.form, id: maxId + 1 }
      //     mockUsers.push(newItem)
      //     ElMessage.success('添加成功（本地模拟）')
      //   }
      //   this.load()
      //   this.dialogVisible = false
      //   return
      // }

      // 真实后端请求
      if(this.form.id){
        // backend has two update handlers; include id and userRole when present
        const payload = {
          id: this.form.id,
          old_username: this.form.old_username || this.form.userName,
          username: this.form.userName,
          password: this.form.password || '',
          checkpassword: this.form.checkpassword || ''
        }
        if (this.form.userRole !== undefined) {
          payload.userRole = this.form.userRole
        }
        request.post("/updateUser", payload).then(res =>{
          console.log(res)
          if(res.code == 1){
            ElMessage({
              message: res.msg || '更新成功',
              type: 'success',
            })
          }
          else {
            ElMessage.error(res.msg)
          }

          this.load()
          this.dialogVisible = false
        })
      }
      else {
        // add user via backend /addUser
        const payload = {
          username: this.form.userName,
          password: this.form.password,
          checkpassword: this.form.checkpassword,
          phone: this.form.phone,
          userRole: this.form.userRole
        }
        request.post("/addUser", payload).then(res =>{
          console.log(res)
          if(res.code == 1){
            ElMessage.success(res.msg || '添加成功')
          }
          else {
            ElMessage.error(res.msg)
          }
          this.load()
          this.dialogVisible = false
        })
      }

    },


    handleEdit(row){
      this.form = JSON.parse(JSON.stringify(row))
      // preserve old username for backend update
      this.form.old_username = row.userName || row.username
      this.dialogVisible = true
    },
    handleCurrentChange(pageNum){
      this.currentPage = pageNum
      this.load()
    },
  },
  data() {
    return {
      form: {},
      dialogVisible : false,
      search1:'',
      search2:'',
      search3:'',
      search4:'',
      total:10,
      currentPage:1,
      pageSize: 5,
      tableData: [

      ],
      user:{},
      ids:[],
      // 当前是否使用本地模拟数据，后端写好后设为 false 即可真实联调
      useMock: false,
    }
  },
}
</script>

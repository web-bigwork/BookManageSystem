<template>
  <div class="home" style ="padding: 10px">
    <!-- 搜索-->
    <div style="margin: 10px 0;">

      <el-form inline="true" size="small" >
        <el-form-item label="图书编号" >
          <el-input v-model="search1" placeholder="请输入图书编号"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search/></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item label="图书名称" >
          <el-input v-model="search2" placeholder="请输入图书名称"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini"  type="danger" @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- 数据字段-->
    <el-table :data="tableData" stripe border="true">
      <el-table-column prop="isbn" label="图书编号" sortable />
      <el-table-column prop="bookName" label="图书名称" />
      <el-table-column prop="lendtime" label="借出时间" />
      <el-table-column prop="deadtime" label="应归还时间" />
      <el-table-column prop="returnTime" label="实际还书时间" />
      <el-table-column prop="status" label="借阅状态">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status == 0" type="warning">在借</el-tag>
          <el-tag v-else-if="scope.row.status == 1" type="success">已还</el-tag>
          <el-tag v-else-if="scope.row.status == 2" type="danger">逾期</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作">
        <template v-slot="scope">
          <el-button v-if="scope.row.status == 0" type="primary" size="mini" @click="handleRenew(scope.row)">续借</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--    分页-->
    <div style="margin: 10px 0">
      <el-pagination
          v-model:currentPage="currentPage"
          :page-size="5"
          layout="total, prev, pager, next, jumper"
          :total="total"
          @current-change="handleCurrentChange"
      >
      </el-pagination>

    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import request from "../utils/request";
import {ElMessage} from "element-plus";

// 本地模拟借阅状态数据，方便在未接后端时调试前端
const mockBookWithUser = [
  {
    id: 1,
    isbn: '978730000001',
    bookName: 'Java 从入门到放弃',
    nickName: '张三',
    lendtime: '2025-01-01 10:00:00',
    deadtime: '2025-02-01 10:00:00',
    returnTime: '',
    status: 0,
    prolong: 1
  },
  {
    id: 2,
    isbn: '978730000002',
    bookName: 'Vue3 实战指南',
    nickName: '李四',
    lendtime: '2025-01-10 15:30:00',
    deadtime: '2025-02-09 15:30:00',
    returnTime: '2025-02-05 10:00:00',
    status: 1,
    prolong: 0
  },
  {
    id: 3,
    isbn: '978730000003',
    bookName: '数据库系统概论',
    nickName: '王五',
    lendtime: '2024-12-20 09:00:00',
    deadtime: '2025-01-19 09:00:00',
    returnTime: '',
    status: 0,
    prolong: 2
  }
]

export default {
  created(){
    const raw = localStorage.getItem("user") || sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(raw)
    if (this.user.role !== undefined && this.user.role !== null) {
      this.user.role = Number(this.user.role)
    }
    
    // 如果 user.id 为空，尝试从 token 中恢复
    if (!this.user.id) {
      const token = localStorage.getItem("token") || sessionStorage.getItem("token")
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]))
          this.user.id = payload.id || payload.userId || payload.sub
          console.log('从 token 恢复 user.id:', this.user.id)
        } catch (e) {
          console.error('解析 token 失败:', e)
        }
      }
    }
    
    this.load()
  },
  name: 'BookWithUserView',
  methods: {
    handleRenew(row) {
      if (!row.id) {
        ElMessage.error('记录ID缺失，无法续借')
        return
      }
      const payload = { borrowId: row.id }
      // 后端控制器映射为 @PostMapping("/renewById")
      request.post('/renewById', payload).then(res => {
        if (res.code === 1) {
          ElMessage.success(res.msg || '续借成功')
          this.load()
        } else {
          ElMessage.error(res.msg || '续借失败')
        }
      }).catch(err => {
        console.error('续借失败:', err)
        ElMessage.error('续借失败，请稍后重试')
      })
    },
    load(){
      // 本地模拟：只显示当前用户已归还的记录
      if (this.useMock) {
        const userName = this.user.username || this.user.nickName || ''
        let list = mockBookWithUser.filter(item => {
          const matchUser = userName ? item.nickName === userName : true
          const matchReturned = item.status === 1
          const matchIsbn = this.search1 ? String(item.isbn).includes(this.search1) : true
          const matchName = this.search2 ? (item.bookName || '').includes(this.search2) : true
          return matchUser && matchReturned && matchIsbn && matchName
        })
        this.total = list.length
        const start = (this.currentPage - 1) * this.pageSize
        const end = start + this.pageSize
        this.tableData = list.slice(start, end)
        return
      }

      if (!this.user.id) {
        ElMessage.error("用户ID不存在，请重新登录")
        return
      }

      // 调用新接口：GET /borrow/user/{userId}/records?page=1&pageSize=10
      console.log('请求路径:', `/borrow/user/${this.user.id}/records`)
      request.get(`/borrow/user/${this.user.id}/records`, {
        params: {
          page: this.currentPage,
          pageSize: this.pageSize
        }
      }).then(res => {
        console.log('用户借阅记录:', res)
        const data = res.data || {}
        let rows = Array.isArray(data.rows) ? data.rows : (Array.isArray(data.records) ? data.records : [])
        
        // 打印第一条记录的完整字段，检查是否有 return_time 和 status
        if (rows.length > 0) {
          console.log('第一条记录完整数据:', rows[0])
          console.log('return_time:', rows[0].return_time)
          console.log('status:', rows[0].status)
        }
        
        // 同时加载书籍数据用于名称匹配
        request.get("/books").then(booksRes => {
          const books = Array.isArray(booksRes.data) ? booksRes.data : (booksRes.data?.records || booksRes.data?.rows || [])
          
          // 创建书籍 ID 到名称的 map
          const bookMap = {}
          books.forEach(book => {
            bookMap[book.id] = book.title || book.name || ''
          })
          
          // 字段映射和规范化（后端返回驼峰命名：bookId, bookTitle, userName, borrowDate, dueDate, returnDate）
          const normalized = rows.map(item => ({
            id: item.id,
            isbn: item.bookId || item.book_id || '',
            bookName: item.bookTitle || bookMap[item.bookId] || item.book_name || '',
            lendtime: item.borrowDate ? item.borrowDate.split('T').join(' ').substring(0, 19) : '',
            deadtime: item.dueDate ? item.dueDate.split('T').join(' ').substring(0, 19) : '',
            returnTime: item.returnDate ? item.returnDate.split('T').join(' ').substring(0, 19) : '',
            status: item.status != null ? Number(item.status) : 0
          }))

          // 前端过滤
          const filtered = normalized.filter(item => {
            const matchIsbn = this.search1 ? String(item.isbn).includes(this.search1) : true
            const matchName = this.search2 ? (item.bookName || '').includes(this.search2) : true
            return matchIsbn && matchName
          })

          this.tableData = filtered
          this.total = data.total || filtered.length
        }).catch(err => {
          console.error("加载书籍数据失败:", err)
          ElMessage.error("加载数据失败，请稍后重试")
        })
      }).catch(err => {
        console.error("查询失败:", err)
        console.error("请求的完整URL:", err.config?.url)
        console.error("错误详情:", err.response?.data)
        ElMessage.error(`查询失败：${err.response?.status || ''} ${err.response?.data?.msg || '请检查后端接口是否正确'}`)
        this.tableData = []
        this.total = 0
      })
    },
    clear(){
      this.search1 = ""
      this.search2 = ""
      this.load()
    },
    handleCurrentChange(pageNum){
      this.currentPage = pageNum
      this.load()
    },

  },
  data() {
    return {
      search1:'',
      search2:'',
      total:10,
      currentPage:1,
      pageSize: 5,
      tableData: [],
      user:{},
      // 当前是否使用本地模拟借阅状态数据，后端写好后设为 false 即可真实联调
      useMock: false,
    }
  },
}
</script>

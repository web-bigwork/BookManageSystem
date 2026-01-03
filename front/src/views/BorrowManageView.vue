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
        <el-form-item label="借阅者" v-if="user.role == 1">
          <el-input v-model="search3" placeholder="请输入借阅者昵称"  clearable>
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
    <!-- 按钮-->
    <div style="margin: 10px 0;" >
      <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.role == 1">
        <template #reference>
          <el-button type="danger" size="mini" >批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!-- 数据字段-->
    <el-table :data="tableData" stripe border="true" @selection-change="handleSelectionChange">
      <el-table-column v-if="user.role ==1"
          type="selection"
          width="55">
      </el-table-column>
      <el-table-column prop="isbn" label="图书编号" sortable />
      <el-table-column prop="bookName" label="图书名称" />
      <el-table-column prop="nickName" label="借阅者" />
      <el-table-column prop="lendtime" label="借出时间" />
      <el-table-column prop="deadtime" label="应归还时间" />
      <el-table-column prop="returnTime" label="实际还书时间" />
      <el-table-column label="可借数量">
        <template v-slot="scope">
          <span>{{ (scope.row.availableQty ?? scope.row.available_qty) ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="借阅状态">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status == 0" type="warning">在借</el-tag>
          <el-tag v-else-if="scope.row.status == 1" type="success">已还</el-tag>
          <el-tag v-else-if="scope.row.status == 2" type="danger">逾期</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" >
        <template v-slot="scope">
          <el-button  size="mini" @click ="handleEdit(scope.row)">修改</el-button>
          <el-popconfirm
            v-if="user.role == 1 && scope.row.status == 1"
            title="确认撤销还书？"
            @confirm="handleUndoReturn(scope.row)">
            <template #reference>
              <el-button type="warning" size="mini">撤销还书</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row) ">
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
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      >
      </el-pagination>

      <el-dialog v-model="dialogVisible2" title="修改借阅信息" width="30%">
        <el-form :model="form" label-width="120px">

          <el-form-item label="图书编号">
            <el-input style="width: 80%" v-model="form.isbn"></el-input>
          </el-form-item>
          <el-form-item label="图书名称">
            <el-input style="width: 80%" v-model="form.bookName"></el-input>
          </el-form-item>
          <el-form-item label="借阅者">
            <el-input style="width: 80%" v-model="form.nickName"></el-input>
          </el-form-item>
          <el-form-item label="借出时间">
            <el-input style="width: 80%" v-model="form.lendtime"></el-input>
          </el-form-item>
          <el-form-item label="应归还时间">
            <el-input style="width: 80%" v-model="form.deadtime"></el-input>
          </el-form-item>
          <el-form-item label="实际还书时间">
            <el-input style="width: 80%" v-model="form.returnTime"></el-input>
          </el-form-item>
          <el-form-item label="借阅状态">
            <el-radio-group v-model.number="form.status" @change="onStatusChange">
              <el-radio :label="0">已还</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible2 = false">取 消</el-button>
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
  async created(){
    const raw = localStorage.getItem("user") || sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(raw)
    if (this.user.role !== undefined && this.user.role !== null) {
      this.user.role = Number(this.user.role)
    }
    // 黑名单判断
    try {
      const { isUserInBlacklist } = await import('../utils/blacklist')
      const isBlack = await isUserInBlacklist(this.user.id)
      if (isBlack) {
        this.isBlacklisted = true
        this.$nextTick(() => {
          ElMessage.error('您已被列入黑名单，无法进行借书相关操作！')
        })
        return
      }
    } catch (e) {
      // 黑名单接口异常也允许正常进入页面
      this.isBlacklisted = false
    }
    this.load()
  },
  name: 'BorrowManageView',
  methods: {

    handleSelectionChange(val){
      this.forms = val
    },
    deleteBatch(){
      if (!this.forms.length) {
        ElMessage.warning("请选择数据！")
        return
      }
      
      ElMessage.warning("批量删除功能暂不支持，请逐条删除")
    },
    load(){
      // 本地模拟查询，不访问后端
      if (this.useMock) {
        let list = mockBookWithUser.filter(item => {
          const matchIsbn = this.search1 ? String(item.isbn).includes(this.search1) : true
          const matchName = this.search2 ? (item.bookName || '').includes(this.search2) : true
          const matchNick = this.search3 ? (item.nickName || '').includes(this.search3) : true
          return matchIsbn && matchName && matchNick
        })
        this.total = list.length
        const start = (this.currentPage - 1) * this.pageSize
        const end = start + this.pageSize
        this.tableData = list.slice(start, end)
        return
      }

      // 调用新的后端接口：GET /borrow/records?page=1&pageSize=10
      request.get("/borrow/records", {
        params: {
          page: this.currentPage,
          pageSize: this.pageSize
        }
      }).then(res => {
        console.log('借阅记录:', res)
        const data = res.data || {}
        let rows = Array.isArray(data.rows) ? data.rows : (Array.isArray(data.records) ? data.records : [])
        
        // 同时加载书籍和用户数据用于名称匹配（给足够大的 pageSize，避免只拿到一页导致名称缺失）
        Promise.all([
          request.get("/books", { params: { page: 1, pageSize: 10000 } }),
          request.get("/users", { params: { page: 1, pageSize: 10000 } })
        ]).then(([booksRes, usersRes]) => {
          const books = Array.isArray(booksRes.data) ? booksRes.data : (booksRes.data?.records || booksRes.data?.rows || [])
          const users = Array.isArray(usersRes.data) ? usersRes.data : (usersRes.data?.records || usersRes.data?.rows || [])
          
          // 创建 ID 到名称的 map
          const bookMap = {}
          const bookQtyMap = {}
          const userMap = {}
          books.forEach(book => {
            bookMap[book.id] = book.title || book.name || book.bookTitle || ''
            // 兼容 available_qty / availableQty
            let qty = null
            if (Object.prototype.hasOwnProperty.call(book, 'available_qty')) qty = book.available_qty
            if (qty == null && Object.prototype.hasOwnProperty.call(book, 'availableQty')) qty = book.availableQty
            if (qty == null && Object.prototype.hasOwnProperty.call(book, 'availableqty')) qty = book.availableqty
            bookQtyMap[book.id] = qty
          })
          users.forEach(user => {
            userMap[user.id] = user.userName || user.username || user.nickName || ''
          })
          
          // 字段映射和规范化，加入书籍名称和用户名称
          const normalized = rows.map(item => ({
            id: item.id,
            // 兼容后端不同命名：book_id/bookId，user_id/userId；以及后端可能已返回bookTitle/userName
            isbn: item.book_id || item.bookId || '',
            bookName: item.book_name || item.bookTitle || bookMap[item.book_id || item.bookId] || '',
            nickName: item.user_name || item.userName || userMap[item.user_id || item.userId] || (item.user_id || item.userId || ''),
            lendtime: (item.borrow_date || item.borrowDate) ? (String(item.borrow_date || item.borrowDate).split('T').join(' ').substring(0, 19)) : '',
            deadtime: (item.due_date || item.dueDate) ? (String(item.due_date || item.dueDate).split('T').join(' ').substring(0, 19)) : '',
            returnTime: (item.return_time || item.return_date || item.returnDate || item.returnTime)
              ? (String(item.return_time || item.return_date || item.returnDate || item.returnTime).split('T').join(' ').substring(0, 19))
              : '',
            // 将该书的可借数量映射到记录上，表格直接渲染
            availableQty: bookQtyMap[item.book_id || item.bookId] ?? null,
            status: item.status != null ? Number(item.status) : 0
          }))
          
          // 前端过滤（在已获取的数据基础上进一步过滤）
          const filtered = normalized.filter(item => {
            const matchIsbn = this.search1 ? String(item.isbn).includes(this.search1) : true
            const matchName = this.search2 ? (item.bookName || '').includes(this.search2) : true
            const matchNick = this.search3 ? (item.nickName || '').includes(this.search3) : true
            return matchIsbn && matchName && matchNick
          })
          
          this.tableData = filtered
          this.total = data.total || filtered.length
        }).catch(err => {
          console.error("加载书籍/用户数据失败:", err)
          ElMessage.error("加载数据失败，请稍后重试")
        })
      }).catch(err => {
        console.error("查询借阅记录失败:", err)
        ElMessage.error("查询借阅记录失败，请稍后重试")
        this.tableData = []
        this.total = 0
      })
    },
    clear(){
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },
    handleDelete(row){
      if (!row.id) {
        ElMessage.error("记录ID异常")
        return
      }

      // 本地模拟删除
    //   if (this.useMock) {
    //     const index = mockBookWithUser.findIndex(item => item.id === row.id)
    //     if (index !== -1) {
    //       mockBookWithUser.splice(index, 1)
    //       ElMessage.success("删除成功（本地模拟）")
    //       this.load()
    //     } else {
    //       ElMessage.error("未找到该记录（本地模拟）")
    //     }
    //     return
    //   }

      // 调用新的后端删除接口：GET /borrow/delete/{id}
      request.get("/borrow/delete/" + row.id).then(res => {
        console.log(res)
        if (res.code === 1 || res.code === 0) {
          ElMessage.success(res.msg || "删除成功")
          this.load()
        } else {
          ElMessage.error(res.msg || "删除失败")
        }
      }).catch(err => {
        console.error("删除失败:", err)
        ElMessage.error("删除失败，请稍后重试")
      })
    },
    getRemainingBorrow(nickName) {
      // 计算用户当前在借数量，借阅上限为 5
      if (this.useMock) {
        const borrowCount = mockBookWithUser.filter(item => item.nickName === nickName && item.status === 0).length
        return Math.max(0, 5 - borrowCount)
      }
      // 实际联调时可以调用后端接口获取 count，例如 /findBorrowingByUser?userId=...
      return '-'
    },
    save(){
      // 本地模拟修改
      if (this.useMock) {
        if (this.form.id) {
          const index = mockBookWithUser.findIndex(item => item.id === this.form.id)
          if (index !== -1) {
            mockBookWithUser.splice(index, 1, { ...mockBookWithUser[index], ...this.form })
            ElMessage.success('修改信息成功（本地模拟）')
          } else {
            ElMessage.error('未找到该记录（本地模拟）')
          }
        }
        this.load()
        this.dialogVisible2 = false
        return
      }

      // 黑名单判断
      if (this.isBlacklisted) {
        ElMessage.error('您已被列入黑名单，无法借书！')
        return
      }

      if (!this.form.id) {
        ElMessage.error("记录ID异常")
        return
      }

      // 调用新的后端编辑接口：POST /borrow/update
      const toIso = (s) => {
        if (!s) return ''
        // 接受类似 "yyyy-MM-dd HH:mm:ss" 或 "yyyy-MM-ddTHH:mm:ss"，统一转为 ISO "yyyy-MM-ddTHH:mm:ss"
        const t = String(s).trim().replace(' ', 'T')
        return t.substring(0, 19)
      }
      const toSpace = (s) => {
        if (!s) return ''
        // 接受 ISO，转为空格分隔 "yyyy-MM-dd HH:mm:ss"
        const t = String(s).trim().replace('T', ' ')
        return t.substring(0, 19)
      }
      const nowSpace = () => {
        const pad = (n) => String(n).padStart(2, '0')
        const d = new Date()
        return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
      }

      const payload = {
        id: Number(this.form.id),
        status: Number(this.form.status)
      }
      if (this.form.deadtime) payload.dueDate = toIso(this.form.deadtime)
      if (Number(this.form.status) === 0) {
        // 设为在借：不传 returnTime 字段（避免后端解析空字符串失败）
        // 确保之前未设置该字段
        if (payload.returnTime !== undefined) delete payload.returnTime
      } else if (this.form.returnTime) {
        payload.returnTime = toSpace(this.form.returnTime)
      } else if (Number(this.form.status) === 1) {
        // 若设置为已还但未填写实际还书时间，默认当前时间
        payload.returnTime = nowSpace()
      }

      console.log('提交 /borrow/update payload:', payload)
      request.post("/borrow/update", payload).then(res => {
        console.log('更新结果:', res)
        if (res.code === 1) {
          ElMessage.success(res.msg || '修改信息成功')
          this.load()
          this.dialogVisible2 = false
        } else {
          ElMessage.error(res.msg || '修改信息失败')
        }
      }).catch(err => {
        const status = err?.response?.status
        const serverMsg = err?.response?.data?.msg
        console.error("修改失败:", status, err?.response?.data)
        // 兜底：若设为已还且后端 /borrow/update 不可用，尝试调用 /return
        if (Number(this.form.status) === 1 && (status === 404 || status === 500)) {
          request.post('/return', { borrowId: Number(this.form.id) }).then(r => {
            if (r.code === 1) {
              ElMessage.success(r.msg || '已还成功（使用兜底接口）')
              this.load(); this.dialogVisible2 = false
              return
            }
            ElMessage.error(r.msg || '还书失败')
          }).catch(e2 => {
            console.error('兜底还书失败:', e2)
            ElMessage.error(`修改失败（HTTP ${status || ''}）：${serverMsg || '请稍后重试'}`)
          })
          return
        }
        ElMessage.error(`修改失败（HTTP ${status || ''}）：${serverMsg || '请稍后重试'}`)
      })
    },

    async handleEdit(row){
      // 借阅按钮点击时判断黑名单
      if (this.isBlacklisted) {
        ElMessage.error('您已被列入黑名单，无法借书！')
        return
      }
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogVisible2 = true
    },
    handleUndoReturn(row) {
      if (!row.id) {
        ElMessage.error("记录ID异常")
        return
      }

      const payload = { id: Number(row.id), status: 0 }
      request.post('/borrow/update', payload).then(res => {
        if (res.code === 1) {
          ElMessage.success(res.msg || '已撤销还书，状态恢复在借')
          this.load()
          return
        }
        ElMessage.error(res.msg || '撤销失败')
      }).catch(err => {
        const status = err?.response?.status
        const serverMsg = err?.response?.data?.msg
        console.error('撤销失败:', err)
        ElMessage.error(`撤销失败（HTTP ${status || ''}）：${serverMsg || '请稍后重试'}`)
      })
    },
    onStatusChange(val){
      if (Number(val) === 0) {
        // 切回在借，清空实际还书时间输入框
        this.form.returnTime = ''
      }
    },
    handleSizeChange(pageSize){
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum){
      this.currentPage = pageNum
      this.load()
    },

  },
  data() {
    return {
      form: {},
      form2:{},
      form3:{},
      dialogVisible: false,
      dialogVisible2: false,
      search1:'',
      search2:'',
      search3:'',
      total:10,
      currentPage:1,
      pageSize: 10,
      tableData: [],
      user:{},
      forms:[],
      // 当前是否使用本地模拟借阅状态数据，后端写好后设为 false 即可真实联调
      useMock: false,
      isBlacklisted: false,
    }
  },
}
</script>

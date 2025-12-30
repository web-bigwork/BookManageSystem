<template>
  <div class="home" style ="padding: 10px">

    <!-- 搜索-->
    <div style="margin: 10px 0;">
      <el-form inline="true" size="small">
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
        <el-form-item label="作者" >
          <el-input v-model="search3" placeholder="请输入作者"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini" >
            <svg-icon iconClass="search"/>查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini"  type="danger" @click="clear">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right" v-if="numOfOutDataBook!=0">
          <el-popconfirm
              confirm-button-text="查看"
              cancel-button-text="取消"
              :icon="InfoFilled"
              icon-color="red"
              title="您有图书已逾期，请尽快归还"
              @confirm="toLook"
          >
            <template #reference>
              <el-button  type="warning">逾期通知</el-button>
            </template>
          </el-popconfirm>
        </el-form-item>
      </el-form>
    </div>
    <!-- 按钮-->
    <div style="margin: 10px 0;" >
      <!-- <el-button type="primary" size="mini" @click = "add" v-if="user.role === 1" style="margin-right: 10px">上架</el-button> -->
      <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.role === 1">
        <template #reference>
          <el-button type="danger" size="mini" >批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!-- 数据字段-->
    <el-table :data="tableData" stripe border="true" @selection-change="handleSelectionChange">
      <el-table-column v-if="user.role === 1"
                       type="selection"
                       width="55">
      </el-table-column>
      <el-table-column prop="id" label="图书编号" sortable />
      <el-table-column prop="title" label="书名" />
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="available_qty" label="可借阅数量" sortable />
      <el-table-column fixed="right" label="操作" >
        <template v-slot="scope">
          <el-button  size="mini" @click ="handleEdit(scope.row)" v-if="user.role === 1">修改</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row.id)" v-if="user.role === 1">
            <template #reference>
              <el-button type="danger" size="mini" >删除</el-button>
            </template>
          </el-popconfirm>
          <el-button  size="mini" @click ="handlelend(scope.row.id)" v-if="user.role == 0" :disabled="scope.row.hasBorrowed">借阅</el-button>
          <el-popconfirm title="确认还书?" @confirm="handlereturn(scope.row.borrowId)" v-if="user.role == 0">
            <template #reference>
              <el-button type="danger" size="mini" :disabled="!scope.row.borrowId" >还书</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
<!--测试,通知对话框-->
    <el-dialog
        v-model="dialogVisible3"
        v-if="numOfOutDataBook!=0"
        title="逾期详情"
        width="50%"
        :before-close="handleClose"
    >
        <el-table :data="outDateBook" style="width: 100%">
          <el-table-column prop="isbn" label="图书编号" />
          <el-table-column prop="bookName" label="书名" />
          <el-table-column prop="lendtime" label="借阅日期" />
          <el-table-column prop="deadtime" label="截至日期" />
        </el-table>

      <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="dialogVisible3 = false"
        >确认</el-button>
      </span>
      </template>
    </el-dialog>
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

      <el-dialog v-model="dialogVisible" title="上架书籍" width="30%">
        <el-form :model="form" label-width="120px">

          <el-form-item label="书名">
            <el-input style="width: 80%" v-model="form.title"></el-input>
          </el-form-item>
          <el-form-item label="作者">
            <el-input style="width: 80%" v-model="form.author"></el-input>
          </el-form-item>
          <el-form-item label="可借阅数量">
            <el-input style="width: 80%" v-model.number="form.available_qty" type="number" min="0"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </span>
        </template>
      </el-dialog>

      <el-dialog v-model="dialogVisible2" title="修改书籍信息" width="30%">
        <el-form :model="form" label-width="120px">

          <el-form-item label="书名">
            <el-input style="width: 80%" v-model="form.title"></el-input>
          </el-form-item>
          <el-form-item label="作者">
            <el-input style="width: 80%" v-model="form.author"></el-input>
          </el-form-item>
          <el-form-item label="可借阅数量">
            <el-input style="width: 80%" v-model.number="form.available_qty" type="number" min="0"></el-input>
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

// 本地模拟一些图书数据，方便在未接后端时调试前端
const mockBooks = [
  {
    id: 1,
    title: 'Java 从入门到放弃',
    author: '张三',
    available_qty: 5
  },
  {
    id: 2,
    title: 'Vue3 实战指南',
    author: '李四',
    available_qty: 2
  },
  {
    id: 3,
    title: '数据库系统概论',
    author: '王五',
    available_qty: 10
  }
]

export default {
  created(){
    const raw = sessionStorage.getItem("user") || localStorage.getItem("user") || "{}"
    this.user = JSON.parse(raw)
    
    // 如果 user.id 为 null，尝试从 token 中解析
    if (!this.user.id) {
      const token = sessionStorage.getItem("token") || localStorage.getItem("token")
      if (token) {
        try {
          // 解析 JWT token (格式: header.payload.signature)
          const payload = JSON.parse(atob(token.split('.')[1]))
          if (payload.id) {
            this.user.id = payload.id
            // 更新本地存储
            const storageKey = sessionStorage.getItem("user") ? "sessionStorage" : "localStorage"
            if (storageKey === "sessionStorage") {
              sessionStorage.setItem("user", JSON.stringify(this.user))
            } else {
              localStorage.setItem("user", JSON.stringify(this.user))
            }
            console.log("从 token 中恢复用户 ID:", payload.id)
          }
        } catch (e) {
          console.error("解析 token 失败:", e)
        }
      }
    }
    
    // 确保 role 是数字类型，便于比较
    if (this.user.role !== undefined && this.user.role !== null) {
      this.user.role = Number(this.user.role)
    }
    console.log("User data:", this.user)
    console.log("User role:", this.user.role, "Type:", typeof this.user.role)
    this.load()
  },
  name: 'BookView',
  methods: {
  // (this.isbnArray.indexOf(scope.row.isbn)) == -1
    handleSelectionChange(val){
      this.ids = val.map(v =>v.id)
    },
    deleteBatch(){
      if (!this.ids.length) {
        ElMessage.warning("请选择数据！")
        return
      }
      // 本地模拟批量删除
      if (this.useMock) {
        this.ids.forEach(id => {
          const index = mockBooks.findIndex(b => b.id === id)
          if (index !== -1) {
            mockBooks.splice(index, 1)
          }
        })
        ElMessage.success("批量删除成功（本地模拟）")
        this.load()
        return
      }
      // 真实后端批量删除：逐个调用后端 /delById?id=...
      Promise.all(
        this.ids.map(id =>
          request.get("/delById", { params: { id } })
        )
      ).then(() => {
        ElMessage.success("批量删除成功")
        this.load()
      }).catch(() => {
        ElMessage.error("批量删除失败")
      })
    },
    load(){
      // 本地模拟查询，不访问后端
      if (this.useMock) {
        let list = mockBooks.filter(item => {
          const matchId = this.search1 ? String(item.id).includes(this.search1) : true
          const matchTitle = this.search2 ? (item.title || '').includes(this.search2) : true
          const matchAuthor = this.search3 ? (item.author || '').includes(this.search3) : true
          return matchId && matchTitle && matchAuthor
        })
        this.total = list.length
        const start = (this.currentPage - 1) * this.pageSize
        const end = start + this.pageSize
        this.tableData = list.slice(start, end)
        return
      }

      // 构建查询参数
      const params = {
        page: this.currentPage,
        pageSize: this.pageSize,
      }

      // 根据搜索条件选择接口，若无匹配则默认 /books
      let url = "/books"
      if (this.search2 && this.search2.trim()) {
        url = "/findByTitle"
        params.keyword = this.search2.trim()
      } else if (this.search3 && this.search3.trim()) {
        url = "/findByAuthor"
        params.author = this.search3.trim()
      }

      request.get(url, { params }).then(res => {
        let listData = []
        const payload = res.data

        if (Array.isArray(payload)) {
          listData = payload
        } else if (payload && Array.isArray(payload.records)) {
          listData = payload.records
        } else if (payload && Array.isArray(payload.rows)) {
          listData = payload.rows
        } else if (payload) {
          listData = payload
        }

        if (!Array.isArray(listData)) {
          listData = []
        }

        // 规范字段
        const mapped = listData.map(item => ({
          id: item.id,
          title: item.title || item.name || item.bookName || '',
          author: item.author || '',
          available_qty: item.available_qty != null ? item.available_qty : (item.borrownum != null ? item.borrownum : 0),
          borrownum: item.borrownum != null ? item.borrownum : 0,
          status: (typeof item.status === 'string' ? Number(item.status) : (item.status != null ? item.status : 1)),
          isbn: item.isbn || '',
          borrowId: item.borrowId || item.borrow_id || null
        }))
        
        console.log('原始数据示例:', listData[0])
        console.log('映射后数据示例:', mapped[0])

        // 前端过滤，支持编号/标题/作者组合筛选
        const filtered = mapped.filter(item => {
          const matchId = this.search1 ? String(item.id || '').includes(this.search1.trim()) : true
          const matchTitle = this.search2 ? (item.title || '').includes(this.search2.trim()) : true
          const matchAuthor = this.search3 ? (item.author || '').includes(this.search3.trim()) : true
          return matchId && matchTitle && matchAuthor
        })

        this.tableData = filtered
        this.total = (payload && payload.total) ? payload.total : filtered.length
        
        // 如果是普通用户，加载借阅记录
        if (this.user.role === 0 && this.user.id) {
          this.loadBorrowRecords()
        }
      }).catch(err => {
        console.error("查询失败:", err)
        ElMessage.error("查询失败，请稍后重试")
        this.tableData = []
        this.total = 0
      })
    },
    clear(){
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.currentPage = 1
      this.$nextTick(() => {
        this.load()
      })
    },
    
    loadBorrowRecords(){
      // 获取当前用户正在借阅的图书（未归还的）
      request.get("/borrowing/" + this.user.id).then(res => {
        const borrowRecords = res.data || []
        console.log('用户正在借阅的记录:', borrowRecords)
        
        // 将借阅记录匹配到图书列表
        this.tableData = this.tableData.map(book => {
          // 查找这本书的未还借阅记录（status=0）
          const borrowRecord = borrowRecords.find(record => 
            (record.bookId === book.id || record.book_id === book.id) && 
            (record.status === 0 || record.status === '0')
          )
          
          if (borrowRecord) {
            return {
              ...book,
              borrowId: borrowRecord.id || borrowRecord.borrowId,
              hasBorrowed: true
            }
          }
          return book
        })
        
        console.log('匹配后的图书列表:', this.tableData)
      }).catch(err => {
        console.error("获取借阅记录失败:", err)
        // 如果接口返回错误，静默处理
      })
    },

    handleDelete(id){
      // // 本地模拟删除
      // if (this.useMock) {
      //   const index = mockBooks.findIndex(b => b.id === id)
      //   if (index !== -1) {
      //     mockBooks.splice(index, 1)
      //     ElMessage.success("删除成功（本地模拟）")
      //     this.load()
      //   } else {
      //     ElMessage.error("未找到该图书（本地模拟）")
      //   }
      //   return
      // }

      // 真实后端删除 /delById?id=...
      request.get("/delById", { params: { id } }).then(res =>{
        console.log(res)
        if(res.code === 1 || res.code === 0 ){
          ElMessage.success(res.msg || "删除成功")
        }
        else {
          ElMessage.error(res.msg || "删除失败")
        }
        this.load()
      })
    },
    handlereturn(borrowId){
      if(!borrowId){
        ElMessage.warning('缺少借阅记录ID')
        return
      }
      
      console.log('还书参数:', { borrowId: borrowId })
      
      // 调用新的后端还书接口
      request.post("/return", {
        borrowId: Number(borrowId)
      }).then(res => {
        console.log('还书响应:', res)
        if(res.code === 1 || res.code === 0){
          ElMessage.success(res.msg || '还书成功')
          this.$nextTick(() => {
            this.load()
          })
        } else {
          ElMessage.error(res.msg || '还书失败')
        }
      }).catch(err => {
        console.error("还书失败:", err)
        const errorMsg = err.response?.data?.msg || err.message || '还书失败，请稍后重试'
        ElMessage.error(errorMsg)
      })
    },
    handlelend(id){
      if(this.number ==5){
        ElMessage.warning("您不能再借阅更多的书籍了")
        return;
      }
      if(this.numOfOutDataBook !=0){
        ElMessage.warning("在您归还逾期书籍前不能再借阅书籍")
        return;
      }
      
      // 参数验证
      if(!this.user.id){
        ElMessage.error('用户信息异常，请重新登录')
        return;
      }
      if(!id){
        ElMessage.error('图书信息异常')
        return;
      }
      
      console.log('借书参数:', { userId: this.user.id, bookId: id })
      
      // 调用新的后端借书接口
      request.post("/borrow", {
        userId: Number(this.user.id),
        bookId: Number(id)
      }).then(res => {
        console.log('借书响应:', res)
        if(res.code === 1 || res.code === 0){
          ElMessage.success(res.msg || '借阅成功')
          this.$nextTick(() => {
            this.load()
          })
        } else {
          ElMessage.error(res.msg || '借阅失败')
        }
      }).catch(err => {
        console.error("借阅失败:", err)
        const errorMsg = err.response?.data?.msg || err.message || '借阅失败，请稍后重试'
        ElMessage.error(errorMsg)
      })
    },
    add(){
      this.dialogVisible= true
      this.form ={
        available_qty: 0
      }
    },
    save(){
      // 本地模拟上架 / 修改图书
      if (this.useMock) {
        if(this.form.id){
          const index = mockBooks.findIndex(b => b.id === this.form.id)
          if (index !== -1) {
            mockBooks.splice(index, 1, { ...mockBooks[index], ...this.form })
            ElMessage.success('修改书籍信息成功（本地模拟）')
          } else {
            ElMessage.error('未找到该图书（本地模拟）')
          }
        }
        else {
          const maxId = mockBooks.reduce((max, item) => Math.max(max, item.id), 0)
          const newItem = {
            ...this.form,
            id: maxId + 1,
          }
          mockBooks.push(newItem)
          ElMessage.success('上架书籍成功（本地模拟）')
        }
        this.$nextTick(() => {
          this.load()
          this.dialogVisible = false
          this.dialogVisible2 = false
        })
        return
      }

      // 真实后端：根据是否有 id 区分新增/修改
      if(this.form.id){
        // 更新可借阅数量
        request.post("/updateBook",{
          id: this.form.id,
          available_qty: this.form.available_qty
        }).then(res =>{
          console.log(res)
          if(res.code === 1 || res.code === 0){
            ElMessage({
              message: res.msg || '修改书籍信息成功',
              type: 'success',
            })
          }
          else {
            ElMessage.error(res.msg || '修改书籍信息失败')
          }
          this.$nextTick(() => {
            this.load()
            this.dialogVisible2 = false
          })
        })
      }
      else {
        // 新增书籍
        request.post("/addBook",{
          title: this.form.title,
          author: this.form.author,
          available_qty: this.form.available_qty
        }).then(res =>{
          console.log(res)
          if(res.code === 1 || res.code === 0){
            ElMessage.success(res.msg || '上架书籍成功')
          }
          else {
            ElMessage.error(res.msg || '上架书籍失败')
          }
          this.$nextTick(() => {
            this.load()
            this.dialogVisible = false
          })
        })
      }

    },
    // formatter(row) {:formatter="formatter"
    //   return row.address
    // },

    handleEdit(row){
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogVisible2 = true
    },
    handleSizeChange(pageSize){
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum){
      this.currentPage = pageNum
      this.$nextTick(() => {
        this.load()
      })
    },
    toLook(){
      this.dialogVisible3 =true;
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
      pageSize: 5,
      tableData: [],
      user:{},
      number:0,
      bookData:[],
      isbnArray:[],
      outDateBook:[],
      numOfOutDataBook: 0,
      dialogVisible3 : true,
      // 当前是否使用本地模拟图书数据，后端写好后设为 false 即可真实联调
      useMock: false,
    }
  },
}
</script>

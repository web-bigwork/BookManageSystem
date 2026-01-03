<template>
  <div class="home" style="padding: 10px">
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
      <el-table-column prop="userId" label="用户编号" sortable />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="联系电话" />
      <el-table-column prop="overdueBooksCount" label="逾期书籍数量" sortable />
      <el-table-column prop="totalOverdueDays" label="累计逾期天数" sortable />
      <el-table-column label="状态">
        <template v-slot>
          <el-tag type="danger">黑名单</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200px">
        <template v-slot="scope">
          <el-button size="mini" @click="viewDetails(scope.row)">查看详情</el-button>
          <el-popconfirm title="确认移出黑名单?" @confirm="removeFromBlacklist(scope.row)">
            <template #reference>
              <el-button type="warning" size="mini">移出黑名单</el-button>
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
    </div>

    <!-- 详情对话框-->
    <el-dialog v-model="detailDialogVisible" title="逾期详情" width="50%">
      <div v-if="selectedUser">
        <p><strong>用户编号：</strong>{{ selectedUser.userId }}</p>
        <p><strong>用户名：</strong>{{ selectedUser.username }}</p>
        <p><strong>联系电话：</strong>{{ selectedUser.phone }}</p>
        <p><strong>逾期书籍数量：</strong>{{ selectedUser.overdueBooksCount }}</p>
        <p><strong>累计逾期天数：</strong>{{ selectedUser.totalOverdueDays }}</p>
        <p><strong>逾期书籍详情：</strong></p>
        <el-table :data="selectedUser.overdueDetails" size="small" style="margin-top: 10px">
          <el-table-column prop="bookName" label="书籍名称" />
          <el-table-column prop="lendTime" label="借出时间" />
          <el-table-column prop="dueTime" label="应还时间" />
          <el-table-column prop="overdueDays" label="逾期天数" />
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关 闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from "../utils/request";
import {ElMessage} from "element-plus";

export default {
  name: 'BlacklistView',
  created(){
    this.load()
  },
  methods: {
    load(){
      request.get("/blacklist/users", {
        params: {
          page: this.currentPage,
          pageSize: this.pageSize,
          userId: this.search1 || undefined,
          username: this.search2 || undefined
        }
      }).then(res => {
        console.log('黑名单数据:', res)
        if (res.code === 1) {
          const data = res.data || {}
          this.tableData = data.rows || data.records || []
          this.total = data.total || this.tableData.length
        } else {
          ElMessage.error(res.msg || '加载黑名单失败')
          this.tableData = []
          this.total = 0
        }
      }).catch(err => {
        console.error("加载黑名单失败:", err)
        ElMessage.error("加载黑名单失败，请稍后重试")
        this.tableData = []
        this.total = 0
      })
    },
    clear(){
      this.search1 = ""
      this.search2 = ""
      this.load()
    },
    viewDetails(row){
      request.get(`/blacklist/user/${row.userId}/details`).then(res => {
        console.log('用户逾期详情:', res)
        if (res.code === 1) {
          this.selectedUser = {
            ...row,
            overdueDetails: res.data || []
          }
          this.detailDialogVisible = true
        } else {
          ElMessage.error(res.msg || '获取详情失败')
        }
      }).catch(err => {
        console.error("获取详情失败:", err)
        ElMessage.error("获取详情失败，请稍后重试")
      })
    },
    removeFromBlacklist(row){
      request.post("/blacklist/remove", { userId: row.userId }).then(res => {
        console.log('移出黑名单结果:', res)
        if (res.code === 1) {
          ElMessage.success(res.msg || `已将用户 ${row.username} 移出黑名单`)
          this.load()
        } else {
          ElMessage.error(res.msg || '移出黑名单失败')
        }
      }).catch(err => {
        console.error("移出黑名单失败:", err)
        ElMessage.error("移出黑名单失败，请稍后重试")
      })
    },
    handleSizeChange(pageSize){
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum){
      this.currentPage = pageNum
      this.load()
    }
  },
  data() {
    return {
      search1: '',
      search2: '',
      tableData: [],
      detailDialogVisible: false,
      selectedUser: null,
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
}
</script>

<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in cards" :key="item.title">
        <el-card class="box-card">
          <template #header>
            <div class="clearfix">{{ item.title }}</div>
          </template>
          <div class="text item">
            <svg class="icon" aria-hidden="true">
              <use :xlink:href="item.icon" style="width: 100px"></use>
            </svg>
            <span class="text">{{ item.data }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <div id="myTimer" style="margin-left: 15px;font-weight: 550;"></div>
    <!-- 为 ECharts 准备一个具备大小（宽高）的 DOM -->
    <div id="main" style="margin-left: 5px"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {ElMessage} from "element-plus";
import request from "../utils/request";

export default {
  name: "DashboardView",
  data() {
    return {
      cards: [
        { title: '已借阅', data: 100, icon: '#iconlend-record-pro' },
        { title: '总访问', data: 100, icon: '#iconvisit'   },
        { title: '图书数', data: 100, icon: '#iconbook-pro' },
        { title: '用户数', data: 1000, icon: '#iconpopulation' }
      ],
      data:{},
      timer: null
    }
  },
  created() {

  },
  mounted() {
    this.circleTimer()

    // 访客统计：按“用户”计一次；无用户时按浏览器计一次
    // 1) 先获取 userId（优先从 user 对象，其次从 token 恢复）
    let userId
    try {
      const raw = localStorage.getItem('user') || sessionStorage.getItem('user') || '{}'
      const user = JSON.parse(raw)
      userId = user && user.id
    } catch (e) {
      console.warn('读取本地用户信息失败:', e)
    }
    if (!userId) {
      const token = localStorage.getItem('token') || sessionStorage.getItem('token')
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]))
          userId = payload.id || payload.userId || payload.sub
        } catch (e) {
          console.warn('解析 token 失败:', e)
        }
      }
    }

    // 2) 针对已登录用户：每个 userId 只计一次；未登录用户：浏览器只计一次
    const baseKey = 'lib_hasVisited_v1'
    const visitKey = userId ? `${baseKey}_user_${userId}` : baseKey
    const shouldRecord = !localStorage.getItem(visitKey)
    const recordPromise = shouldRecord ? request.get('/visit') : Promise.resolve()

    recordPromise
      .catch((err) => { console.warn('访问记录失败（忽略）：', err) }) // 记录失败不影响后续获取 dashboard
      .finally(() => {
        if (shouldRecord) localStorage.setItem(visitKey, '1')
        
        // 再获取 dashboard 数据（包含 visitCount）
        request.get('/dashboard').then(res => {
          if (res.code == 1) {
            this.cards[0].data = res.data.lendRecordCount
            this.cards[1].data = res.data.visitCount
            this.cards[2].data = res.data.bookCount
            this.cards[3].data = res.data.userCount

            // 基于准备好的dom，初始化echarts实例
            var myChart = echarts.init(document.getElementById('main'))
            console.log(this.cards[0].data)
            
            // 绘制图表
            myChart.setOption({
              title: {
                text: '统计'
              },
              tooltip: {
                trigger: 'axis'
              },
              grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
              },
              xAxis: {
                type: 'category',
                data: this.cards.map(item => item.title),
                axisTick: {
                  alignWithLabel: true
                }
              },
              yAxis: {
                type: 'value'
              },
              series: [
                {
                  type: 'bar',
                  label: { show: true },
                  barWidth: '25%',
                  data: [
                    {
                      value: this.cards[0].data,
                      itemStyle: { color: '#5470c6' }
                    },
                    {
                      value: this.cards[1].data,
                      itemStyle: { color: '#91cc75' }
                    },
                    {
                      value: this.cards[2].data,
                      itemStyle: { color: '#fac858' }
                    },
                    {
                      value: this.cards[3].data,
                      itemStyle: { color: '#ee6666' }
                    }
                  ]
                }
              ]
            })
            
            window.addEventListener('resize', () => {
              myChart.resize()
            })
          } else {
            ElMessage.error(res.msg || '获取统计失败')
          }
        }).catch(err => {
          console.error('获取统计失败:', err)
          ElMessage.error('获取统计失败')
        })
      })
  },
  beforeUnmount() {
    // 组件卸载时清除定时器，避免访问已销毁的 DOM
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
  methods: {
    circleTimer() {
      this.getTimer()
      this.timer = setInterval(() => {
        this.getTimer()
      }, 1000)
    },
    getTimer() {
      var d = new Date()
      var t = d.toLocaleString()
      const el = document.getElementById('myTimer')
      if (el) {
        el.innerHTML = t
      }
    }
  }
}
</script>

<style scoped>
.box-card {
   width: 80%;
  margin-bottom: 25px;
  margin-left: 10px;
}

.clearfix {
  text-align: center;
  font-size: 15px;
}

.text {
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  vertical-align: super;
}

#main {
  width: 100%;
  height: 450px;
  margin-top: 20px;
}

.icon {
  width: 50px;
  height: 50px;
  padding-top: 5px;
  padding-right: 10px;
}
</style>
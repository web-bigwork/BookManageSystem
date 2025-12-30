const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: (error) => {
          // 忽略 ResizeObserver 相关错误
          const ignoreErrors = [
            'ResizeObserver loop completed with undelivered notifications',
            'ResizeObserver loop limit exceeded'
          ]
          return !ignoreErrors.some(msg => error.message.includes(msg))
        }
      }
    },
    proxy: {
      // 前端 axios 里所有以 /api 开头的请求，都会被转发到后端 Spring Boot
      '/api': {
        target: 'http://localhost:8080', // TODO: 如果后端端口不是 8080，在这里改
        changeOrigin: true,
        pathRewrite: {
          '^/api': '' // 去掉前缀 /api，让后端直接收到 /login、/books 等路径
        },
        onProxyRes: function(proxyRes, req, res) {
          // 确保代理响应能够正确处理，防止CORS问题
          proxyRes.headers['Access-Control-Allow-Credentials'] = 'true'
        }
      }
    }
  }
})

module.exports = {
  devServer: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      },
      '/chatbot': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': require('path').resolve(__dirname, 'src')
      }
    }
  }
}

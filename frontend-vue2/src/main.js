import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import 'font-awesome/css/font-awesome.min.css'
import './style.css'
import App from './App.vue'

Vue.use(ElementUI)

new Vue({
  render: function (h) { return h(App) }
}).$mount('#app')

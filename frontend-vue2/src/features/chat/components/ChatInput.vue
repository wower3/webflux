<template>
  <div class="input-container">
    <div class="input-box">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="给大模型发送消息..."
        @keydown.enter.exact.native="handleSend"
        :disabled="loading"
        resize="none"
      />
      <el-button
        type="success"
        circle
        @click="handleSend"
        :loading="loading"
        :disabled="!inputText.trim()"
        class="send-btn"
      >
        <i class="fa fa-paper-plane" style="color: white;"></i>
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatInput',
  props: {
    loading: { type: Boolean, default: false }
  },
  data: function () {
    return {
      inputText: ''
    }
  },
  methods: {
    handleSend: function () {
      var message = this.inputText.trim()
      if (!message || this.loading) return

      this.$emit('send', message)
      this.inputText = ''
    }
  }
}
</script>

<style scoped>
.input-container {
  border-top: 1px solid #e5e7eb;
  background-color: #ffffff;
  padding: 16px 24px;
  display: flex;
  justify-content: center;
}

.input-box {
  max-width: 100%;
  width: 100%;
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 10px 16px;
  background: #fff;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.05);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-box:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15), 0 0 15px rgba(0, 0, 0, 0.05);
}

.input-box ::v-deep .el-textarea {
  flex: 1;
}

.input-box ::v-deep .el-textarea__inner {
  border: none;
  box-shadow: none;
  resize: none;
  padding: 0;
  font-size: 15px;
  font-family: inherit;
}

.send-btn {
  margin-left: 10px;
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  min-height: 32px;
}
</style>

<template>
  <div class="phone">
    <div class="toast" :class="[toastType, { show: showToast }]">
      {{ toastMsg }}
    </div>

    <div class="left-side">
      <div class="big-logo">CC</div>
      <h1>CC 打车</h1>
      <p>手机号快捷登录</p>
    </div>

    <div class="right-side">
      <div class="form-box">
        <h2>验证码登录</h2>

        <div class="input-wrap">
          <span class="label" :class="{ active: phone }">手机号</span>
          <input
            type="text"
            placeholder="请输入手机号"
            v-model="phone"
            :class="{ error: isPhoneError }"
            @focus="phoneFocus"
            maxlength="11"
          />
          <span v-if="isPhoneError" class="error-text">请输入正确的11位手机号</span>
        </div>

        <div class="input-wrap code-row">
          <span class="label" :class="{ active: code }">验证码</span>
          <input
            type="text"
            placeholder="请输入验证码"
            v-model="code"
            :class="{ error: isCodeError }"
            @focus="codeFocus"
            maxlength="6"
          />
          <button class="code-btn" @click="getCode" :disabled="countDown>0">
            {{ countDown > 0 ? `${countDown}秒` : '获取验证码' }}
          </button>
          <span v-if="isCodeError" class="error-text">验证码为6位数字</span>
        </div>

        <button class="submit" @click="phoneLogin">登录</button>

        <div class="links">
          <router-link to="/login">返回账号登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const phoneRegex = /^1[3-9]\d{9}$/
const codeRegex = /^\d{6}$/

const isPhoneError = ref(false)
const isCodeError = ref(false)
const phone = ref('')
const code = ref('')
const countDown = ref(0)
let timer: number | null = null

// 提示
const toastMsg = ref('')
const toastType = ref<'success' | 'error'>('success')
const showToast = ref(false)

function showTip(msg: string, type: 'success' | 'error') {
  toastMsg.value = msg
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 1000)
}

function phoneFocus() {
  isPhoneError.value = false
}
function codeFocus() {
  isCodeError.value = false
}

async function getCode() {
  if (!phoneRegex.test(phone.value)) {
    isPhoneError.value = true
    showTip('手机号格式错误', 'error')
    return
  }
  countDown.value = 60
  timer = window.setInterval(() => {
    countDown.value--
    if (countDown.value <= 0) clearInterval(timer!)
  }, 1000)
  try {
    await axios.get("http://localhost:8080/login/phone/code", {
      params: {
        phone: phone.value,
        type: 'login'
      }
    })
    showTip('验证码发送成功', 'success')
  } catch (e) {
    showTip('发送失败', 'error')
  }
}

async function phoneLogin() {
  if (!phoneRegex.test(phone.value)) {
    isPhoneError.value = true
    showTip('手机号错误', 'error')
    return
  }
  if (!codeRegex.test(code.value)) {
    isCodeError.value = true
    showTip('验证码错误', 'error')
    return
  }
  try {
    const res = await axios.post('http://localhost:8080/login/phone', {
      phone: phone.value,
      code: code.value,
    })
    const data = res.data
    if (data.code === 1) {
      localStorage.setItem('token', data.data)
      showTip('登录成功', 'success')
      setTimeout(() => router.replace('/admin'), 1000)
    } else {
      showTip(data.msg, 'error')
    }
  } catch (e) {
    showTip('登录异常', 'error')
  }
}
</script>
<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
.phone {
  width: 100vw;
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  overflow: hidden;
}
.left-side {
  width: 40%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  gap: 12px;
  box-shadow: 2px 0 10px rgba(0,0,0,0.05);
}
.big-logo {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: #32d699;
  font-size: 72px;
  font-weight: bold;
  color: #fff;
  display: grid;
  place-items: center;
  transition: all 0.3s ease;
}
.left-side h1 {
  font-size: 44px;
  color: #222;
  font-weight: 600;
}
.left-side p {
  color: #666;
  font-size: 16px;
  letter-spacing: 1px;
}
.right-side {
  width: 60%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}
.form-box {
  width: 380px;
}
.form-box h2 {
  font-size: 32px;
  color: #222;
  margin-bottom: 36px;
  font-weight: 600;
}
.input-wrap {
  position: relative;
  margin-bottom: 24px;
}
.code-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.input-wrap .label {
  position: absolute;
  left: 22px;
  top: 50%;
  transform: translateY(-50%);
  color: #32d699;
  font-size: 15px;
  transition: all 0.25s ease;
  pointer-events: none;
  z-index: 1;
}
.input-wrap .label.active {
  top: 0;
  left: 18px;
  font-size: 12px;
  background: #f5f7fa;
  padding: 0 6px;
  border-radius: 4px;
}
.input-wrap input {
  width: 100%;
  height: 54px;
  background: #ffffff;
  border-radius: 12px;
  padding: 0 20px 0 80px;
  font-size: 16px;
  color: #333;
  border: 1px solid #eee;
  outline: none;
  transition: all 0.25s ease;
}
.input-wrap input:focus {
  box-shadow: 0 0 0 2px #32d699 !important;
  border-color: #32d699;
}
.input-wrap input.error {
  box-shadow: 0 0 0 2px #f56c6c !important;
  border-color: #f56c6c;
}
.error-text {
  position: absolute;
  bottom: -18px;
  left: 4px;
  font-size: 12px;
  color: #f56c6c;
}
.code-btn {
  width: 120px;
  height: 54px;
  background: #32d699;
  color: #fff;
  font-weight: bold;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  flex-shrink: 0;
}
.code-btn:hover:not(:disabled) {
  background: #2ac98d;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(50, 214, 153, 0.2);
}
.code-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  color: #999;
}
.submit {
  width: 100%;
  height: 54px;
  background: #32d699;
  color: #fff;
  font-size: 17px;
  font-weight: bold;
  border: none;
  border-radius: 12px;
  margin-top: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
}
.submit:hover {
  background: #2ac98d;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(50, 214, 153, 0.25);
}
.submit:active {
  transform: translateY(0);
}
.links {
  margin-top: 28px;
  text-align: center;
}
.links a {
  color: #32d699;
  text-decoration: none;
  font-size: 15px;
  transition: color 0.2s;
}
.links a:hover {
  color: #2ac98d;
}

/* 提示框 */
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  z-index: 9999;
  opacity: 0;
  transition: all 0.3s ease;
}
.toast.show {
  opacity: 1;
  top: 30px;
}
.toast.success {
  background: #32d699;
}
.toast.error {
  background: #f56c6c;
}
</style>
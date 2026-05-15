<template>
  <div class="register">
    <div class="toast" :class="[toastType, { show: showToast }]">
      {{ toastMsg }}
    </div>

    <div class="left-side">
      <div class="big-logo">CC</div>
      <h1>CC 打车</h1>
      <p>欢迎加入我们</p>
    </div>

    <div class="right-side">
      <div class="form-box">
        <h2>注册账号</h2>

        <div class="input-wrap">
          <span class="label" :class="{ active: username }">用户名</span>
          <input
            type="text"
            placeholder="请输入用户名"
            v-model="username"
            :class="{ error: isUsernameError }"
            @focus="usernameFocus"
            maxlength="12"
          />
          <span v-if="isUsernameError" class="error-text">用户名6-12位字母/数字</span>
        </div>

        <div class="input-wrap">
          <span class="label" :class="{ active: password }">密码</span>
          <input
            type="password"
            placeholder="请输入密码"
            v-model="password"
            :class="{ error: isPasswordError }"
            @focus="passwordFocus"
            maxlength="12"
          />
          <span v-if="isPasswordError" class="error-text">密码6-12位数字</span>
        </div>

        <button class="submit" @click="register">注册</button>

        <div class="links">
          <router-link to="/login">已有账号 → 登录</router-link>
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
const usernameRegex = /^[a-zA-Z0-9]{6,12}$/
const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,12}$/

const isUsernameError = ref(false)
const isPasswordError = ref(false)
const username = ref('')
const password = ref('')

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

function usernameFocus() {
  isUsernameError.value = false
}
function passwordFocus() {
  isPasswordError.value = false
}

async function register() {
  if (!username.value || !usernameRegex.test(username.value)) {
    isUsernameError.value = true
    showTip('用户名格式错误', 'error')
    return
  }
  if (!password.value || !passwordRegex.test(password.value)) {
    isPasswordError.value = true
    showTip('密码格式错误', 'error')
    return
  }
  try {
    const res = await axios.post('http://localhost:8080/login/register', {
      username: username.value,
      password: password.value,
    })
    const data = res.data
    if (data.code === 1) {
      showTip('注册成功', 'success')
      setTimeout(() => router.push('/login'), 1000)
    } else {
      showTip(data.msg, 'error')
    }
  } catch (e) {
    showTip('注册异常', 'error')
  }
}
</script>
<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
.register {
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
<template>
  <div class="pwd-page">

    <!-- toast -->
    <div
      class="toast"
      :class="[toastType, { show: showToast }]"
    >
      {{ toastMsg }}
    </div>

    <div class="form-box">
      <h2>修改密码</h2>

      <p class="desc">
        通过手机号验证码验证身份
      </p>

      <!-- 手机号 -->
      <div class="input-wrap">
        <span class="label active">
          手机号
        </span>

        <input
          type="text"
          :value="maskPhone(phone)"
          disabled
          class="disabled-input"
        />
      </div>

      <!-- 验证码 -->
      <div class="input-wrap code-row">
        <span
          class="label"
          :class="{ active: code }"
        >
          验证码
        </span>

        <input
          type="text"
          placeholder="请输入验证码"
          v-model="code"
          maxlength="6"
          :class="{ error: codeError }"
          @focus="codeError = false"
        />

        <button
          class="code-btn"
          @click="sendCode"
          :disabled="countDown > 0"
        >
          {{
            countDown > 0
              ? `${countDown}秒`
              : '获取验证码'
          }}
        </button>

        <span
          v-if="codeError"
          class="error-text"
        >
          验证码格式错误
        </span>
      </div>

      <!-- 新密码 -->
      <div class="input-wrap">
        <span
          class="label"
          :class="{ active: newPassword }"
        >
          新密码
        </span>

        <input
          type="password"
          placeholder="6-12位数字字母组合"
          v-model="newPassword"
          maxlength="12"
          :class="{ error: pwdError }"
          @focus="pwdError = false"
        />

        <span
          v-if="pwdError"
          class="error-text"
        >
          密码必须为6-12位数字字母组合
        </span>
      </div>

      <!-- 强度条 -->
      <div class="strength-wrapper">
        <div class="bars">
          <div
            class="bar"
            :class="{
              active: strengthLevel >= 1,
              weak: strengthLevel === 1,
              middle: strengthLevel === 2,
              strong: strengthLevel === 3,
            }"
          ></div>

          <div
            class="bar"
            :class="{
              active: strengthLevel >= 2,
              middle: strengthLevel === 2,
              strong: strengthLevel === 3,
            }"
          ></div>

          <div
            class="bar"
            :class="{
              active: strengthLevel >= 3,
              strong: strengthLevel === 3,
            }"
          ></div>
        </div>

        <span
          class="strength-text"
          :class="strengthClass"
        >
          {{ strengthText }}
        </span>
      </div>

      <!-- 提交 -->
      <button
        class="submit"
        @click="submit"
      >
        确认修改
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { jwtDecode } from 'jwt-decode'
import request from '../../utils/request'
// ====================
// token解析
// ====================

interface UserInfo {
  phone: string
}

function getUserInfo(): UserInfo | null {
  const token =
    localStorage.getItem('token')

  if (!token) return null

  try {
    return jwtDecode(token)
  } catch {
    return null
  }
}

const user = getUserInfo()

const phone = user?.phone || ''

function maskPhone(phone: string) {
  return phone.replace(
    /^(\d{3})\d{4}(\d{4})$/,
    '$1****$2'
  )
}

// ====================
// 数据
// ====================

const code = ref('')
const newPassword = ref('')

const codeError = ref(false)
const pwdError = ref(false)

const countDown = ref(0)

// ====================
// toast
// ====================

const toastMsg = ref('')
const toastType = ref<
  'success' | 'error'
>('success')

const showToast = ref(false)

function showTip(
  msg: string,
  type: 'success' | 'error'
) {
  toastMsg.value = msg
  toastType.value = type
  showToast.value = true

  setTimeout(() => {
    showToast.value = false
  }, 1200)
}

// ====================
// 密码正则
// ====================

const pwdRegex =/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,12}$/

const codeRegex = /^\d{6}$/

// ====================
// 强度
// ====================

const strengthLevel = computed(() => {
  const pwd = newPassword.value

  if (!pwd) return 0

  if (
    /^[A-Za-z]+$/.test(pwd) ||
    /^\d+$/.test(pwd)
  ) {
    return 1
  }

  if (pwdRegex.test(pwd)) {
    return 3
  }

  return 2
})

const strengthText = computed(() => {
  if (strengthLevel.value === 1)
    return '弱'

  if (strengthLevel.value === 2)
    return '中'

  if (strengthLevel.value === 3)
    return '强'

  return ''
})

const strengthClass = computed(() => {
  if (strengthLevel.value === 1)
    return 'weak-text'

  if (strengthLevel.value === 2)
    return 'middle-text'

  if (strengthLevel.value === 3)
    return 'strong-text'

  return ''
})

// ====================
// 获取验证码
// ====================

async function sendCode() {
  try {
    const token =
      localStorage.getItem('token')

    await request.get("/login/phone/code",{
      params:
      {
        phone: phone,
        type: 'modify'
      }
    },
      {
        headers: {
          token: token,
        },
      }
    )

    showTip(
      '验证码发送成功',
      'success'
    )

    countDown.value = 60

    const timer = setInterval(() => {
      countDown.value--

      if (countDown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch {
    showTip('发送失败', 'error')
  }
}

// ====================
// 提交
// ====================

async function submit() {
  codeError.value = false
  pwdError.value = false

  if (!codeRegex.test(code.value)) {
    codeError.value = true
    return
  }

  if (
    !pwdRegex.test(newPassword.value)
  ) {
    pwdError.value = true
    return
  }

  try {
    const token =
      localStorage.getItem('token')

    await request.post('/login/phone/password',
      {
        phone: phone,
        code: code.value,
        password:newPassword.value,
      },
      {
        headers: {
          token: token,
        },
      }
    )

    showTip(
      '密码修改成功',
      'success'
    )

    localStorage.removeItem('token')

    setTimeout(() => {
      location.href = '/login'
    }, 1000)
  } catch {
    showTip('修改失败', 'error')
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.pwd-page {
  width: 100%;
  min-height: calc(93vh - 70px);
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 40px;
  overflow: hidden;
}

.form-box {
  width: 520px;
  margin-top: 10px;
  background: #fff;
  padding: 32px;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.form-box h2 {
  font-size: 32px;
  color: #222;
  margin-bottom: 10px;
  font-weight: 600;
}

.desc {
  color: #999;
  margin-bottom: 36px;
  font-size: 14px;
}

.input-wrap {
  position: relative;
  margin-bottom: 26px;
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
  background: #fff;
  border-radius: 12px;
  padding: 0 20px 0 80px;
  font-size: 16px;
  color: #333;
  border: 1px solid #eee;
  outline: none;
  transition: all 0.25s ease;
}

.input-wrap input:focus {
  box-shadow: 0 0 0 2px #32d699;
  border-color: #32d699;
}

.input-wrap input.error {
  box-shadow: 0 0 0 2px #f56c6c;
  border-color: #f56c6c;
}

.disabled-input {
  color: #999;
  cursor: not-allowed;
}

.code-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-btn {
  width: 120px;
  height: 54px;
  border: none;
  border-radius: 12px;
  background: #32d699;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.code-btn:hover:not(:disabled) {
  background: #2ac98d;
}

.code-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.submit {
  width: 100%;
  height: 54px;
  background: #32d699;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.25s ease;
}

.submit:hover {
  background: #2ac98d;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(50, 214, 153, 0.25);
}

.error-text {
  position: absolute;
  bottom: -18px;
  left: 4px;
  font-size: 12px;
  color: #f56c6c;
}

/* 强度条 */

.strength-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.bars {
  display: flex;
  gap: 6px;
}

.bar {
  width: 50px;
  height: 8px;
  border-radius: 4px;
  background: #e5e7eb;
  transition: all 0.3s;
}

.bar.active.weak {
  background: #f56c6c;
}

.bar.active.middle {
  background: #e6a23c;
}

.bar.active.strong {
  background: #67c23a;
}

.strength-text {
  font-size: 13px;
  font-weight: 600;
}

.weak-text {
  color: #f56c6c;
}

.middle-text {
  color: #e6a23c;
}

.strong-text {
  color: #67c23a;
}

/* toast */

.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  opacity: 0;
  transition: all 0.3s ease;
  z-index: 9999;
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
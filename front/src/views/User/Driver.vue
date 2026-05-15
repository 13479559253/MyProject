<template>
  <div class="driver-container">
    <!-- 顶部导航 -->
    <header class="user-header">
      <div class="header-left">
        <div class="logo-icon">CC</div>
        <span class="logo-text">CC打车</span>
      </div>
      <div class="header-right">
        <div class="user-name-wrapper" @click="toggleDropdown" ref="userNameRef">
          <span class="user-name">{{ userName }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          <div v-if="showDropdown" class="dropdown-menu">
            <div class="dropdown-item" @click="handleProfile">修改信息</div>
            <div class="dropdown-item" @click="logout">退出登录</div>
          </div>
        </div>
      </div>
    </header>

    <!-- 状态条 -->
    <div class="status-bar">
      <div class="status-left">
        <div class="status-dot" :class="{ online: isOnline }"></div>
        <span class="status-text">{{ isOnline ? '在线' : '离线' }}</span>
      </div>
      <button class="status-btn" :class="{ online: isOnline, disabled: currentOrder }" :disabled="currentOrder" @click="toggleStatus">
        {{ isOnline ? '下线' : '上线' }}
        <span v-if="currentOrder" class="disabled-tip">进行中</span>
      </button>
    </div>

    <!-- 主内容区 -->
    <main class="driver-main">
      <!-- 左侧区域 60% -->
      <div class="main-left">
        <div class="content">
          <h2>司机端页面</h2>
          <p>司机端功能开发中...</p>
        </div>
      </div>
      
      <!-- 右侧订单展示区域 40% -->
      <div class="main-right">
        <!-- 正在进行的订单 -->
        <template v-if="currentOrder">
          <h3>当前订单</h3>
          <div class="current-order">
            <div class="order-info">
              <div class="location">
                <span class="label">用户:</span>
                <span class="value">{{ currentOrder.userInfo?.name }}</span>
              </div>
              <div class="location">
                <span class="label">电话:</span>
                <span class="value">{{ currentOrder.userInfo?.phone }}</span>
              </div>
              <div class="location">
                <span class="label">起点:</span>
                <span class="value">{{ currentOrder.startAddress }}</span>
              </div>
              <div class="location">
                <span class="label">终点:</span>
                <span class="value">{{ currentOrder.endAddress }}</span>
              </div>
            </div>
            <button class="complete-btn" @click="completeOrder">完成订单</button>
          </div>
        </template>
        
        <!-- 新订单列表 -->
        <template v-else>
          <h3>新订单列表</h3>
          <div class="order-list">
            <div v-if="orders.length === 0" class="empty-order">
              暂无新订单
            </div>
            <div v-for="order in orders" :key="order.id" class="order-item">
              <div class="order-info">
                <div class="location">
                  <span class="label">起点:</span>
                  <span class="value">{{ order.startAddress }}</span>
                </div>
                <div class="location">
                  <span class="label">终点:</span>
                  <span class="value">{{ order.endAddress }}</span>
                </div>
              </div>
              <button class="accept-btn" @click="acceptOrder(order)">接单</button>
            </div>
          </div>
        </template>
      </div>
    </main>
  </div>
</template>

<script lang="ts" setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const router = useRouter()
const userName = ref('司机')
const isOnline = ref(false)
const ws = ref(null)
const showDropdown = ref(false)
const userNameRef = ref(null)

// 订单列表数据
const orders = ref([])
// 当前正在进行的订单
const currentOrder = ref(null)

// 点击外部关闭下拉菜单
if (typeof window !== 'undefined') {
  window.addEventListener('click', (e) => {
    if (userNameRef.value && !userNameRef.value.contains(e.target as Node)) {
      showDropdown.value = false
    }
  })
}

function getUserInfo() {
  const token = localStorage.getItem('token')
  if (!token) return null
  
  try {
    return jwtDecode(token) as { name: string }
  } catch (e) {
    return null
  }
}

function logout() {
  localStorage.removeItem('token')
  router.replace('/login')
}

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

function handleProfile() {
  console.log('修改信息')
  showDropdown.value = false
}

function toggleStatus() {
  isOnline.value = !isOnline.value
  if(isOnline.value){
    connectWebSocket()
  } else { 
    if(ws.value){
      orders.value = []
      ws.value.close(1000, 'User logged out')
      ws.value = null
    }
  }
}

function getPosition(){
  return new Promise((resolve, reject) => {
    if(!navigator.geolocation){
      reject('浏览器不支持定位')
      return
    }
    navigator.geolocation.getCurrentPosition((position) => {
      resolve(position)
    }, (error) => {
      reject('定位失败')
    })
  })
}
function connectWebSocket() {
  const token = localStorage.getItem('token')
  const url = `ws://localhost:8080/function?token=${token}`
  ws.value = new WebSocket(url)
  ws.value.onopen = () => {
    
  }
  ws.value.onmessage = (event) => {
    const json = JSON.parse(event.data)
    if (json.type === 'order-new') {
      // 避免重复添加相同订单
      const newOrder = json.data
      const exists = orders.value.find(o => o.id === newOrder.orderId)
      if (!exists) {
        orders.value.push({
          id: newOrder.orderId,  
          startAddress: newOrder.startAddress,
          endAddress: newOrder.endAddress,
          startLongitude: newOrder.startLongitude,
          startLatitude: newOrder.startLatitude,
          endLongitude: newOrder.endLongitude,
          endLatitude: newOrder.endLatitude,
          userInfo: newOrder.userInfo
        })
      }
    } 
    else if (json.type === 'order-receive') {
      const receivedOrderId = json.data
      orders.value = orders.value.filter(o => o.id !== receivedOrderId)
    }
    else if(json.type === 'order-cancel')  {
      ElMessage.warning('订单已被用户取消')
      if (positionSendTimer) {
        clearInterval(positionSendTimer)
        positionSendTimer = null
      }
      currentOrder.value = null
    }
  }
  
  ws.value.onerror = (error) => {
    console.error('Driver WebSocket error:', error)
    isOnline.value = false
  }
  
  ws.value.onclose = (event) => {
    console.log('Driver WebSocket closed:', event.code, event.reason)
    if (event.code !== 1000 && isOnline.value) {
      setTimeout(() => connectWebSocket(), 3000)
    }
  }
}

let positionSendTimer = null

function sendPosition(userId: number){
  positionSendTimer = setInterval(async () => {
    const position = await getPosition()
    ws.value?.send(JSON.stringify({
      type: 'position-send',
      data: {
        longitude: position.coords.longitude,
        latitude: position.coords.latitude,
        id:userId
      }
    }))
  },5000)
}

async function acceptOrder(order) {
  try{
    const {data,code,msg} = await request.get("/order/receive",{
      params: {
        orderId: order.id
      }
    })
    if(code === 1){
      ElMessage.success('接单成功')
      orders.value = orders.value.filter(o => o.id !== order.id)
      // 保存当前订单
      currentOrder.value = order
      sendPosition(data)
    } else {
      ElMessage.error(msg || '接单失败')
    }
  } catch (error) {
    ElMessage.error('接单失败',error)
  }
}

async function completeOrder() {
  if (!currentOrder.value) return
  
  try {
    const {data, code, msg} = await request.get("/order/complete", {
      params: {
        orderId: currentOrder.value.id
      }
    })
    if (code === 1) {
      ElMessage.success('订单完成')
      clearInterval(positionSendTimer)
      currentOrder.value = null
    } else {
      ElMessage.error(msg || '完成失败')
    }
  } catch (error) {
    console.error('完成订单失败:', error)
    ElMessage.error('完成订单失败')
  }
}

const user = getUserInfo()
if (user) {
  userName.value = user.name || '司机'
}

onUnmounted(() => {
  clearInterval(positionSendTimer)
  if (ws.value) {
    ws.value.close(1000, 'Component unmounted')
    ws.value = null
  }
})
</script>

<style scoped>
.driver-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;
}

.user-header {
  height: 70px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #32d699;
  color: #fff;
  font-size: 22px;
  font-weight: bold;
  display: grid;
  place-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #222;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-name-wrapper:hover {
  background-color: #f5f5f7;
}

.user-name {
  font-size: 15px;
  color: #333;
  margin-right: 4px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  padding: 8px 0;
  min-width: 120px;
  z-index: 1000;
}

.dropdown-item {
  padding: 10px 16px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background-color: #f5f5f7;
}

.dropdown-item:first-child {
  border-radius: 8px 8px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 8px 8px;
}

.status-bar {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
}

.status-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ccc;
}

.status-dot.online {
  background: #32d699;
  box-shadow: 0 0 8px rgba(50, 214, 153, 0.6);
}

.status-text {
  font-size: 14px;
  color: #666;
}

.status-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}

.status-btn:hover {
  border-color: #32d699;
  color: #32d699;
}

.status-btn.online {
  background: #fff3f3;
  border-color: #ff6b6b;
  color: #ff6b6b;
}

.status-btn.online:hover {
  background: #ffe5e5;
}

.status-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.status-btn.disabled:hover {
  border-color: #ddd;
  color: #666;
  background: #fff;
}

.disabled-tip {
  font-size: 12px;
  margin-left: 5px;
  color: #999;
}

.driver-main {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
}

.main-left {
  flex: 0 0 60%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
  align-items: center;
}

.content {
  text-align: center;
  color: #666;
}

.content h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 16px;
}

.content p {
  font-size: 14px;
}

.main-right {
  flex: 0 0 40%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.main-right h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.order-list {
  flex: 1;
  overflow-y: auto;
}

.empty-order {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  background: #f8f9fa;
  border-radius: 8px;
}

.order-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.order-id {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
}

.order-info {
  margin-bottom: 12px;
}

.location {
  display: flex;
  margin-bottom: 6px;
}

.location .label {
  font-size: 12px;
  color: #999;
  width: 36px;
}

.location .value {
  font-size: 13px;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.accept-btn {
  width: 100%;
  padding: 8px;
  background: #32d699;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  color: #fff;
  font-weight: 500;
  cursor: pointer;
}

.current-order {
  background: linear-gradient(135deg, #32d699 0%, #28c78a 100%);
  border-radius: 12px;
  padding: 20px;
  color: #fff;
}

.current-order .order-id {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 12px;
}

.current-order .order-info {
  margin-bottom: 16px;
}

.current-order .location .label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  width: 36px;
}

.current-order .location .value {
  font-size: 14px;
  color: #fff;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.complete-btn {
  width: 100%;
  padding: 10px;
  background: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  color: #32d699;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.complete-btn:hover {
  background: #f0fff9;
}

.accept-btn:hover {
  background: #28b88a;
}
</style>

<template>
  <div class="user-container">
    <!-- 顶部导航 -->
    <header class="user-header">
      <div class="header-left">
        <div class="logo-icon">CC</div>
        <span class="logo-text">CC打车</span>
      </div>
      <div class="header-right">
        <div class="user-name-wrapper" @click="toggleDropdown" ref="userNameRef">
          <span class="user-name">{{ userName }}</span>
          <div v-if="showDropdown" class="dropdown-menu">
            <div class="dropdown-item" @click="handleProfile">修改信息</div>
            <div class="dropdown-item" @click="logout">退出登录</div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="user-main">
      <!-- 地图区域70% -->
      <div class="map-section">
        <div id="map-placeholder"></div>
        <!-- 选择提示 -->
        <div v-if="selectMode" class="map-tip">
          <span>{{ selectMode === 'start' ? '请点击地图选择起点位置' : '请点击地图选择终点位置' }}</span>
          <button @click="cancelSelect" class="cancel-btn">取消</button>
        </div>
      </div>

      <!-- 右侧面板30% -->
      <div class="panel-section">
        <div class="panel-card">
          <h3 class="panel-title">设置行程</h3>
          
          <!-- 起点 -->
          <div class="location-item">
            <div class="location-label">起点</div>
            <div class="location-value">{{ startAddress || '未设置' }}</div>
            <button 
              class="select-btn" 
              :class="{ disabled: orderSubmitted }"
              @click="startSelect('start')"
              :disabled="orderSubmitted"
            >选择起点</button>
          </div>

          <!-- 终点 -->
          <div class="location-item">
            <div class="location-label">终点</div>
            <div class="location-value">{{ endAddress || '未设置' }}</div>
            <button 
              class="select-btn" 
              :class="{ disabled: orderSubmitted }"
              @click="startSelect('end')"
              :disabled="orderSubmitted"
            >选择终点</button>
          </div>

          <!-- 发送订单按钮 -->
          <button 
            v-if="!orderSubmitted"
            class="submit-btn" 
            @click="submitOrder"
            :disabled="orderSubmitted"
          >发送订单</button>

          <!-- 接单状态 -->
          <div v-if="orderSubmitted" class="status-section">
            <div class="status-header">
              <span class="status-title">订单状态</span>
              <span class="status-badge">{{ orderStatusText }}</span>
            </div>
            <div class="status-actions">
              <button class="cancel-btn" @click="cancelOrder">取消订单</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import AMapLoader from '@amap/amap-jsapi-loader'

const router = useRouter()
let map = null
let startMarker = null
let endMarker = null
let driverMarker = null


const userName = ref('用户')
const startLngLat = ref<[number, number] | null>(null)
const endLngLat = ref<[number, number] | null>(null)
const startAddress = ref('')
const endAddress = ref('')
const selectMode = ref<'start' | 'end' | null>(null)
const showDropdown = ref(false)
const userNameRef = ref(null)

const userInfo = ref(null)

let orderTimer = null
const orderId = ref(-1)

// 订单状态
const orderSubmitted = ref(false)
const orderStatus = ref<'waiting' | 'accepted' | 'completed'>('waiting')

const ws = ref(null)
const orderStatusText = ref('')

onMounted(() => {
    window._AMapSecurityConfig = {
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_JS_CODE  
    }
    getUserInfo()
    // 点击外部关闭下拉菜单
    document.addEventListener('click', (e) => {
      if (userNameRef.value && !userNameRef.value.contains(e.target)) {
        showDropdown.value = false
      }
    })
    
    navigator.geolocation.getCurrentPosition((position) => {
      const { longitude, latitude } = position.coords
      startLngLat.value = [longitude, latitude]
      initMap()
    }, () => {
      startLngLat.value = [116.397428, 39.90923]
      initMap()
    })
    const token = localStorage.getItem('token')
    const url = `ws://localhost:8080/function?token=${token}`
    ws.value = new WebSocket(url) 
    ws.value.onmessage = (event) => {
      const json = JSON.parse(event.data)
      if(json.type === 'order-receive'){
        orderStatus.value = 'accepted'
        orderStatusText.value = '司机已接单，正在赶来...'
        clearInterval(orderTimer)
      } 
      else if(json.type === 'position'){
        // 更新司机位置
        const { longitude, latitude } = json.data
        updateDriverPosition(longitude, latitude)
      }
      else if(json.type === 'order-complete'){
        console.log("用户接收到订单完成消息")
        orderStatus.value = 'completed'
        orderStatusText.value = '司机已完成订单'
      }
    }
})

async function initMap(){
    const AMap = await AMapLoader.load({
        key: import.meta.env.VITE_AMAP_KEY,
        version: '2.0',
        plugins: ['AMap.Geocoder']
    })
    map = new AMap.Map('map-placeholder', {
        center: [startLngLat.value![0], startLngLat.value![1]],
        zoom: 17
    })
    startMarker = new AMap.Marker({
      position: [startLngLat.value![0], startLngLat.value![1]],
      title: "起点",
    })
    getAddress("start")
    map.add(startMarker)
    
    map.on('click', (e) => {
      if (!selectMode.value) return
      const lng = e.lnglat.getLng()
      const lat = e.lnglat.getLat()
      
      if (selectMode.value === 'start') {
        startLngLat.value = [lng, lat]
        if (startMarker) {
          map.remove(startMarker)
        }
        startMarker = new AMap.Marker({
          position: [lng, lat],
          title: "起点",
        })
        map.add(startMarker)
        getAddress("start")
      } else {
        endLngLat.value = [lng, lat]
        if (endMarker) {
          map.remove(endMarker)
        }
        endMarker = new AMap.Marker({
          position: [lng, lat],
          title: "终点",
        })
        map.add(endMarker)
        getAddress("end")
      }
      selectMode.value = null
    })
}
async function getAddress(type: "start"|"end"){
  let geocoder = new AMap.Geocoder()
  if(type === "start"){
    geocoder.getAddress(startLngLat.value, (status,result) => {
        if (status === "complete" && result.info === "OK") {
        startAddress.value = result.regeocode.formattedAddress
      }
    })
  }
  if(type === "end"){
    geocoder.getAddress(endLngLat.value, (status,result) => {
        if (status === "complete" && result.info === "OK") {
        endAddress.value = result.regeocode.formattedAddress
      }
    })
  }
} 

function updateDriverPosition(longitude: number, latitude: number) {
  if (!map) return
  
  const position = [longitude, latitude]
  
  if (driverMarker) {
    driverMarker.setPosition(position)
  } else {
    const AMap = (window as any).AMap
    driverMarker = new AMap.Marker({
      position: position,
      title: "司机位置"
    })
    map.add(driverMarker)
  }
  
  map.setCenter(position)
}

function startSelect(mode: 'start' | 'end') {
  selectMode.value = mode
}

function cancelSelect() {
  selectMode.value = null
}

async function submitOrder() {
  if (!startLngLat.value || !startAddress.value) {
    ElMessage.error('请先选择起点位置')
    return
  }
  if (!endLngLat.value || !endAddress.value) {
    ElMessage.error('请先选择终点位置')
    return
  }
  try{
      const {data} = await request.post("/order/create",{
        startLongitude: startLngLat.value![0],
        startLatitude: startLngLat.value![1],
        startAddress: startAddress.value,
        endLongitude: endLngLat.value![0],
        endLatitude: endLngLat.value![1],
        endAddress: endAddress.value
      })
      orderSubmitted.value = true
      orderStatus.value = 'waiting'
      orderStatusText.value = '等待接单...'
      ElMessage.success('订单创建成功')
      orderId.value = data
      orderTimer = setInterval(() => {
        ws.value.send(JSON.stringify({
          type: 'order-new',
          data:{
            orderId: orderId.value!,
            startAddress: startAddress.value,
            endAddress: endAddress.value,
            startLongitude: startLngLat.value![0],
            startLatitude: startLngLat.value![1],
            endLongitude: endLngLat.value![0],
            endLatitude: endLngLat.value![1],
            userInfo: userInfo.value
          }
        }))
      }, 5000)
  } catch(error: any){
    const errorMsg = "网络异常"
    ElMessage.error(errorMsg)
  }
}

async function cancelOrder() {
  try {
    const {code,msg} = await request.get("/order/cancel", { params: { orderId: orderId.value! } })
    if(code === 1){
      orderStatus.value = 'cancelled'
      orderStatusText.value = '已取消'
      orderSubmitted.value = false
      ElMessage.success('订单已取消')
    } else {
      ElMessage.error(msg || '订单取消失败') 
    }
  } catch(error: any) {
    const errorMsg = error?.response?.data?.msg || error?.message || '取消失败'
    ElMessage.error(errorMsg)
    clearInterval(orderTimer)
    orderId.value = -1
  }
} 

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

function handleProfile() {
  console.log('修改信息')
  showDropdown.value = false
}

async function getUserInfo() {
  const token = localStorage.getItem('token')
  if (!token) return null
  
  try {
    const user =  jwtDecode(token) as { name: string, userId: number }
    if (user) {
      userName.value = user.name || '用户'
      const {data} = await request.get("/user/info",{params:{id: user.id}})
      userInfo.value = data 
      if(userInfo.value){
        if(userInfo.value.gender = "男"){
          userInfo.value.name = userInfo.value.name.slice(0,1) + "先生"
        } 
        else if(userInfo.value.gender = "女"){
          userInfo.value.name = userInfo.value.name.slice(0,1) + "女士" 
        }
        userInfo.value.phone = userInfo.value.phone.slice(0,3) + "****" + userInfo.value.phone.slice(7)
      }
    }
  } catch (e) {
    console.error("获取用户信息失败")
  }
}

function logout() {
  localStorage.removeItem('token')
  router.replace('/login')
} 

onUnmounted(() => {
  clearInterval(orderTimer)
  if (ws.value) {
    ws.value.close(1000, 'Component unmounted')
    ws.value = null
  }
})
</script>

<style scoped>
.user-container {
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

.logout-btn {
  color: #666;
}

.logout-btn:hover {
  color: #32d699;
}

.user-main {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
}

.map-section {
  flex: 0 0 70%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  position: relative;
}

#map-placeholder {
  width: 100%;
  height: 100%;
}

.map-tip {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
}

.map-tip span {
  font-size: 14px;
  color: #333;
}

.cancel-btn {
  padding: 6px 16px;
  background: #f5f5f7;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #e8e8e8;
}

.panel-section {
  flex: 0 0 30%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  padding: 20px 0;
}

.panel-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  padding: 28px;
  width: 90%;
  min-height: 50vh;
  display: flex;
  flex-direction: column;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin-bottom: 20px;
}

.location-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.location-item:last-child {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.location-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 6px;
}

.location-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
  min-height: 20px;
}

.select-btn {
  padding: 8px 16px;
  background: #f0f9f4;
  border: 1px solid #32d699;
  border-radius: 6px;
  font-size: 13px;
  color: #32d699;
  cursor: pointer;
  transition: all 0.2s;
}

.select-btn:hover {
  background: #32d699;
  color: #fff;
}

.submit-btn {
  width: 100%;
  padding: 12px 24px;
  background: #32d699;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  color: #fff;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover {
  background: #28b88a;
}

.submit-btn:active {
  transform: scale(0.98);
}

.select-btn.disabled {
  background: #f5f5f7;
  border-color: #ddd;
  color: #999;
  cursor: not-allowed;
}

.select-btn.disabled:hover {
  background: #f5f5f7;
  color: #999;
}

.status-section {
  background: #fafafa;
  border-radius: 10px;
  padding: 18px;
  margin-top: 10px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.status-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.status-badge {
  font-size: 12px;
  padding: 4px 12px;
  background: #fff3cd;
  color: #856404;
  border-radius: 20px;
}

.status-actions {
  display: flex;
  justify-content: center;
}

.status-actions .cancel-btn {
  padding: 10px 24px;
  background: #fff;
  border: 1px solid #dc3545;
  border-radius: 8px;
  font-size: 14px;
  color: #dc3545;
  cursor: pointer;
  transition: all 0.2s;
}

.status-actions .cancel-btn:hover {
  background: #dc3545;
  color: #fff;
}
</style>

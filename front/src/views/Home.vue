<template>
  <div class="home">
    <div class="logo-group" :class="anim ? 'up' : ''">
      <div class="logo">CC</div>
      <h1>CC 打车</h1>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const anim = ref(false)

onMounted(() => {
  setTimeout(() => (anim.value = true), 200)
  setTimeout(() => router.push('/login'), 2400)
})
</script>

<style scoped>
.home {
  width: 100vw;
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding-bottom: 10vh;
  margin: 0;
}

.logo-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 1.6s cubic-bezier(0.3, 1, 0.6, 1);
}

.logo-group.up {
  transform: translateY(-42vh);
}

.logo {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: #32d699;
  color: #fff;
  font-size: 44px;
  font-weight: bold;
  display: grid;
  place-items: center;
  margin-bottom: 16px;
}

h1 {
  font-size: 32px;
  color: #222;
  letter-spacing: 4px;
  font-weight: 900;
}
</style>
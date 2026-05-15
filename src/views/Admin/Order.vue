<template>
  <div class="normal-order">
    <!-- 表格 -->
    <div class="table-wrap">
      <el-table :data="orderList" border stripe style="width: 100%">
        <el-table-column label="订单号" prop="id" align="center" />
        <el-table-column label="订单起点" prop="startAddress" align="center" />
        <el-table-column label="订单终点" prop="endAddress" align="center" />
        <el-table-column label="创建时间" prop="createTime" align="center" />
        <el-table-column label="订单状态" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div style="text-align: right; margin-top: 20px">
      <el-pagination
        v-model:current-page="page"
        @current-change="getList"
        :total="total"
        :page-size="14"
        layout="total, prev, pager, next, jumper"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const page = ref(1)
const orderList = ref([])
const total = ref(0)

const getList = async () => {
  try {
    const {data} = await request.get('/admin/order/check', {
      params: {
        page: page.value - 1
      }
    })
    orderList.value = data?.orderList || []
    total.value = data?.total || 0
  } catch (e) {
    console.error('请求订单列表失败', e)
    orderList.value = []
    total.value = 0
  }
}

// 订单状态文字
const getStatusText = (status: number) => {
  if (status === 0) return '待接单'
  if (status === 1) return '已接单'
  if (status === 2) return '已完成'
  if (status === 3) return '已取消'
  return '未知状态'
}

// 订单状态标签颜色
const getStatusTagType = (status: number) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'primary'
  if (status === 2) return 'success'
  if (status === 3) return 'danger'
  return ''
}

// 页面挂载自动请求
onMounted(() => getList())
</script>

<style scoped>
.normal-order {
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>
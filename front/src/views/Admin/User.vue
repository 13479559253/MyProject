<template>
  <div class="normal-user">
    <!-- 搜索栏 -->
    <div class="search-wrap">
      <el-input
        v-model="query.name"
        placeholder="输入姓名搜索"
        style="width: 180px; margin-right: 10px"
      />
      <el-select
        v-model="query.gender"
        placeholder="性别"
        style="width: 120px; margin-right: 10px"
      >
        <el-option label="全部" value="" />
        <el-option label="男" value="男" />
        <el-option label="女" value="女" />
      </el-select>

      <el-select
        v-model="query.role"
        placeholder="角色"
        style="width: 120px; margin-right: 10px"
      >
        <el-option label="全部" value="" />
        <el-option label="用户" value="1" />
        <el-option label="司机" value="2" />
      </el-select>

      <el-button
        type="primary"
        @click="getList"
        style="background: #32d699; border-color: #32d699"
      >
        搜索
      </el-button>
    </div>

    <!-- 表格 -->
    <div class="table-wrap" style="margin-top: 20px">
      <el-table :data="userList" border stripe style="width: 100%">
        <el-table-column label="ID" prop="id" align="center" />
        <el-table-column label="姓名" prop="name" align="center" />
        <el-table-column label="性别" prop="gender" align="center" />
        <el-table-column label="手机号" prop="phone" align="center" />
        <el-table-column label="角色" align="center">
          <template #default="scope">
            {{ scope.row.role == 1 ? '用户' : '司机' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status == 1 ? 'success' : 'danger'">
              {{ scope.row.status == 1 ? '正常' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button 
              type="text" 
              :style="{ color: scope.row.status == 1 ? '#f56c6c' : '#32d699' }"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status == 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>

      </el-table>
    </div>

    <!-- 分页 -->
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
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const query = ref({ name: '', gender: '', role: '' })
const page = ref(1)
const userList = ref([])
const total = ref(0)

// 获取列表
const getList = async () => {
  try {
    const {data} = await request.get('/admin/user/select', {
      params: {
        name: query.value.name,
        gender: query.value.gender,
        role: query.value.role,
        page: page.value - 1
      } 
    })
    userList.value = data?.userList || []
    total.value = data?.total || 0
  } catch (e) {
    console.error('请求失败', e)
    userList.value = []
    total.value = 0
  }
}

// 启用 / 停用
const toggleStatus = async (row: { status: number; id: string }) => {
  try {
    if (row.status == 1) {
      await request.get('/admin/ban?id=' + row.id)
      ElMessage.success('停用成功')
    } else {
      await request.get('/admin/recover?id=' + row.id)
      ElMessage.success('启用成功')
    }

    setTimeout(() => {
      getList()
    }, 200)
    
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => getList())
</script>

<style scoped>
.normal-user {
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
</style>
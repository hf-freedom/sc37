<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">车辆推荐</h3>
    </div>

    <el-card>
      <template #header>
        <span>输入需求条件，系统将为您推荐合适的车辆</span>
      </template>

      <el-form :model="searchForm" label-width="100px" inline>
        <el-form-item label="乘客人数">
          <el-input-number v-model="searchForm.passengerCount" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="偏好车型">
          <el-select v-model="searchForm.preferredType" placeholder="不限" clearable style="width: 150px">
            <el-option label="轿车" value="SEDAN" />
            <el-option label="SUV" value="SUV" />
            <el-option label="面包车" value="VAN" />
            <el-option label="大巴" value="BUS" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRecommend">
            <el-icon><Search /></el-icon>
            推荐车辆
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>推荐结果</span>
      </template>

      <el-table :data="recommendedVehicles" style="width: 100%" v-loading="loading">
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="type" label="车型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === searchForm.preferredType ? 'primary' : 'info'">
              {{ getVehicleTypeDescription(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="seats" label="座位数" width="80">
          <template #default="scope">
            <span v-if="scope.row.seats >= searchForm.passengerCount">{{ scope.row.seats }}座</span>
            <span v-else style="color: #f56c6c">{{ scope.row.seats }}座 (不足)</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success">可用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentMileage" label="当前里程(公里)" width="150" />
        <el-table-column label="是否需要保养" width="120">
          <template #default="scope">
            <el-tag v-if="checkMaintenanceNeeded(scope.row)" type="danger" effect="dark">
              需要保养
            </el-tag>
            <el-tag v-else type="success">
              正常
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐指数" width="120">
          <template #default="scope">
            <el-rate :model-value="getRecommendationScore(scope.row)" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleApply(scope.row)">
              申请用车
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="recommendedVehicles.length === 0 && !loading" description="暂无符合条件的推荐车辆" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { vehicleApi } from '../../api'

const router = useRouter()
const loading = ref(false)
const recommendedVehicles = ref([])

const searchForm = reactive({
  passengerCount: 3,
  preferredType: null
})

const getVehicleTypeDescription = (type) => {
  const typeMap = {
    'SEDAN': '轿车',
    'SUV': 'SUV',
    'VAN': '面包车',
    'BUS': '大巴'
  }
  return typeMap[type] || type
}

const checkMaintenanceNeeded = (vehicle) => {
  const mileageSinceMaintenance = vehicle.currentMileage - vehicle.lastMaintenanceMileage
  return mileageSinceMaintenance >= vehicle.maintenanceCycle
}

const getRecommendationScore = (vehicle) => {
  let score = 3
  if (vehicle.type === searchForm.preferredType) {
    score += 1
  }
  if (!checkMaintenanceNeeded(vehicle)) {
    score += 1
  }
  const extraSeats = vehicle.seats - searchForm.passengerCount
  if (extraSeats >= 0 && extraSeats <= 2) {
    score += 1
  }
  return Math.min(score, 5)
}

const handleRecommend = async () => {
  if (searchForm.passengerCount < 1) {
    ElMessage.warning('请输入乘客人数')
    return
  }

  loading.value = true
  try {
    const params = {
      passengerCount: searchForm.passengerCount
    }
    if (searchForm.preferredType) {
      params.preferredType = searchForm.preferredType
    }
    const res = await vehicleApi.recommend(params)
    recommendedVehicles.value = res.data
    if (res.data.length === 0) {
      ElMessage.info('暂无符合条件的推荐车辆')
    }
  } catch (error) {
    ElMessage.error('获取推荐车辆失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleApply = (vehicle) => {
  router.push({
    path: '/applications/create',
    query: {
      vehicleId: vehicle.id,
      vehicleType: vehicle.type,
      seats: vehicle.seats
    }
  })
}
</script>

<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon vehicle">
              <el-icon :size="40"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.vehicleCount }}</div>
              <div class="stat-label">车辆总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon driver">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.driverCount }}</div>
              <div class="stat-label">司机总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon application">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.applicationCount }}</div>
              <div class="stat-label">今日申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon maintenance">
              <el-icon :size="40"><Tools /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.maintenanceCount }}</div>
              <div class="stat-label">待处理保养</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待审批申请</span>
              <el-button type="primary" link @click="$router.push('/approvals')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="pendingApprovals" style="width: 100%">
            <el-table-column prop="applicantName" label="申请人" />
            <el-table-column prop="destination" label="目的地" />
            <el-table-column prop="passengerCount" label="人数" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag type="warning">待审批</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="pendingApprovals.length === 0" description="暂无待审批申请" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>进行中的行程</span>
              <el-button type="primary" link @click="$router.push('/applications')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="inProgressTrips" style="width: 100%">
            <el-table-column prop="applicantName" label="申请人" />
            <el-table-column prop="destination" label="目的地" />
            <el-table-column prop="passengerCount" label="人数" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag type="primary">进行中</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="inProgressTrips.length === 0" description="暂无进行中的行程" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>可用车辆</span>
              <el-button type="primary" link @click="$router.push('/vehicles')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="availableVehicles" style="width: 100%">
            <el-table-column prop="plateNumber" label="车牌号" />
            <el-table-column prop="type" label="车型">
              <template #default="scope">
                {{ getVehicleTypeDescription(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="seats" label="座位数" />
            <el-table-column prop="currentMileage" label="当前里程">
              <template #default="scope">
                {{ scope.row.currentMileage }} 公里
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>可用司机</span>
              <el-button type="primary" link @click="$router.push('/drivers')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="availableDrivers" style="width: 100%">
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="phone" label="电话" />
            <el-table-column prop="allowedVehicleTypes" label="准驾车型">
              <template #default="scope">
                <div v-if="scope.row.allowedVehicleTypes">
                  <el-tag v-for="type in scope.row.allowedVehicleTypes" :key="type" size="small" class="status-tag">
                    {{ getVehicleTypeDescription(type) }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="currentTaskCount" label="当前任务" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { vehicleApi, driverApi, applicationApi, maintenanceApi } from '../api'

const stats = ref({
  vehicleCount: 0,
  driverCount: 0,
  applicationCount: 0,
  maintenanceCount: 0
})

const pendingApprovals = ref([])
const inProgressTrips = ref([])
const availableVehicles = ref([])
const availableDrivers = ref([])

const getVehicleTypeDescription = (type) => {
  const typeMap = {
    'SEDAN': '轿车',
    'SUV': 'SUV',
    'VAN': '面包车',
    'BUS': '大巴'
  }
  return typeMap[type] || type
}

const loadData = async () => {
  try {
    const [vehiclesRes, driversRes, applicationsRes, maintenanceRes] = await Promise.all([
      vehicleApi.getAll(),
      driverApi.getAll(),
      applicationApi.getAll(),
      maintenanceApi.getAll()
    ])

    const vehicles = vehiclesRes.data
    const drivers = driversRes.data
    const applications = applicationsRes.data
    const maintenance = maintenanceRes.data

    stats.value.vehicleCount = vehicles.length
    stats.value.driverCount = drivers.length
    stats.value.applicationCount = applications.length
    stats.value.maintenanceCount = maintenance.filter(m => m.status !== 'COMPLETED' && m.status !== 'CANCELLED').length

    pendingApprovals.value = applications.filter(a => a.status === 'SUBMITTED').slice(0, 5)
    inProgressTrips.value = applications.filter(a => a.status === 'IN_PROGRESS').slice(0, 5)
    availableVehicles.value = vehicles.filter(v => v.status === 'AVAILABLE').slice(0, 5)
    availableDrivers.value = drivers.filter(d => d.status === 'AVAILABLE').slice(0, 5)
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-container {
  padding: 0;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-icon.vehicle {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.driver {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.application {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.maintenance {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  margin-left: 20px;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

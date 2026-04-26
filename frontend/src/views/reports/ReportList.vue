<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">月度报表</h3>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>生成月度报表</span>
        </div>
      </template>

      <div class="generate-section">
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          value-format="yyyy-MM"
          style="width: 200px; margin-right: 20px;"
        />
        <el-button type="primary" @click="handleGenerate">
          <el-icon><DocumentAdd /></el-icon>
          生成报表
        </el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>报表列表</span>
      </template>

      <el-table :data="reports" style="width: 100%" v-loading="loading">
        <el-table-column prop="month" label="月份" width="150">
          <template #default="scope">
            {{ scope.row.month }}
          </template>
        </el-table-column>
        <el-table-column prop="totalTrips" label="总出车次数" width="120" />
        <el-table-column prop="totalMileage" label="总里程(公里)" width="150">
          <template #default="scope">
            {{ scope.row.totalMileage?.toFixed(1) || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalFuelCost" label="油费(元)" width="120">
          <template #default="scope">
            {{ scope.row.totalFuelCost?.toFixed(2) || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalTollCost" label="过路费(元)" width="120">
          <template #default="scope">
            {{ scope.row.totalTollCost?.toFixed(2) || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalMaintenanceCost" label="维修保养费(元)" width="150">
          <template #default="scope">
            {{ scope.row.totalMaintenanceCost?.toFixed(2) || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCost" label="总费用(元)" width="120">
          <template #default="scope">
            <el-tag type="danger">
              {{ scope.row.totalCost?.toFixed(2) || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vehicleUtilizationRate" label="车辆使用率" width="120">
          <template #default="scope">
            <el-progress
              :percentage="scope.row.vehicleUtilizationRate || 0"
              :stroke-width="10"
              :color="getUtilizationColor(scope.row.vehicleUtilizationRate)"
              :show-text="true"
            />
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="reports.length === 0" description="暂无报表数据，请先生成报表" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '../../api'

const loading = ref(false)
const reports = ref([])
const selectedMonth = ref(null)

const getUtilizationColor = (rate) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#909399'
}

const loadReports = async () => {
  loading.value = true
  try {
    const res = await reportApi.getAll()
    reports.value = res.data
  } catch (error) {
    ElMessage.error('加载报表列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  if (!selectedMonth.value) {
    ElMessage.warning('请选择月份')
    return
  }

  const [year, month] = selectedMonth.value.split('-').map(Number)

  try {
    await reportApi.generate({ year, month })
    ElMessage.success('报表生成成功')
    loadReports()
  } catch (error) {
    ElMessage.error('生成报表失败')
    console.error(error)
  }
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.generate-section {
  display: flex;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

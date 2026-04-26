<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">用车申请管理</h3>
    </div>

    <div class="action-buttons">
      <el-button type="primary" @click="$router.push('/applications/create')">
        <el-icon><Plus /></el-icon>
        新建申请
      </el-button>
      <el-button @click="loadApplications">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table :data="applications" style="width: 100%" v-loading="loading">
      <el-table-column prop="applicantName" label="申请人" width="100" />
      <el-table-column prop="applicantDepartment" label="部门" width="120" />
      <el-table-column prop="startTime" label="开始时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="destination" label="目的地" width="120" />
      <el-table-column prop="passengerCount" label="人数" width="80" />
      <el-table-column prop="purpose" label="用车原因" width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusDescription(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="needsReview" label="费用复核" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.needsReview" type="danger" effect="dark">待复核</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === 'DRAFT'" type="primary" link @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button v-if="scope.row.status === 'DRAFT'" type="success" link @click="handleSubmit(scope.row)">
            提交
          </el-button>
          <el-button v-if="scope.row.status === 'APPROVED' && scope.row.assignedVehicleId" type="primary" link @click="handleStartTrip(scope.row)">
            开始行程
          </el-button>
          <el-button v-if="scope.row.status === 'IN_PROGRESS'" type="success" link @click="handleCompleteTrip(scope.row)">
            完成行程
          </el-button>
          <el-button v-if="scope.row.status === 'APPROVED' && !scope.row.assignedVehicleId" type="warning" link @click="handleAssign(scope.row)">
            派车
          </el-button>
          <el-button v-if="scope.row.status === 'NEEDS_REVIEW'" type="warning" link @click="handleReview(scope.row)">
            费用复核
          </el-button>
          <el-button v-if="scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELLED'" type="danger" link @click="handleCancel(scope.row)">
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="assignDialogVisible" title="派车" width="600px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="选择车辆">
          <el-select v-model="assignForm.vehicleId" placeholder="请选择车辆" style="width: 100%" @change="handleVehicleChange">
            <el-option
              v-for="vehicle in availableVehicles"
              :key="vehicle.id"
              :label="`${vehicle.plateNumber} - ${getVehicleTypeDescription(vehicle.type)} (${vehicle.seats}座)`"
              :value="vehicle.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择司机">
          <el-select v-model="assignForm.driverId" placeholder="请选择司机" style="width: 100%">
            <el-option
              v-for="driver in availableDrivers"
              :key="driver.id"
              :label="`${driver.name} - ${driver.phone}`"
              :value="driver.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAssignSubmit">确定派车</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="完成行程" width="600px">
      <el-form :model="completeForm" label-width="120px">
        <el-form-item label="实际里程(公里)">
          <el-input-number v-model="completeForm.actualMileage" :min="0" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="油费(元)">
          <el-input-number v-model="completeForm.fuelCost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="过路费(元)">
          <el-input-number v-model="completeForm.tollCost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="其他费用(元)">
          <el-input-number v-model="completeForm.otherCost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCompleteSubmit">确定完成</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="费用复核" width="500px">
      <el-alert :title="reviewReason" type="warning" show-icon style="margin-bottom: 20px;" />
      <el-form label-width="100px">
        <el-form-item label="复核结果">
          <el-radio-group v-model="reviewForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleReviewSubmit">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applicationApi, vehicleApi, driverApi } from '../../api'

const router = useRouter()
const applications = ref([])
const loading = ref(false)
const availableVehicles = ref([])
const availableDrivers = ref([])
const currentApplication = ref(null)

const assignDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const reviewReason = ref('')

const assignForm = reactive({
  vehicleId: null,
  driverId: null
})

const completeForm = reactive({
  actualMileage: 0,
  fuelCost: 0,
  tollCost: 0,
  otherCost: 0
})

const reviewForm = reactive({
  approved: true,
  comment: ''
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getStatusDescription = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'SUBMITTED': '待审批',
    'APPROVED': '审批通过',
    'REJECTED': '审批驳回',
    'IN_PROGRESS': '出车中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'NEEDS_REVIEW': '费用待复核'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'SUBMITTED': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info',
    'NEEDS_REVIEW': 'danger'
  }
  return typeMap[status] || 'info'
}

const getVehicleTypeDescription = (type) => {
  const typeMap = {
    'SEDAN': '轿车',
    'SUV': 'SUV',
    'VAN': '面包车',
    'BUS': '大巴'
  }
  return typeMap[type] || type
}

const loadApplications = async () => {
  loading.value = true
  try {
    const res = await applicationApi.getAll()
    applications.value = res.data
  } catch (error) {
    ElMessage.error('加载申请列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadAvailableVehicles = async (passengerCount = null) => {
  try {
    const res = await vehicleApi.getAvailable()
    let vehicles = res.data
    if (passengerCount != null) {
      vehicles = vehicles.filter(v => v.seats >= passengerCount)
    }
    availableVehicles.value = vehicles
  } catch (error) {
    console.error('加载可用车辆失败', error)
  }
}

const loadAvailableDrivers = async (vehicleType = null) => {
  try {
    let res
    if (vehicleType) {
      res = await driverApi.getAvailableForVehicleType(vehicleType)
    } else {
      res = await driverApi.getAvailable()
    }
    availableDrivers.value = res.data
  } catch (error) {
    console.error('加载可用司机失败', error)
  }
}

const handleEdit = (row) => {
  router.push(`/applications/edit/${row.id}`)
}

const handleSubmit = async (row) => {
  ElMessageBox.confirm('确定要提交该申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await applicationApi.submit(row.id)
      ElMessage.success('提交成功')
      loadApplications()
    } catch (error) {
      ElMessage.error('提交失败')
      console.error(error)
    }
  }).catch(() => {})
}

const handleAssign = (row) => {
  currentApplication.value = row
  loadAvailableVehicles(row.passengerCount)
  assignForm.vehicleId = null
  assignForm.driverId = null
  assignDialogVisible.value = true
}

const handleVehicleChange = (vehicleId) => {
  const vehicle = availableVehicles.value.find(v => v.id === vehicleId)
  if (vehicle) {
    loadAvailableDrivers(vehicle.type)
  }
}

const handleAssignSubmit = async () => {
  if (!assignForm.vehicleId || !assignForm.driverId) {
    ElMessage.warning('请选择车辆和司机')
    return
  }

  try {
    await applicationApi.assign(currentApplication.value.id, {
      vehicleId: assignForm.vehicleId,
      driverId: assignForm.driverId
    })
    ElMessage.success('派车成功')
    assignDialogVisible.value = false
    loadApplications()
  } catch (error) {
    ElMessage.error('派车失败')
    console.error(error)
  }
}

const handleStartTrip = async (row) => {
  ElMessageBox.confirm('确定要开始该行程吗？系统将检查车辆是否需要保养。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await applicationApi.start(row.id)
      ElMessage.success('行程已开始')
      loadApplications()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '开始行程失败')
      console.error(error)
    }
  }).catch(() => {})
}

const handleCompleteTrip = (row) => {
  currentApplication.value = row
  completeForm.actualMileage = row.estimatedMileage || 0
  completeForm.fuelCost = 0
  completeForm.tollCost = 0
  completeForm.otherCost = 0
  completeDialogVisible.value = true
}

const handleCompleteSubmit = async () => {
  try {
    await applicationApi.complete(currentApplication.value.id, {
      actualMileage: completeForm.actualMileage,
      fuelCost: completeForm.fuelCost,
      tollCost: completeForm.tollCost,
      otherCost: completeForm.otherCost
    })
    ElMessage.success('行程已完成')
    completeDialogVisible.value = false
    loadApplications()
  } catch (error) {
    ElMessage.error('完成行程失败')
    console.error(error)
  }
}

const handleReview = (row) => {
  currentApplication.value = row
  reviewReason.value = row.reviewReason || '实际里程超过预估里程过多'
  reviewForm.approved = true
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

const handleReviewSubmit = async () => {
  try {
    await applicationApi.review(currentApplication.value.id, {
      approved: reviewForm.approved,
      comment: reviewForm.comment
    })
    ElMessage.success('复核完成')
    reviewDialogVisible.value = false
    loadApplications()
  } catch (error) {
    ElMessage.error('复核失败')
    console.error(error)
  }
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await applicationApi.cancel(row.id, {})
      ElMessage.success('取消成功')
      loadApplications()
    } catch (error) {
      ElMessage.error('取消失败')
      console.error(error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadApplications()
})
</script>

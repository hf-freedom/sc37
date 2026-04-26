<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">维修保养管理</h3>
    </div>

    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建工单
      </el-button>
      <el-button @click="loadMaintenanceOrders">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table :data="maintenanceOrders" style="width: 100%" v-loading="loading">
      <el-table-column prop="vehiclePlateNumber" label="车牌号" width="120" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'REPAIR' ? 'danger' : 'warning'">
            {{ getTypeDescription(scope.row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusDescription(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
      <el-table-column prop="scheduledDate" label="计划时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.scheduledDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.startDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="completionDate" label="完成时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.completionDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="cost" label="费用(元)" width="100">
        <template #default="scope">
          {{ scope.row.cost || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 'SCHEDULED'"
            type="primary"
            link
            @click="handleStart(scope.row)"
          >
            开始
          </el-button>
          <el-button
            v-if="scope.row.status === 'IN_PROGRESS'"
            type="success"
            link
            @click="handleComplete(scope.row)"
          >
            完成
          </el-button>
          <el-button
            v-if="scope.row.status !== 'COMPLETED'"
            type="danger"
            link
            @click="handleCancel(scope.row)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="addDialogVisible" title="新建维修保养工单" width="600px">
      <el-form :model="addForm" label-width="120px">
        <el-form-item label="选择车辆">
          <el-select v-model="addForm.vehicleId" placeholder="请选择车辆" style="width: 100%">
            <el-option
              v-for="vehicle in vehicles"
              :key="vehicle.id"
              :label="`${vehicle.plateNumber} - ${getVehicleTypeDescription(vehicle.type)}`"
              :value="vehicle.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="addForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="常规保养" value="REGULAR_MAINTENANCE" />
            <el-option label="维修" value="REPAIR" />
            <el-option label="年检" value="INSPECTION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="计划时间">
          <el-date-picker
            v-model="addForm.scheduledDate"
            type="datetime"
            placeholder="选择计划时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="完成工单" width="500px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="费用(元)">
          <el-input-number v-model="completeForm.cost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="completeForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCompleteSubmit">确定完成</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelDialogVisible" title="取消工单" width="500px">
      <el-form>
        <el-form-item label="取消原因">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleCancelSubmit">确认取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { maintenanceApi, vehicleApi } from '../../api'

const loading = ref(false)
const maintenanceOrders = ref([])
const vehicles = ref([])
const currentOrder = ref(null)

const addDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const cancelDialogVisible = ref(false)

const addForm = reactive({
  vehicleId: null,
  type: 'REGULAR_MAINTENANCE',
  description: '',
  scheduledDate: null
})

const completeForm = reactive({
  cost: 0,
  notes: ''
})

const cancelForm = reactive({
  reason: ''
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getTypeDescription = (type) => {
  const typeMap = {
    'REGULAR_MAINTENANCE': '常规保养',
    'REPAIR': '维修',
    'INSPECTION': '年检',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

const getStatusDescription = (status) => {
  const statusMap = {
    'SCHEDULED': '已安排',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'SCHEDULED': 'warning',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
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

const loadMaintenanceOrders = async () => {
  loading.value = true
  try {
    const res = await maintenanceApi.getAll()
    maintenanceOrders.value = res.data
  } catch (error) {
    ElMessage.error('加载工单列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadVehicles = async () => {
  try {
    const res = await vehicleApi.getAll()
    vehicles.value = res.data
  } catch (error) {
    console.error('加载车辆列表失败', error)
  }
}

const handleAdd = () => {
  addForm.vehicleId = null
  addForm.type = 'REGULAR_MAINTENANCE'
  addForm.description = ''
  addForm.scheduledDate = null
  addDialogVisible.value = true
}

const handleAddSubmit = async () => {
  if (!addForm.vehicleId) {
    ElMessage.warning('请选择车辆')
    return
  }

  try {
    const data = {
      ...addForm,
      scheduledDate: addForm.scheduledDate ? addForm.scheduledDate.toISOString() : null
    }
    await maintenanceApi.create(data)
    ElMessage.success('创建工单成功')
    addDialogVisible.value = false
    loadMaintenanceOrders()
  } catch (error) {
    ElMessage.error('创建工单失败')
    console.error(error)
  }
}

const handleStart = async (row) => {
  ElMessageBox.confirm('确定要开始该工单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await maintenanceApi.start(row.id)
      ElMessage.success('工单已开始')
      loadMaintenanceOrders()
    } catch (error) {
      ElMessage.error('开始工单失败')
      console.error(error)
    }
  }).catch(() => {})
}

const handleComplete = (row) => {
  currentOrder.value = row
  completeForm.cost = 0
  completeForm.notes = ''
  completeDialogVisible.value = true
}

const handleCompleteSubmit = async () => {
  try {
    await maintenanceApi.complete(currentOrder.value.id, {
      cost: completeForm.cost,
      notes: completeForm.notes
    })
    ElMessage.success('工单已完成')
    completeDialogVisible.value = false
    loadMaintenanceOrders()
  } catch (error) {
    ElMessage.error('完成工单失败')
    console.error(error)
  }
}

const handleCancel = (row) => {
  currentOrder.value = row
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

const handleCancelSubmit = async () => {
  if (!cancelForm.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }

  try {
    await maintenanceApi.cancel(currentOrder.value.id, {
      reason: cancelForm.reason
    })
    ElMessage.success('工单已取消')
    cancelDialogVisible.value = false
    loadMaintenanceOrders()
  } catch (error) {
    ElMessage.error('取消工单失败')
    console.error(error)
  }
}

onMounted(() => {
  loadMaintenanceOrders()
  loadVehicles()
})
</script>

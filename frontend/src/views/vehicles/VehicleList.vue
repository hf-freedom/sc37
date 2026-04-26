<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">车辆管理</h3>
    </div>

    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加车辆
      </el-button>
      <el-button @click="loadVehicles">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table :data="vehicles" style="width: 100%" v-loading="loading">
      <el-table-column prop="plateNumber" label="车牌号" width="120" />
      <el-table-column prop="type" label="车型" width="100">
        <template #default="scope">
          {{ getVehicleTypeDescription(scope.row.type) }}
        </template>
      </el-table-column>
      <el-table-column prop="seats" label="座位数" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusDescription(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentMileage" label="当前里程(公里)" width="150" />
      <el-table-column prop="maintenanceCycle" label="保养周期(公里)" width="150" />
      <el-table-column prop="lastMaintenanceMileage" label="上次保养里程(公里)" width="180" />
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
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="form.plateNumber" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车型" prop="type">
          <el-select v-model="form.type" placeholder="请选择车型" style="width: 100%">
            <el-option label="轿车" value="SEDAN" />
            <el-option label="SUV" value="SUV" />
            <el-option label="面包车" value="VAN" />
            <el-option label="大巴" value="BUS" />
          </el-select>
        </el-form-item>
        <el-form-item label="座位数" prop="seats">
          <el-input-number v-model="form.seats" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="当前里程(公里)" prop="currentMileage">
          <el-input-number v-model="form.currentMileage" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="保养周期(公里)" prop="maintenanceCycle">
          <el-input-number v-model="form.maintenanceCycle" :min="1000" :precision="1" />
        </el-form-item>
        <el-form-item label="上次保养里程(公里)" prop="lastMaintenanceMileage">
          <el-input-number v-model="form.lastMaintenanceMileage" :min="0" :precision="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { vehicleApi } from '../../api'

const vehicles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const dialogTitle = ref('添加车辆')

const form = reactive({
  id: null,
  plateNumber: '',
  type: 'SEDAN',
  seats: 5,
  currentMileage: 0,
  maintenanceCycle: 5000,
  lastMaintenanceMileage: 0
})

const rules = {
  plateNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  type: [{ required: true, message: '请选择车型', trigger: 'change' }],
  seats: [{ required: true, message: '请输入座位数', trigger: 'blur' }]
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

const getStatusDescription = (status) => {
  const statusMap = {
    'AVAILABLE': '可用',
    'IN_USE': '使用中',
    'MAINTENANCE': '维修保养中',
    'UNAVAILABLE': '不可用'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'IN_USE': 'primary',
    'MAINTENANCE': 'warning',
    'UNAVAILABLE': 'danger'
  }
  return typeMap[status] || 'info'
}

const checkMaintenanceNeeded = (vehicle) => {
  const mileageSinceMaintenance = vehicle.currentMileage - vehicle.lastMaintenanceMileage
  return mileageSinceMaintenance >= vehicle.maintenanceCycle
}

const loadVehicles = async () => {
  loading.value = true
  try {
    const res = await vehicleApi.getAll()
    vehicles.value = res.data
  } catch (error) {
    ElMessage.error('加载车辆列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.plateNumber = ''
  form.type = 'SEDAN'
  form.seats = 5
  form.currentMileage = 0
  form.maintenanceCycle = 5000
  form.lastMaintenanceMileage = 0
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加车辆'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑车辆'
  form.id = row.id
  form.plateNumber = row.plateNumber
  form.type = row.type
  form.seats = row.seats
  form.currentMileage = row.currentMileage
  form.maintenanceCycle = row.maintenanceCycle
  form.lastMaintenanceMileage = row.lastMaintenanceMileage
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该车辆吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await vehicleApi.delete(row.id)
      ElMessage.success('删除成功')
      loadVehicles()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await vehicleApi.update(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await vehicleApi.create(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadVehicles()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  loadVehicles()
})
</script>

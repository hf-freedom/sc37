<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">司机管理</h3>
    </div>

    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加司机
      </el-button>
      <el-button @click="loadDrivers">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table :data="drivers" style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="licenseNumber" label="驾驶证号" width="150" />
      <el-table-column prop="allowedVehicleTypes" label="准驾车型" width="250">
        <template #default="scope">
          <div v-if="scope.row.allowedVehicleTypes">
            <el-tag v-for="type in scope.row.allowedVehicleTypes" :key="type" size="small" class="status-tag">
              {{ getVehicleTypeDescription(type) }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ getStatusDescription(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentTaskCount" label="当前任务数" width="100" />
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
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="驾驶证号" prop="licenseNumber">
          <el-input v-model="form.licenseNumber" placeholder="请输入驾驶证号" />
        </el-form-item>
        <el-form-item label="准驾车型" prop="allowedVehicleTypes">
          <el-select
            v-model="form.allowedVehicleTypes"
            multiple
            placeholder="请选择准驾车型"
            style="width: 100%"
          >
            <el-option label="轿车" value="SEDAN" />
            <el-option label="SUV" value="SUV" />
            <el-option label="面包车" value="VAN" />
            <el-option label="大巴" value="BUS" />
          </el-select>
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
import { driverApi } from '../../api'

const drivers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const dialogTitle = ref('添加司机')

const form = reactive({
  id: null,
  name: '',
  phone: '',
  licenseNumber: '',
  allowedVehicleTypes: []
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  licenseNumber: [{ required: true, message: '请输入驾驶证号', trigger: 'blur' }],
  allowedVehicleTypes: [{ required: true, message: '请选择准驾车型', trigger: 'change' }]
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
    'ON_DUTY': '出车中',
    'ON_LEAVE': '请假',
    'UNAVAILABLE': '不可用'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'ON_DUTY': 'primary',
    'ON_LEAVE': 'warning',
    'UNAVAILABLE': 'danger'
  }
  return typeMap[status] || 'info'
}

const loadDrivers = async () => {
  loading.value = true
  try {
    const res = await driverApi.getAll()
    drivers.value = res.data
  } catch (error) {
    ElMessage.error('加载司机列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.phone = ''
  form.licenseNumber = ''
  form.allowedVehicleTypes = []
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加司机'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑司机'
  form.id = row.id
  form.name = row.name
  form.phone = row.phone
  form.licenseNumber = row.licenseNumber
  form.allowedVehicleTypes = row.allowedVehicleTypes || []
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该司机吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await driverApi.delete(row.id)
      ElMessage.success('删除成功')
      loadDrivers()
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
          await driverApi.update(form.id, form)
          ElMessage.success('更新成功')
        } else {
          await driverApi.create(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadDrivers()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  loadDrivers()
})
</script>

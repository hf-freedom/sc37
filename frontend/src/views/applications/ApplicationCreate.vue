<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">新建用车申请</h3>
    </div>

    <el-card>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" :inline="false">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请人" prop="applicantName">
              <el-select v-model="form.applicantId" placeholder="请选择申请人" style="width: 100%" @change="handleApplicantChange">
                <el-option
                  v-for="employee in employees"
                  :key="employee.id"
                  :label="`${employee.name} - ${employee.department}`"
                  :value="employee.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input v-model="form.applicantDepartment" placeholder="自动填充" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                :disabled-date="disabledDate"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                :disabled-date="disabledDate"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目的地" prop="destination">
              <el-input v-model="form.destination" placeholder="请输入目的地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乘车人数" prop="passengerCount">
              <el-input-number v-model="form.passengerCount" :min="1" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="用车原因" prop="purpose">
          <el-input
            v-model="form.purpose"
            type="textarea"
            :rows="3"
            placeholder="请输入用车原因"
          />
        </el-form-item>

        <el-form-item label="预估里程(公里)" prop="estimatedMileage">
          <el-input-number v-model="form.estimatedMileage" :min="0" :precision="1" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">车辆推荐</el-divider>

      <div class="recommend-section">
        <el-button type="primary" @click="handleRecommend" :disabled="!canRecommend">
          <el-icon><Search /></el-icon>
          获取推荐车辆
        </el-button>
        <span style="margin-left: 10px; color: #909399;">
          {{ canRecommend ? '选择人数后可获取推荐' : '请先填写乘车人数' }}
        </span>
      </div>

      <el-table
        v-if="recommendedVehicles.length > 0"
        :data="recommendedVehicles"
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="type" label="车型" width="100">
          <template #default="scope">
            {{ getVehicleTypeDescription(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="seats" label="座位数" width="80" />
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
      </el-table>

      <el-divider />

      <div class="action-buttons">
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button type="primary" @click="handleSave">
          <el-icon><Document /></el-icon>
          保存草稿
        </el-button>
        <el-button type="success" @click="handleSubmit">
          <el-icon><Check /></el-icon>
          提交申请
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applicationApi, employeeApi, vehicleApi } from '../../api'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const employees = ref([])
const recommendedVehicles = ref([])

const form = reactive({
  applicantId: null,
  applicantName: '',
  applicantDepartment: '',
  startTime: null,
  endTime: null,
  destination: '',
  passengerCount: 1,
  purpose: '',
  estimatedMileage: 0
})

const rules = {
  applicantId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  passengerCount: [{ required: true, message: '请输入乘车人数', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入用车原因', trigger: 'blur' }]
}

const canRecommend = computed(() => {
  return form.passengerCount > 0
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

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const handleApplicantChange = (id) => {
  const employee = employees.value.find(e => e.id === id)
  if (employee) {
    form.applicantName = employee.name
    form.applicantDepartment = employee.department
  }
}

const handleRecommend = async () => {
  if (form.passengerCount < 1) {
    ElMessage.warning('请先填写乘车人数')
    return
  }

  try {
    const params = {
      passengerCount: form.passengerCount
    }
    const res = await vehicleApi.recommend(params)
    recommendedVehicles.value = res.data
    if (res.data.length === 0) {
      ElMessage.info('暂无符合条件的推荐车辆')
    }
  } catch (error) {
    ElMessage.error('获取推荐车辆失败')
    console.error(error)
  }
}

const loadEmployees = async () => {
  try {
    const res = await employeeApi.getAll()
    employees.value = res.data
  } catch (error) {
    console.error('加载员工列表失败', error)
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = {
          ...form,
          startTime: form.startTime ? form.startTime.toISOString() : null,
          endTime: form.endTime ? form.endTime.toISOString() : null
        }
        await applicationApi.create(data)
        ElMessage.success('保存草稿成功')
        router.push('/applications')
      } catch (error) {
        ElMessage.error('保存失败')
        console.error(error)
      }
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = {
          ...form,
          startTime: form.startTime ? form.startTime.toISOString() : null,
          endTime: form.endTime ? form.endTime.toISOString() : null
        }
        const createRes = await applicationApi.create(data)
        await applicationApi.submit(createRes.data.id)
        ElMessage.success('提交申请成功')
        router.push('/applications')
      } catch (error) {
        ElMessage.error('提交失败')
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  loadEmployees()
  
  if (route.query.passengerCount) {
    form.passengerCount = parseInt(route.query.passengerCount)
  }
})
</script>

<style scoped>
.recommend-section {
  display: flex;
  align-items: center;
}
</style>

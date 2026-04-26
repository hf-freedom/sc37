<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">编辑用车申请</h3>
    </div>

    <el-card v-loading="loading">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="申请人">
              <el-input v-model="form.applicantName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input v-model="form.applicantDepartment" disabled />
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

      <el-divider />

      <div class="action-buttons">
        <el-button @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-button type="primary" @click="handleSave">
          <el-icon><Document /></el-icon>
          保存
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applicationApi } from '../../api'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  id: null,
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
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  passengerCount: [{ required: true, message: '请输入乘车人数', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入用车原因', trigger: 'blur' }]
}

const loadApplication = async () => {
  loading.value = true
  try {
    const res = await applicationApi.getById(route.params.id)
    const data = res.data
    form.id = data.id
    form.applicantName = data.applicantName
    form.applicantDepartment = data.applicantDepartment
    form.startTime = data.startTime ? new Date(data.startTime) : null
    form.endTime = data.endTime ? new Date(data.endTime) : null
    form.destination = data.destination
    form.passengerCount = data.passengerCount
    form.purpose = data.purpose
    form.estimatedMileage = data.estimatedMileage || 0
  } catch (error) {
    ElMessage.error('加载申请详情失败')
    console.error(error)
  } finally {
    loading.value = false
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
        ElMessage.success('保存成功')
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
        await applicationApi.create(data)
        await applicationApi.submit(form.id)
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
  loadApplication()
})
</script>

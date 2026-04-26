<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">审批管理</h3>
    </div>

    <el-card>
      <template #header>
        <span>待审批申请</span>
      </template>

      <el-table :data="pendingApprovals" style="width: 100%" v-loading="loading">
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
        <el-table-column prop="purpose" label="用车原因" width="200" show-overflow-tooltip />
        <el-table-column prop="estimatedMileage" label="预估里程" width="120">
          <template #default="scope">
            {{ scope.row.estimatedMileage || '-' }} 公里
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">
              查看详情
            </el-button>
            <el-button type="success" link @click="handleApprove(scope.row)">
              通过
            </el-button>
            <el-button type="danger" link @click="handleReject(scope.row)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="pendingApprovals.length === 0" description="暂无待审批申请" />
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>已审批申请</span>
      </template>

      <el-table :data="processedApprovals" style="width: 100%">
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="applicantDepartment" label="部门" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="destination" label="目的地" width="120" />
        <el-table-column prop="passengerCount" label="人数" width="80" />
        <el-table-column prop="status" label="审批状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusDescription(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="申请详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请人">{{ currentApplication?.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentApplication?.applicantDepartment }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentApplication?.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentApplication?.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="目的地">{{ currentApplication?.destination }}</el-descriptions-item>
        <el-descriptions-item label="乘车人数">{{ currentApplication?.passengerCount }}人</el-descriptions-item>
        <el-descriptions-item label="用车原因" :span="2">{{ currentApplication?.purpose }}</el-descriptions-item>
        <el-descriptions-item label="预估里程">
          {{ currentApplication?.estimatedMileage || '-' }} 公里
        </el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag :type="getStatusTagType(currentApplication?.status)">
            {{ getStatusDescription(currentApplication?.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider v-if="currentApplication?.approvalRecords?.length > 0" content-position="left">
        审批记录
      </el-divider>
      <el-timeline v-if="currentApplication?.approvalRecords?.length > 0">
        <el-timeline-item
          v-for="(record, index) in currentApplication.approvalRecords"
          :key="index"
          :timestamp="formatDateTime(record.approvedAt)"
          placement="top"
        >
          <el-card>
            <h4>{{ record.approverName }}</h4>
            <el-tag :type="record.status === 'APPROVED' ? 'success' : 'danger'">
              {{ record.status === 'APPROVED' ? '已通过' : '已驳回' }}
            </el-tag>
            <p style="margin-top: 10px;">{{ record.comment || '无备注' }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentApplication?.status === 'SUBMITTED'"
            type="success"
            @click="handleApprove(currentApplication)"
          >
            通过
          </el-button>
          <el-button
            v-if="currentApplication?.status === 'SUBMITTED'"
            type="danger"
            @click="handleReject(currentApplication)"
          >
            驳回
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="500px">
      <el-form>
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleRejectSubmit">确认驳回</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applicationApi } from '../../api'

const loading = ref(false)
const applications = ref([])
const currentApplication = ref(null)
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)

const rejectForm = reactive({
  comment: ''
})

const pendingApprovals = computed(() => {
  return applications.value.filter(a => a.status === 'SUBMITTED')
})

const processedApprovals = computed(() => {
  return applications.value.filter(a => a.status === 'APPROVED' || a.status === 'REJECTED')
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

const handleView = (row) => {
  currentApplication.value = row
  detailDialogVisible.value = true
}

const handleApprove = async (row) => {
  ElMessageBox.confirm('确定要通过该申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await applicationApi.approve(row.id, {
        approverId: 1,
        approverName: '系统管理员',
        comment: '审批通过'
      })
      ElMessage.success('审批通过')
      detailDialogVisible.value = false
      loadApplications()
    } catch (error) {
      ElMessage.error('审批失败')
      console.error(error)
    }
  }).catch(() => {})
}

const handleReject = (row) => {
  currentApplication.value = row
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

const handleRejectSubmit = async () => {
  if (!rejectForm.comment.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  try {
    await applicationApi.reject(currentApplication.value.id, {
      approverId: 1,
      approverName: '系统管理员',
      comment: rejectForm.comment
    })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    detailDialogVisible.value = false
    loadApplications()
  } catch (error) {
    ElMessage.error('驳回失败')
    console.error(error)
  }
}

onMounted(() => {
  loadApplications()
})
</script>

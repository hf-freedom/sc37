<template>
  <el-container class="app-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="30"><Car /></el-icon>
        <span>用车管理系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-sub-menu index="vehicle">
          <template #title>
            <el-icon><Van /></el-icon>
            <span>车辆管理</span>
          </template>
          <el-menu-item index="/vehicles">车辆列表</el-menu-item>
          <el-menu-item index="/vehicles/recommend">车辆推荐</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="driver">
          <template #title>
            <el-icon><User /></el-icon>
            <span>司机管理</span>
          </template>
          <el-menu-item index="/drivers">司机列表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="application">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>用车申请</span>
          </template>
          <el-menu-item index="/applications">申请列表</el-menu-item>
          <el-menu-item index="/applications/create">新建申请</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="approval">
          <template #title>
            <el-icon><Edit /></el-icon>
            <span>审批管理</span>
          </template>
          <el-menu-item index="/approvals">待审批列表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="maintenance">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>维修保养</span>
          </template>
          <el-menu-item index="/maintenance">工单列表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="report">
          <template #title>
            <el-icon><DataLine /></el-icon>
            <span>报表统计</span>
          </template>
          <el-menu-item index="/reports">月度报表</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="employee">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>员工管理</span>
          </template>
          <el-menu-item index="/employees">员工列表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-title">企业用车管理系统</div>
        <div class="header-user">
          <el-icon><User /></el-icon>
          <span>管理员</span>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', Arial, sans-serif;
}

.app-container {
  height: 100%;
}

.aside {
  background-color: #304156;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
  gap: 10px;
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #304156;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.page-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.table-container {
  margin-top: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.action-buttons {
  margin-bottom: 20px;
}

.dialog-footer {
  text-align: right;
}

.status-tag {
  margin-left: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

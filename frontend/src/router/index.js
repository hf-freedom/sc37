import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/vehicles',
    name: 'Vehicles',
    component: () => import('../views/vehicles/VehicleList.vue')
  },
  {
    path: '/vehicles/recommend',
    name: 'VehicleRecommend',
    component: () => import('../views/vehicles/VehicleRecommend.vue')
  },
  {
    path: '/drivers',
    name: 'Drivers',
    component: () => import('../views/drivers/DriverList.vue')
  },
  {
    path: '/applications',
    name: 'Applications',
    component: () => import('../views/applications/ApplicationList.vue')
  },
  {
    path: '/applications/create',
    name: 'ApplicationCreate',
    component: () => import('../views/applications/ApplicationCreate.vue')
  },
  {
    path: '/applications/edit/:id',
    name: 'ApplicationEdit',
    component: () => import('../views/applications/ApplicationEdit.vue')
  },
  {
    path: '/approvals',
    name: 'Approvals',
    component: () => import('../views/approvals/ApprovalList.vue')
  },
  {
    path: '/maintenance',
    name: 'Maintenance',
    component: () => import('../views/maintenance/MaintenanceList.vue')
  },
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('../views/reports/ReportList.vue')
  },
  {
    path: '/employees',
    name: 'Employees',
    component: () => import('../views/employees/EmployeeList.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

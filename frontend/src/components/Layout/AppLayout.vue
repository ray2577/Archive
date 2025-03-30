<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">Archive System</div>
      <el-menu
        mode="horizontal"
        :router="true"
        class="menu"
      >
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/archive">档案管理</el-menu-item>
        <el-menu-item index="/about">关于</el-menu-item>
      </el-menu>
      <div class="user-info">
        <el-dropdown>
          <span class="el-dropdown-link">
            用户名
            <el-icon class="el-icon--right">
              <arrow-down />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人信息</el-dropdown-item>
              <el-dropdown-item>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container>
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ArrowDown } from '@element-plus/icons-vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 40px;
}

.menu {
  flex: 1;
}

.user-info {
  margin-left: 20px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style> 
<template>
  <div class="about">
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <h2>关于我们</h2>
        </div>
      </template>
      <div class="content">
        <h3>档案管理系统</h3>
        <p>这是一个基于 Vue 3 + Element Plus 开发的现代化档案管理系统。</p>
        <h4>主要功能：</h4>
        <ul>
          <li>档案的创建、编辑、删除和查询</li>
          <li>档案分类管理</li>
          <li>实时统计和数据分析</li>
          <li>用户权限管理</li>
        </ul>
        <h4>技术栈：</h4>
        <ul>
          <li>Vue 3 - 渐进式 JavaScript 框架</li>
          <li>Element Plus - 基于 Vue 3 的组件库</li>
          <li>Vue Router - 官方路由管理器</li>
          <li>Pinia - Vue 的状态管理库</li>
        </ul>
        <div class="version">
          <p>当前版本：v1.0.0</p>
          <p>最后更新：2024-03-04</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.about {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.about-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content {
  line-height: 1.6;
}

h3 {
  color: #409EFF;
  margin-bottom: 20px;
}

h4 {
  margin: 20px 0 10px;
  color: #606266;
}

ul {
  padding-left: 20px;
  margin: 10px 0;
}

li {
  margin: 5px 0;
  color: #606266;
}

.version {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
  color: #909399;
  font-size: 14px;
}

.version p {
  margin: 5px 0;
}
</style>
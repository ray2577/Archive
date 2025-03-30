<template>
  <div class="api-docs-container">
    <h2>后端API文档</h2>
    
    <el-card>
      <div class="api-docs-content">
        <el-input
          v-model="apiDocUrl"
          placeholder="输入Swagger或API文档地址"
          class="docs-url-input"
          clearable
        >
          <template #append>
            <el-button @click="loadApiDocs">加载</el-button>
          </template>
        </el-input>
        
        <iframe v-if="docLoaded" :src="apiDocUrl" class="docs-iframe"></iframe>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const apiDocUrl = ref('http://localhost:8080/swagger-ui.html')
const docLoaded = ref(false)

const loadApiDocs = () => {
  docLoaded.value = true
}
</script>

<style scoped>
.api-docs-container {
  padding: 20px;
}

.docs-url-input {
  margin-bottom: 20px;
}

.docs-iframe {
  width: 100%;
  height: 600px;
  border: none;
}
</style> 
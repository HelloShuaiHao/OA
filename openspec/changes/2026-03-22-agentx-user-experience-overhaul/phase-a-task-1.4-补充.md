# Phase A Task 1.4 补充：从 Flowable 选择流程

**补充内容：** 后端实现 - 从 Flowable 加载流程定义

---

## 后端实现

### 1. Controller 实现

```java
@RestController
@RequestMapping("/agentx/process")
public class AgentProcessController {

    @Resource
    private RepositoryService repositoryService;
    
    @Resource
    private ProcessDiagramGenerator processDiagramGenerator;

    /**
     * 获取 Flowable 流程定义列表
     */
    @GetMapping("/definitions")
    public CommonResult<List<ProcessDefinitionVO>> getProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService
            .createProcessDefinitionQuery()
            .latestVersion()  // 只获取最新版本
            .orderByProcessDefinitionName()
            .asc()
            .list();
        
        List<ProcessDefinitionVO> result = definitions.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return success(result);
    }

    /**
     * 获取流程图
     */
    @GetMapping("/diagram/{processDefinitionId}")
    public void getProcessDiagram(
        @PathVariable String processDefinitionId,
        HttpServletResponse response
    ) throws IOException {
        ProcessDefinition pd = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId)
            .singleResult();
        
        if (pd == null) {
            response.setStatus(404);
            return;
        }
        
        // 获取 BPMN 模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        
        // 生成流程图
        InputStream diagram = processDiagramGenerator.generateDiagram(
            bpmnModel,
            "png",
            Collections.emptyList(),
            Collections.emptyList(),
            "宋体", "宋体", "宋体",
            null, 1.0, false
        );
        
        // 输出图片
        response.setContentType("image/png");
        IOUtils.copy(diagram, response.getOutputStream());
    }

    private ProcessDefinitionVO convertToVO(ProcessDefinition pd) {
        ProcessDefinitionVO vo = new ProcessDefinitionVO();
        vo.setKey(pd.getKey());
        vo.setName(pd.getName());
        vo.setVersion(pd.getVersion());
        vo.setDescription(pd.getDescription());
        vo.setCategory(pd.getCategory());
        vo.setDiagramUrl("/agentx/process/diagram/" + pd.getId());
        return vo;
    }
}
```

---

### 2. 前端实现补充

```vue
<template>
  <div class="process-selector">
    <el-card>
      <template #header>
        <span>选择流程</span>
        <el-button type="primary" size="small" @click="loadProcesses">
          刷新列表
        </el-button>
      </template>

      <!-- 流程列表 -->
      <el-table
        :data="processes"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="流程名称" prop="name" />
        <el-table-column label="流程Key" prop="key" />
        <el-table-column label="版本" prop="version" width="80" />
        <el-table-column label="分类" prop="category" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewDiagram(row)"
            >
              查看流程图
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 流程图预览对话框 -->
    <el-dialog
      v-model="diagramVisible"
      title="流程图预览"
      width="80%"
    >
      <img
        :src="currentDiagramUrl"
        style="width: 100%"
        alt="流程图"
      />
    </el-dialog>

    <!-- 流程选择策略 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>流程选择策略</span>
      </template>

      <el-radio-group v-model="selectionMode">
        <el-radio label="rule">规则选择</el-radio>
        <el-radio label="auto">AI 自动选择</el-radio>
      </el-radio-group>

      <!-- 规则配置器 -->
      <div v-if="selectionMode === 'rule'" style="margin-top: 20px">
        <el-button @click="addRule" size="small">添加规则</el-button>
        
        <div
          v-for="(rule, index) in rules"
          :key="index"
          class="rule-item"
        >
          <el-select v-model="rule.field" placeholder="字段">
            <el-option label="用户角色" value="userRole" />
            <el-option label="请假天数" value="leaveDays" />
            <el-option label="金额" value="amount" />
          </el-select>

          <el-select v-model="rule.operator" placeholder="操作符">
            <el-option label="等于" value="=" />
            <el-option label="大于" value=">" />
            <el-option label="小于" value="<" />
          </el-select>

          <el-input v-model="rule.value" placeholder="值" />

          <span>→</span>

          <el-select v-model="rule.processKey" placeholder="流程">
            <el-option
              v-for="p in selectedProcesses"
              :key="p.key"
              :label="p.name"
              :value="p.key"
            />
          </el-select>

          <el-button
            @click="deleteRule(index)"
            type="danger"
            icon="Delete"
            size="small"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProcessDefinitions } from '@/api/agentx/process'

const processes = ref([])
const selectedProcesses = ref([])
const diagramVisible = ref(false)
const currentDiagramUrl = ref('')
const selectionMode = ref('rule')
const rules = ref([])

const loadProcesses = async () => {
  const res = await getProcessDefinitions()
  processes.value = res.data
}

const handleSelectionChange = (selection) => {
  selectedProcesses.value = selection
}

const viewDiagram = (row) => {
  currentDiagramUrl.value = row.diagramUrl
  diagramVisible.value = true
}

const addRule = () => {
  rules.value.push({
    field: '',
    operator: '=',
    value: '',
    processKey: ''
  })
}

const deleteRule = (index) => {
  rules.value.splice(index, 1)
}

onMounted(() => {
  loadProcesses()
})
</script>
```

---

### 3. 验收标准补充

**功能验收：**
- [ ] 可以从 Flowable 加载流程定义列表
- [ ] 只显示最新版本的流程
- [ ] 流程列表按名称排序
- [ ] 可以多选流程
- [ ] 点击"查看流程图"显示流程图预览
- [ ] 流程图清晰可见，支持中文
- [ ] 规则配置器可以添加/删除规则
- [ ] 规则中的流程下拉框只显示已选择的流程

**性能验收：**
- [ ] 流程列表加载时间 < 1s
- [ ] 流程图加载时间 < 2s
- [ ] 支持 100+ 流程定义

**测试用例：**
```java
@Test
public void testGetProcessDefinitions() {
    // 1. 部署 3 个流程，每个 2 个版本
    deployProcess("leave_approval", 2);
    deployProcess("expense_approval", 2);
    deployProcess("purchase_approval", 2);
    
    // 2. 调用 API
    List<ProcessDefinitionVO> result = processController
        .getProcessDefinitions()
        .getData();
    
    // 3. 验证只返回最新版本
    assertEquals(3, result.size());
    
    // 4. 验证版本号都是 2
    assertTrue(result.stream().allMatch(p -> p.getVersion() == 2));
}
```

---

**补充完成！**

# 执行自动化测试

## 前置条件

1. OA 系统运行在 `http://localhost:48080`
2. OpenFang 运行在 `http://localhost:4201`
3. 已创建 `oa-approval-assistant` agent（ID: 05ea9016-b608-40d2-b6f0-7ff3e04497b8）

## 执行步骤

### 1. 准备测试数据

**方式 1：SQL 导入（需要 MySQL 密码）**
```bash
mysql -h127.0.0.1 -uroot -p<your_password> ruoyi-vue-pro < prepare-test-data.sql
```

**方式 2：手动创建（推荐）**
在 OA 系统中手动创建 3 条请假申请：
- 用户 A：请假 1 天
- 用户 B：请假 2 天
- 用户 C：请假 5 天

### 2. 运行自动化测试
```bash
chmod +x test-auto-approval.sh
./test-auto-approval.sh
```

### 3. 观察结果

测试脚本会：
1. 获取审批人 token
2. 查询待办任务
3. 让 agent 自动处理审批
4. 验证审批结果

## 预期结果

- 2天的请假：自动通过
- 1天的请假：自动通过
- 5天的请假：agent 询问是否通过

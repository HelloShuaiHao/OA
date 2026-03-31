import { expect, test, type Page } from '@playwright/test'

const TENANT_ID = 1

async function login(page: Page) {
  const loginResponse = await page.request.post('http://localhost:48080/admin-api/system/auth/login', {
    headers: {
      'Content-Type': 'application/json',
      'tenant-id': String(TENANT_ID)
    },
    data: {
      username: 'e1221805',
      password: 'admin123456',
      tenantName: '芋道源码',
      captchaVerification: ''
    }
  })
  const loginBody = await loginResponse.json()
  expect(loginBody.code).toBe(0)

  const infoResponse = await page.request.get(
    'http://localhost:48080/admin-api/system/auth/get-permission-info',
    {
      headers: {
        Authorization: `Bearer ${loginBody.data.accessToken}`,
        'tenant-id': String(TENANT_ID)
      }
    }
  )
  const infoBody = await infoResponse.json()
  expect(infoBody.code).toBe(0)

  await page.addInitScript(
    ({ accessToken, refreshToken, tenantId, userInfo, roleRouters }) => {
      const encode = (value: unknown) => {
        const now = Date.now()
        return JSON.stringify({
          c: now,
          e: now + 24 * 60 * 60 * 1000,
          v: JSON.stringify(value)
        })
      }
      window.localStorage.setItem('ACCESS_TOKEN', encode(accessToken))
      window.localStorage.setItem('REFRESH_TOKEN', encode(refreshToken))
      window.localStorage.setItem('tenantId', encode(tenantId))
      window.localStorage.setItem('user', encode(userInfo))
      window.localStorage.setItem('roleRouters', encode(roleRouters))
    },
    {
      accessToken: loginBody.data.accessToken,
      refreshToken: loginBody.data.refreshToken,
      tenantId: TENANT_ID,
      userInfo: infoBody.data,
      roleRouters: infoBody.data.menus
    }
  )

  await page.goto('/')
  await page.waitForLoadState('networkidle')
}

async function selectFirstDept(page: Page) {
  await page.locator('.el-loading-mask').waitFor({ state: 'detached' }).catch(() => undefined)
  const deptSelect = page.locator('.el-form-item').filter({ hasText: '所属部门' }).locator('.el-select').first()
  await deptSelect.click()
  await page.locator('[role="option"]:visible').filter({ hasText: /\S/ }).first().click()
}

async function fillBasicInfo(page: Page, agentName: string, description: string) {
  const nameInput = page.locator('.el-form-item').filter({ hasText: '员工名称' }).locator('input').first()
  const descriptionInput = page.locator('.el-form-item').filter({ hasText: '员工描述' }).locator('textarea').first()
  await nameInput.fill(agentName)
  await expect(nameInput).toHaveValue(agentName)
  await Promise.all([
    page.waitForResponse(
      (response) => response.url().includes('/agentx/agent/check-name') && response.request().method() === 'GET'
    ),
    nameInput.evaluate((element) => (element as HTMLInputElement).blur())
  ])
  await descriptionInput.fill(description)
}

test.describe('AgentX acceptance', () => {
  test('create wizard step gate should follow acceptance criteria', async ({ page }) => {
    await login(page)
    await page.goto('/agentx/agent/list')
    await page.waitForLoadState('networkidle')
    await page.getByRole('button', { name: /创建数字员工|创建 Agent/i }).click()
    await expect(page.getByText('5 步向导：创建数字员工')).toBeVisible()

    const nextButton = page.getByRole('button', { name: '下一步' })
    await expect(nextButton).toBeDisabled()

    await fillBasicInfo(page, '请假审批助手E2E', '这是一个用于自动处理请假审批流程的数字员工。')
    await selectFirstDept(page)

    await expect(nextButton).toBeEnabled()
    await nextButton.click()
    await expect(page.getByText('选择模板')).toBeVisible()

    await expect(nextButton).toBeDisabled()
    await page.locator('.template-card').first().click()
    await expect(nextButton).toBeEnabled()
    await nextButton.click()

    await expect(page.getByText('配置能力')).toBeVisible()
    const checked = page.locator('.el-checkbox.is-checked')
    const checkedCount = await checked.count()
    if (checkedCount > 0) {
      for (let i = 0; i < checkedCount; i++) {
        await checked.nth(0).click()
      }
    }
    await expect(nextButton).toBeDisabled()
    await page.locator('.group-options .el-checkbox').first().click()
    await expect(nextButton).toBeEnabled()
  })

  test('phase a draft lifecycle should work in real browser', async ({ page }) => {
    const agentName = `请假助手${Date.now().toString().slice(-6)}`

    await login(page)
    await page.goto('/agentx/agent/list')
    await page.waitForLoadState('networkidle')

    await expect(page.getByRole('button', { name: /创建数字员工|创建 Agent/i })).toBeVisible()
    await page.getByRole('button', { name: /创建数字员工|创建 Agent/i }).click()
    await expect(page.getByText('5 步向导：创建数字员工')).toBeVisible()
    await page.locator('.el-loading-mask').waitFor({ state: 'detached' }).catch(() => undefined)

    await fillBasicInfo(page, agentName, '这是一个用于真实浏览器验收的数字员工草稿。')
    await selectFirstDept(page)
    await expect(page.getByRole('button', { name: '下一步' })).toBeEnabled()
    await page.getByRole('button', { name: '下一步' }).click()

    await page.locator('.template-card').first().click()
    await page.getByRole('button', { name: '下一步' }).click()
    await page.getByRole('button', { name: '下一步' }).click()

    await expect(page.getByText('从流程库选择')).toBeVisible()
    await page.locator('.process-row .el-checkbox').first().click()
    await expect(page.getByText(/已选流程 1 个/)).toBeVisible()
    await page.getByRole('button', { name: '下一步' }).click()

    await expect(page.getByText('配置预览')).toBeVisible()
    await expect(page.getByText(agentName)).toBeVisible()
    await page.getByRole('button', { name: '保存草稿' }).click()

    await page.waitForURL(/\/agentx\/agent\/detail\/\d+$/)
    await page.waitForLoadState('networkidle')

    await expect(page.getByText('数字员工详情')).toBeVisible()
    await expect(page.getByText(agentName)).toBeVisible()
    await page.goto('/agentx/agent/list')
    await page.waitForLoadState('networkidle')
    await page.getByPlaceholder('请输入员工名称').fill(agentName)
    await page.waitForTimeout(700)
    const agentCard = page.locator('.agent-card', { hasText: agentName })
    await expect(agentCard).toBeVisible()
    await agentCard.click()

    await expect(page.getByText('数字员工详情')).toBeVisible()
    await expect(page.getByText(agentName)).toBeVisible()
    await expect(page.getByText('关联流程', { exact: true })).toBeVisible()
    await expect(page.getByText('能力配置', { exact: true })).toBeVisible()
    await expect(page.getByText('创建时间', { exact: true })).toBeVisible()
    await expect(page.getByText('最后更新时间', { exact: true })).toBeVisible()

    await page.getByRole('button', { name: '启用' }).click()
    await expect(page.getByText('激活')).toBeVisible()

    await page.getByRole('button', { name: '删除' }).click()
    await page.getByRole('button', { name: /确定|确 定/ }).click()
    await page.waitForURL('**/agentx/agent/list')
    await page.waitForLoadState('networkidle')
    await expect(page.getByText(agentName)).toHaveCount(0)
  })
})

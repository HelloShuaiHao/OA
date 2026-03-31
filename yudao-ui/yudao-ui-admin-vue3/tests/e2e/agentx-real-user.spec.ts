import { expect, test, type Page } from '@playwright/test'

const TENANT_ID = 1
const BACKEND_BASE_URL = 'http://127.0.0.1:48080'
const OPENFANG_BASE_URL = 'http://127.0.0.1:4201'

async function login(page: Page) {
  const loginResponse = await page.request.post(`${BACKEND_BASE_URL}/admin-api/system/auth/login`, {
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

  const infoResponse = await page.request.get(`${BACKEND_BASE_URL}/admin-api/system/auth/get-permission-info`, {
    headers: {
      Authorization: `Bearer ${loginBody.data.accessToken}`,
      'tenant-id': String(TENANT_ID)
    }
  })
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
  await page.waitForLoadState('domcontentloaded')
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

test('real user flow: sidebar -> list -> create publish -> detail -> openfang', async ({ page }) => {
  const suffix = Date.now().toString().slice(-8)
  const agentName = `真实验收员工${suffix}`

  await login(page)

  await page.goto('/agentx/agent/list')
  await page.waitForURL(/\/agentx\/agent\/list$/)

  await expect(page.getByRole('link', { name: '员工列表' })).toBeVisible()
  await expect(page.getByText('员工名称', { exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '创建数字员工' })).toBeVisible()

  await page.getByRole('button', { name: '创建数字员工' }).click()
  await page.waitForURL(/\/agentx\/agent\/create$/)
  await expect(page.getByText('5 步向导：创建数字员工')).toBeVisible()

  await fillBasicInfo(page, agentName, '真实用户路径发布验收')
  await selectFirstDept(page)
  await page.getByRole('button', { name: '下一步' }).click()

  await page.locator('.template-card').first().click()
  await page.getByRole('button', { name: '下一步' }).click()
  await page.getByRole('button', { name: '下一步' }).click()

  await expect(page.getByText('从流程库选择')).toBeVisible()
  await page.locator('.process-row .el-checkbox').first().click()
  await expect(page.getByText(/已选流程 1 个/)).toBeVisible()
  await page.getByRole('button', { name: '下一步' }).click()

  await expect(page.getByText('配置预览')).toBeVisible()
  await expect(page.getByText('保存草稿仅保存到 OA；点击“激活并发布”后才会同步到 OpenFang 并可用于渠道接入。')).toBeVisible()
  await page.getByRole('button', { name: '激活并发布' }).click()

  await page.waitForURL(/\/agentx\/agent\/detail\/\d+$/)
  await expect(page.getByText('数字员工详情')).toBeVisible()
  await expect(page.getByText(agentName)).toBeVisible()
  await expect(page.getByText('OpenFang 同步状态')).toBeVisible()
  await expect(page.getByText('成功', { exact: true })).toBeVisible({ timeout: 20000 })

  await page.goto('/agentx/agent/list')
  await page.waitForURL(/\/agentx\/agent\/list$/)
  await page.getByPlaceholder('请输入员工名称').fill(agentName)
  await page.waitForTimeout(800)
  const agentCard = page.locator('.agent-card', { hasText: agentName })
  await expect(agentCard).toBeVisible()
  await agentCard.getByRole('button', { name: '详情' }).click()
  await page.waitForURL(/\/agentx\/agent\/detail\/\d+$/)
  await expect(page.getByText(agentName)).toBeVisible()

  const openfangResponse = await page.request.get(`${OPENFANG_BASE_URL}/api/agents`)
  expect(openfangResponse.ok()).toBeTruthy()
  const openfangAgents = await openfangResponse.json()
  const matchedAgent = openfangAgents.find((item: Record<string, unknown>) => {
    return String(item.name || '').includes(agentName)
  })
  expect(matchedAgent, `OpenFang 未找到 ${agentName}`).toBeTruthy()
})

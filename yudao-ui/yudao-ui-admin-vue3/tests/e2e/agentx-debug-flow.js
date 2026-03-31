const { chromium } = require('@playwright/test')

async function main() {
  const log = (...args) => console.log(new Date().toISOString(), ...args)
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage()
  const tenantId = 1

  try {
    page.on('response', async (response) => {
      if (response.url().includes('/admin-api/agentx/agent/get?id=')) {
        try {
          const body = await response.json()
          log('detail api response', JSON.stringify(body.data))
        } catch (error) {
          log('detail api response parse failed', error.message)
        }
      }
    })

    log('login start')
    const loginResp = await page.request.post('http://127.0.0.1:48080/admin-api/system/auth/login', {
      headers: {
        'Content-Type': 'application/json',
        'tenant-id': String(tenantId)
      },
      data: {
        username: 'e1221805',
        password: 'admin123456',
        tenantName: '芋道源码',
        captchaVerification: ''
      }
    })
    const loginBody = await loginResp.json()
    log('login code', loginBody.code)

    const infoResp = await page.request.get('http://127.0.0.1:48080/admin-api/system/auth/get-permission-info', {
      headers: {
        Authorization: `Bearer ${loginBody.data.accessToken}`,
        'tenant-id': String(tenantId)
      }
    })
    const infoBody = await infoResp.json()

    await page.addInitScript(
      ({ accessToken, refreshToken, currentTenantId, userInfo, roleRouters }) => {
        const encode = (value) => {
          const now = Date.now()
          return JSON.stringify({ c: now, e: now + 24 * 60 * 60 * 1000, v: JSON.stringify(value) })
        }
        localStorage.setItem('ACCESS_TOKEN', encode(accessToken))
        localStorage.setItem('REFRESH_TOKEN', encode(refreshToken))
        localStorage.setItem('tenantId', encode(currentTenantId))
        localStorage.setItem('user', encode(userInfo))
        localStorage.setItem('roleRouters', encode(roleRouters))
      },
      {
        accessToken: loginBody.data.accessToken,
        refreshToken: loginBody.data.refreshToken,
        currentTenantId: tenantId,
        userInfo: infoBody.data,
        roleRouters: infoBody.data.menus
      }
    )

    await page.goto('http://127.0.0.1:3000/agentx/agent/list', { waitUntil: 'domcontentloaded' })
    await page.waitForTimeout(1500)
    const agentName = `真实验收员工${Date.now().toString().slice(-8)}`
    log('at list', page.url(), agentName)

    await page.getByRole('button', { name: '创建数字员工' }).click()
    log('clicked create')
    await page.waitForURL(/\/agentx\/agent\/create$/)

    const nameInput = page.locator('.el-form-item').filter({ hasText: '员工名称' }).locator('input').first()
    const descriptionInput = page.locator('.el-form-item').filter({ hasText: '员工描述' }).locator('textarea').first()

    await nameInput.fill(agentName)
    await Promise.all([
      page.waitForResponse(
        (response) => response.url().includes('/agentx/agent/check-name') && response.request().method() === 'GET',
        { timeout: 15000 }
      ),
      nameInput.evaluate((element) => element.blur())
    ])
    log('name checked')

    await descriptionInput.fill('真实用户路径发布验收')
    const deptSelect = page.locator('.el-form-item').filter({ hasText: '所属部门' }).locator('.el-select').first()
    await deptSelect.click()
    await page.locator('[role="option"]:visible').filter({ hasText: /\S/ }).first().click()
    log('dept selected')

    await page.getByRole('button', { name: '下一步' }).click()
    log('step 1 done')
    await page.locator('.template-card').first().click()
    await page.getByRole('button', { name: '下一步' }).click()
    log('step 2 done')
    await page.getByRole('button', { name: '下一步' }).click()
    log('step 3 done')
    await page.locator('.process-row .el-checkbox').first().click()
    log('process selected')
    await page.getByRole('button', { name: '下一步' }).click()
    log('step 4 done')
    await page.getByRole('button', { name: '激活并发布' }).click()
    log('clicked publish')

    await page.waitForURL(/\/agentx\/agent\/detail\/\d+$/, { timeout: 30000 })
    log('arrived detail', page.url())
    await page.waitForTimeout(2000)

    const syncSuccessVisible = await page.getByText('成功', { exact: true }).isVisible().catch(() => false)
    const processVisible = await page.getByText('OA 请假', { exact: true }).isVisible().catch(() => false)
    if (!syncSuccessVisible) {
      throw new Error('详情页未显示 OpenFang 同步成功')
    }
    if (!processVisible) {
      throw new Error('详情页未显示关联流程 OA 请假')
    }
    log('detail checks passed')

    await page.goto('http://127.0.0.1:3000/agentx/agent/list', { waitUntil: 'domcontentloaded' })
    await page.getByPlaceholder('请输入员工名称').fill(agentName)
    await page.waitForTimeout(800)
    const agentCard = page.locator('.agent-card', { hasText: agentName })
    if (!(await agentCard.isVisible().catch(() => false))) {
      throw new Error(`列表页未找到 ${agentName}`)
    }
    await agentCard.getByRole('button', { name: '详情' }).click()
    await page.waitForURL(/\/agentx\/agent\/detail\/\d+$/, { timeout: 30000 })
    log('list search checks passed')

    const response = await fetch('http://127.0.0.1:4201/api/agents')
    const agents = await response.json()
    const matchedAgent = agents.find((item) => String(item.name || '').includes(agentName))
    if (!matchedAgent) {
      throw new Error(`OpenFang 未找到 ${agentName}`)
    }
    log('openfang matched', JSON.stringify(matchedAgent))
  } catch (error) {
    console.error('SCRIPT_FAILED', error)
    await page.screenshot({ path: '/tmp/agentx-debug-fail.png', fullPage: true }).catch(() => undefined)
  } finally {
    await browser.close()
  }
}

main()

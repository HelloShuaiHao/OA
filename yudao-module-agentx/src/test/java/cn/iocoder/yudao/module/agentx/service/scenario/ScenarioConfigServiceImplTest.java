package cn.iocoder.yudao.module.agentx.service.scenario;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigSaveReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.scenario.AgentxScenarioConfigMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScenarioConfigServiceImplTest {

    @Test
    void shouldSaveThenQueryScenarioConfig() {
        Map<Long, AgentxScenarioConfigDO> store = new HashMap<>();
        AtomicLong idGen = new AtomicLong(1);
        ScenarioConfigServiceImpl service = createService(store, idGen);

        AgentxScenarioConfigSaveReqVO createReqVO = new AgentxScenarioConfigSaveReqVO();
        createReqVO.setScenarioCode("oa.leave.approval");
        createReqVO.setScenarioName("请假审批助手");
        createReqVO.setOpenfangWorkflowId("leave-approval-assistant");
        createReqVO.setWorkflowVersion("1.0.0");
        createReqVO.setEnabled(1);
        createReqVO.setConfig("{\"autoApproveMaxDays\":2}");

        Long id = service.createScenarioConfig(createReqVO);
        AgentxScenarioConfigDO config = service.getScenarioConfig(id);

        assertNotNull(config);
        assertEquals("oa.leave.approval", config.getScenarioCode());
        assertEquals("leave-approval-assistant", config.getOpenfangWorkflowId());
        assertEquals("1.0.0", config.getWorkflowVersion());
    }

    @Test
    void shouldRejectDuplicatedScenarioCode() {
        Map<Long, AgentxScenarioConfigDO> store = new HashMap<>();
        AtomicLong idGen = new AtomicLong(2);
        AgentxScenarioConfigDO existing = new AgentxScenarioConfigDO();
        existing.setId(1L);
        existing.setScenarioCode("oa.leave.approval");
        store.put(1L, existing);

        ScenarioConfigServiceImpl service = createService(store, idGen);

        AgentxScenarioConfigSaveReqVO createReqVO = new AgentxScenarioConfigSaveReqVO();
        createReqVO.setScenarioCode("oa.leave.approval");
        createReqVO.setScenarioName("重复配置");
        createReqVO.setOpenfangWorkflowId("leave-approval-assistant");
        createReqVO.setEnabled(1);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.createScenarioConfig(createReqVO));

        assertEquals(1_024_003_002, exception.getCode());
    }

    private ScenarioConfigServiceImpl createService(Map<Long, AgentxScenarioConfigDO> store, AtomicLong idGen) {
        AgentxScenarioConfigMapper mapper = (AgentxScenarioConfigMapper) Proxy.newProxyInstance(
                AgentxScenarioConfigMapper.class.getClassLoader(),
                new Class<?>[] { AgentxScenarioConfigMapper.class },
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("insert".equals(name)) {
                        AgentxScenarioConfigDO config = (AgentxScenarioConfigDO) args[0];
                        long id = idGen.getAndIncrement();
                        config.setId(id);
                        store.put(id, cloneConfig(config));
                        return 1;
                    }
                    if ("updateById".equals(name)) {
                        AgentxScenarioConfigDO update = (AgentxScenarioConfigDO) args[0];
                        AgentxScenarioConfigDO existing = store.get(update.getId());
                        if (existing != null) {
                            if (update.getScenarioCode() != null) existing.setScenarioCode(update.getScenarioCode());
                            if (update.getScenarioName() != null) existing.setScenarioName(update.getScenarioName());
                            if (update.getOpenfangWorkflowId() != null) existing.setOpenfangWorkflowId(update.getOpenfangWorkflowId());
                            if (update.getWorkflowVersion() != null) existing.setWorkflowVersion(update.getWorkflowVersion());
                            if (update.getEnabled() != null) existing.setEnabled(update.getEnabled());
                            if (update.getConfig() != null) existing.setConfig(update.getConfig());
                        }
                        return 1;
                    }
                    if ("deleteById".equals(name)) {
                        store.remove((Long) args[0]);
                        return 1;
                    }
                    if ("selectById".equals(name)) {
                        return store.get((Long) args[0]);
                    }
                    if ("selectByScenarioCode".equals(name)) {
                        String scenarioCode = (String) args[0];
                        return store.values().stream()
                                .filter(item -> scenarioCode.equals(item.getScenarioCode()))
                                .findFirst()
                                .orElse(null);
                    }
                    if ("selectPage".equals(name)) {
                        throw new UnsupportedOperationException("not needed in this test");
                    }
                    return null;
                });

        ScenarioConfigServiceImpl service = new ScenarioConfigServiceImpl();
        ReflectionTestUtils.setField(service, "scenarioConfigMapper", mapper);
        return service;
    }

    private AgentxScenarioConfigDO cloneConfig(AgentxScenarioConfigDO source) {
        AgentxScenarioConfigDO target = new AgentxScenarioConfigDO();
        target.setId(source.getId());
        target.setScenarioCode(source.getScenarioCode());
        target.setScenarioName(source.getScenarioName());
        target.setOpenfangWorkflowId(source.getOpenfangWorkflowId());
        target.setWorkflowVersion(source.getWorkflowVersion());
        target.setEnabled(source.getEnabled());
        target.setConfig(source.getConfig());
        return target;
    }

}

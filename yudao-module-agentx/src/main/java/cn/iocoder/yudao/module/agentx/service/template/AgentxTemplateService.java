package cn.iocoder.yudao.module.agentx.service.template;

import cn.iocoder.yudao.module.agentx.controller.admin.template.vo.AgentxTemplateRespVO;

import java.util.List;

public interface AgentxTemplateService {

    List<AgentxTemplateRespVO> getTemplateList();

    AgentxTemplateRespVO getTemplate(String templateType);

}

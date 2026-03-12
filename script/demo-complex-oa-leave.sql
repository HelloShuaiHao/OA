SET NAMES utf8mb4;

SET @bpmn_xml = '<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
             targetNamespace="http://flowable.org/processdef">
  <process id="oa_leave" name="OA 请假" isExecutable="true">
    <startEvent id="startEvent" name="提交申请"/>
    <userTask id="leaderReviewTask"
              name="初审"
              flowable:candidateStrategy="30"
              flowable:candidateParam="145"/>
    <exclusiveGateway id="typeGateway" name="按请假类型分支"/>
    <userTask id="medicalReviewTask"
              name="病假补充确认"
              flowable:candidateStrategy="30"
              flowable:candidateParam="145"/>
    <userTask id="marriageReviewTask"
              name="婚假专项确认"
              flowable:candidateStrategy="30"
              flowable:candidateParam="145"/>
    <parallelGateway id="parallelSplitGateway" name="并行处理开始"/>
    <userTask id="financeRecordTask"
              name="备案检查"
              flowable:candidateStrategy="30"
              flowable:candidateParam="145"/>
    <userTask id="archiveTask"
              name="归档确认"
              flowable:candidateStrategy="30"
              flowable:candidateParam="145"/>
    <parallelGateway id="parallelJoinGateway" name="并行处理结束"/>
    <userTask id="returnConfirmTask"
              name="最终确认"
              flowable:candidateStrategy="30"
              flowable:candidateParam="145"/>
    <endEvent id="endEvent" name="结束"/>

    <sequenceFlow id="flow_start_leader" sourceRef="startEvent" targetRef="leaderReviewTask"/>
    <sequenceFlow id="flow_leader_gateway" sourceRef="leaderReviewTask" targetRef="typeGateway"/>
    <sequenceFlow id="flow_gateway_medical" sourceRef="typeGateway" targetRef="medicalReviewTask">
      <conditionExpression xsi:type="tFormalExpression"><![CDATA[${type == 1}]]></conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="flow_gateway_marriage" sourceRef="typeGateway" targetRef="marriageReviewTask">
      <conditionExpression xsi:type="tFormalExpression"><![CDATA[${type == 3}]]></conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="flow_gateway_default" sourceRef="typeGateway" targetRef="parallelSplitGateway"/>
    <sequenceFlow id="flow_medical_parallel" sourceRef="medicalReviewTask" targetRef="parallelSplitGateway"/>
    <sequenceFlow id="flow_marriage_parallel" sourceRef="marriageReviewTask" targetRef="parallelSplitGateway"/>
    <sequenceFlow id="flow_parallel_finance" sourceRef="parallelSplitGateway" targetRef="financeRecordTask"/>
    <sequenceFlow id="flow_parallel_archive" sourceRef="parallelSplitGateway" targetRef="archiveTask"/>
    <sequenceFlow id="flow_finance_join" sourceRef="financeRecordTask" targetRef="parallelJoinGateway"/>
    <sequenceFlow id="flow_archive_join" sourceRef="archiveTask" targetRef="parallelJoinGateway"/>
    <sequenceFlow id="flow_join_confirm" sourceRef="parallelJoinGateway" targetRef="returnConfirmTask"/>
    <sequenceFlow id="flow_confirm_end" sourceRef="returnConfirmTask" targetRef="endEvent"/>
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_oa_leave">
    <bpmndi:BPMNPlane id="BPMNPlane_oa_leave" bpmnElement="oa_leave">
      <bpmndi:BPMNShape id="shape_startEvent" bpmnElement="startEvent">
        <dc:Bounds x="120" y="222" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_leaderReviewTask" bpmnElement="leaderReviewTask">
        <dc:Bounds x="220" y="200" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_typeGateway" bpmnElement="typeGateway" isMarkerVisible="true">
        <dc:Bounds x="385" y="215" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_medicalReviewTask" bpmnElement="medicalReviewTask">
        <dc:Bounds x="500" y="90" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_marriageReviewTask" bpmnElement="marriageReviewTask">
        <dc:Bounds x="500" y="330" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_parallelSplitGateway" bpmnElement="parallelSplitGateway" isMarkerVisible="true">
        <dc:Bounds x="675" y="215" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_financeRecordTask" bpmnElement="financeRecordTask">
        <dc:Bounds x="790" y="120" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_archiveTask" bpmnElement="archiveTask">
        <dc:Bounds x="790" y="310" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_parallelJoinGateway" bpmnElement="parallelJoinGateway" isMarkerVisible="true">
        <dc:Bounds x="955" y="215" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_returnConfirmTask" bpmnElement="returnConfirmTask">
        <dc:Bounds x="1070" y="200" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="shape_endEvent" bpmnElement="endEvent">
        <dc:Bounds x="1235" y="222" width="36" height="36"/>
      </bpmndi:BPMNShape>

      <bpmndi:BPMNEdge id="edge_flow_start_leader" bpmnElement="flow_start_leader">
        <di:waypoint x="156" y="240"/>
        <di:waypoint x="220" y="240"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_leader_gateway" bpmnElement="flow_leader_gateway">
        <di:waypoint x="320" y="240"/>
        <di:waypoint x="385" y="240"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_gateway_medical" bpmnElement="flow_gateway_medical">
        <di:waypoint x="410" y="215"/>
        <di:waypoint x="410" y="130"/>
        <di:waypoint x="500" y="130"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_gateway_marriage" bpmnElement="flow_gateway_marriage">
        <di:waypoint x="410" y="265"/>
        <di:waypoint x="410" y="370"/>
        <di:waypoint x="500" y="370"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_gateway_default" bpmnElement="flow_gateway_default">
        <di:waypoint x="435" y="240"/>
        <di:waypoint x="675" y="240"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_medical_parallel" bpmnElement="flow_medical_parallel">
        <di:waypoint x="600" y="130"/>
        <di:waypoint x="700" y="130"/>
        <di:waypoint x="700" y="215"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_marriage_parallel" bpmnElement="flow_marriage_parallel">
        <di:waypoint x="600" y="370"/>
        <di:waypoint x="700" y="370"/>
        <di:waypoint x="700" y="265"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_parallel_finance" bpmnElement="flow_parallel_finance">
        <di:waypoint x="725" y="240"/>
        <di:waypoint x="790" y="160"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_parallel_archive" bpmnElement="flow_parallel_archive">
        <di:waypoint x="725" y="240"/>
        <di:waypoint x="790" y="350"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_finance_join" bpmnElement="flow_finance_join">
        <di:waypoint x="890" y="160"/>
        <di:waypoint x="980" y="160"/>
        <di:waypoint x="980" y="215"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_archive_join" bpmnElement="flow_archive_join">
        <di:waypoint x="890" y="350"/>
        <di:waypoint x="980" y="350"/>
        <di:waypoint x="980" y="265"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_join_confirm" bpmnElement="flow_join_confirm">
        <di:waypoint x="1005" y="240"/>
        <di:waypoint x="1070" y="240"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="edge_flow_confirm_end" bpmnElement="flow_confirm_end">
        <di:waypoint x="1170" y="240"/>
        <di:waypoint x="1235" y="240"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>';

UPDATE act_ge_bytearray
SET BYTES_ = CONVERT(@bpmn_xml USING utf8mb4)
WHERE ID_ IN ('14e99e9f-1def-11f1-a228-1abf1e159bc8', '14fcd881-1def-11f1-a228-1abf1e159bc8');

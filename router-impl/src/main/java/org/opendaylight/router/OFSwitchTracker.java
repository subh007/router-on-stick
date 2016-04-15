/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.router;

import java.util.Arrays;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OFSwitchTracker implements DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(RouterProvider.class);
    SalFlowService salFlowService;

    public OFSwitchTracker(SalFlowService salFlowService) {
        this.salFlowService = salFlowService;
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {

        Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();

        // get the switch ID and install the default flow rule
        for(Map.Entry<InstanceIdentifier<?>, DataObject> elem : createdData.entrySet()) {

            FlowCapableNode flowCapableNode = ((Node) elem.getValue()).getAugmentation(FlowCapableNode.class);
            if (flowCapableNode!= null) {
                LOG.info("identified the OF NODE : {}", elem.getKey());
                InstanceIdentifier<Node> nodeIID = (InstanceIdentifier<Node>) elem.getKey();
                InstanceIdentifier<Table> tableRef = getTableInstanceIdentifier(nodeIID);
                InstanceIdentifier<Flow> flowRef = getFlowInstanceId(tableRef);

                installDefaultFlowRule(nodeIID,
                        tableRef,
                        flowRef,
                        getDefaultPuntToControllerFlow());
            }
        }
    }

    private InstanceIdentifier<Table> getTableInstanceIdentifier(InstanceIdentifier<Node> nodeIID) {

        return nodeIID.augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey((short) 0));
    }

    private InstanceIdentifier<Flow> getFlowInstanceId(InstanceIdentifier<Table> tableId) {
        // generate unique flow key
        FlowId flowId = new FlowId("router-on-stick");
        FlowKey flowKey = new FlowKey(flowId);
        return tableId.builder().child(Flow.class, flowKey).build();
    }

    public void installDefaultFlowRule(InstanceIdentifier<Node> nodRef,
            InstanceIdentifier<Table> tableRef,
            InstanceIdentifier<Flow> flowRef,
            Flow flow) {

        // set the required info
        AddFlowInputBuilder inputBuilder = new AddFlowInputBuilder(flow);
        inputBuilder.setNode(new NodeRef(nodRef));
        inputBuilder.setFlowTable(new FlowTableRef(tableRef));
        inputBuilder.setFlowRef(new FlowRef(flowRef));

        // TODO: it returns the future add code for handling it
        // install the flow rule
        salFlowService.addFlow(inputBuilder.build());
    }

    public static Flow getDefaultPuntToControllerFlow() {

        // Create Action to forward the packet to controller
        ActionBuilder actBuilder = new ActionBuilder();
        actBuilder.setAction(
                new OutputActionCaseBuilder()
                .setOutputAction(
                        new OutputActionBuilder()
                        .setOutputNodeConnector(
                                new Uri(OutputPortValues.CONTROLLER.toString()))
                        .build())
                .build())
        .setOrder(0);

        // Create instruction
        InstructionBuilder instBuilder = new InstructionBuilder();
        instBuilder.setInstruction(
                new ApplyActionsCaseBuilder()
                .setApplyActions(
                        new ApplyActionsBuilder()
                        .setAction(Arrays.asList(actBuilder.build()))
                        .build())
                .build())
        .setOrder(0);

        // create match field
        MatchBuilder matchBuilder = new MatchBuilder();

        // create flow
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setBufferId(0xffffffffL)
        .setInstructions(
                new InstructionsBuilder()
                .setInstruction(
                        Arrays.asList(instBuilder.build()))
                .build()
                )
        .setTableId((short)0)
        .setPriority(0)
        .setMatch(matchBuilder.build());

        return flowBuilder.build();
    }

    public void createAndInstallLearingFlowRule(Ipv4Address src, Ipv4Address dest,
            int sVlanID, int dVlanID, Node node, NodeConnector inport,
            NodeConnector outport) {

        InstanceIdentifier<Node> nodeIID = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(node.getKey()));
        InstanceIdentifier<Table> tableIID = getTableInstanceIdentifier(nodeIID);
        InstanceIdentifier<Flow> flowIID = getFlowInstanceId(tableIID);

        Flow flow = getLearningFlow(src, dest,sVlanID, dVlanID, node, inport, outport);

        installDefaultFlowRule(nodeIID, tableIID, flowIID, flow);
    }
    static Flow getLearningFlow(Ipv4Address src, Ipv4Address dest,
            int sVlanID, int dVlanID, Node node, NodeConnector inport,
            NodeConnector outport){

        FlowBuilder flowBuilder = new FlowBuilder();

        // create match criteria
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setInPort(new NodeConnectorId(inport.getId()));

        matchBuilder.setEthernetMatch(
                new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder()
                        .setType(new EtherType(new Long(0x0800)))
                        .build())
                .build());
        matchBuilder.setLayer3Match(
                new Ipv4MatchBuilder()
                .setIpv4Source(
                        new Ipv4Prefix(src.getValue() + "/32"))
                .setIpv4Destination(
                        new Ipv4Prefix(dest.getValue() + "/32"))
                .build()
                );
        matchBuilder.setVlanMatch(
                new VlanMatchBuilder()
                .setVlanId(
                        new VlanIdBuilder()
                        .setVlanIdPresent(true)
                        .setVlanId(new VlanId(new Integer(sVlanID)))
                        .build())
                .build()
                );

        // create actions
        ActionBuilder rewirteVlanID = new ActionBuilder();
        rewirteVlanID.setAction(
                new SetVlanIdActionCaseBuilder()
                .setSetVlanIdAction(new SetVlanIdActionBuilder()
                        .setVlanId(new VlanId(new Integer(dVlanID)))
                        .build()
                        )
                .build())
        .setOrder(0);

        ActionBuilder outputActionBuilder = new ActionBuilder();
        outputActionBuilder.setAction(
                new OutputActionCaseBuilder()
                .setOutputAction(new OutputActionBuilder()
                        .setOutputNodeConnector(
                                new Uri(outport.getId())
                                )
                        .build())
                .build())
        .setOrder(1);

        // create Instruction
        InstructionBuilder instructionBuidler = new InstructionBuilder();
        instructionBuidler.setInstruction(
                new ApplyActionsCaseBuilder()
                .setApplyActions(
                        new ApplyActionsBuilder()
                        .setAction(Arrays.asList(rewirteVlanID.build(), outputActionBuilder.build()))
                        .build())
                .build());

        // create flow
        flowBuilder.setInstructions(
                new InstructionsBuilder()
                .setInstruction(Arrays.asList(instructionBuidler.build()))
                .build())
        .setBarrier(true)
        .setIdleTimeout(new Integer(30))
        .setBufferId(0xffffffffL)
        .setMatch(matchBuilder.build())
        .setPriority(100)
        .setTableId((short) 0);

        return flowBuilder.build();
    }
}

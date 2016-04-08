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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.actions._case.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
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
            }
        }
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
                new WriteActionsCaseBuilder()
                .setWriteActions(
                        new WriteActionsBuilder()
                        .setAction(Arrays.asList(actBuilder.build()))
                        .build())
                .build())
        .setOrder(0);

        // create flow
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setBufferId(new Long(-1))
        .setInstructions(
                new InstructionsBuilder()
                .setInstruction(
                        Arrays.asList(instBuilder.build()))
                .build()
                )
        .setTableId((short)0);

        return flowBuilder.build();
    }

}

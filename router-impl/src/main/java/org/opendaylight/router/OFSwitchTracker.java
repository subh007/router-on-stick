/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.router;

import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OFSwitchTracker implements DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(RouterProvider.class);

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

    public void installDefaultFlowRule(Node node) {

    }

}

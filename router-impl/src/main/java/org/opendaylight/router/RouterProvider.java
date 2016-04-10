/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.router;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.proxyarp.ProxyArp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RouterProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RouterProvider.class);


    private ListenerRegistration<NotificationListener> listener;
    private ListenerRegistration<DataChangeListener> dataChangeListener;

    private DataBroker dataBroker;
    private SalFlowService salFlowService;
    private ProxyArp proxyArp;

    private static String ROUTER_MAC_ADDRESS="10:20:30:40:50:60";
    private static InstanceIdentifier<Node> NODE_IID = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();

    public RouterProvider(NotificationProviderService notificationProviderService, DataBroker broker) {
        proxyArp = new ProxyArp();
        listener = notificationProviderService.registerNotificationListener(proxyArp);
        dataBroker = broker;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("HelloProvider Session Initiated");

        salFlowService = session.getRpcService(SalFlowService.class);
        proxyArp.setPacketProcessingService(session.getRpcService(PacketProcessingService.class));

        dataChangeListener = dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                NODE_IID,
                new OFSwitchTracker(salFlowService),
                DataChangeScope.BASE);
    }

    @Override
    public void close() throws Exception {
        this.listener.close();
        dataChangeListener.close();

        listener = null;
        dataChangeListener = null;
        salFlowService = null;
    }
}



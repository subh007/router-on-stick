/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.router;

import java.util.Arrays;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.proxyarp.ProxyArp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.Subinterfaces;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.subinterfaces.SubInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.subinterfaces.SubInterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.subinterfaces.SubInterfaceKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;


public class RouterProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RouterProvider.class);


    private ListenerRegistration<NotificationListener> listener;
    private ListenerRegistration<DataChangeListener> dataChangeListener;
    private ListenerRegistration<DataChangeListener> dataListenerForUserData;

    private DataBroker dataBroker;
    private SalFlowService salFlowService;
    private ProxyArp proxyArp;

    private static String ROUTER_MAC_ADDRESS="10:20:30:40:50:60";
    private static InstanceIdentifier<Node> NODE_IID = InstanceIdentifier.builder(Nodes.class).child(Node.class).build();

    public RouterProvider(NotificationProviderService notificationProviderService, DataBroker broker) {
        proxyArp = new ProxyArp();
        listener = notificationProviderService.registerNotificationListener(proxyArp);
        dataBroker = broker;

        InstanceIdentifier<SubInterface> iid = InstanceIdentifier.create(Subinterfaces.class).child(SubInterface.class);
        UserDataHandler userDataHandler = new UserDataHandler();
        userDataHandler.setDataBroker(dataBroker);

        dataListenerForUserData = dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                iid,
                userDataHandler,
                DataChangeScope.BASE);
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("HelloProvider Session Initiated");

        salFlowService = session.getRpcService(SalFlowService.class);
        proxyArp.setPacketProcessingService(session.getRpcService(PacketProcessingService.class));
        OFSwitchTracker ofSwitchTracker = new OFSwitchTracker(salFlowService);

        dataChangeListener = dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL,
                NODE_IID,
                ofSwitchTracker,
                DataChangeScope.BASE);

        populateStaticData();
        proxyArp.setOFSwitchTracker(ofSwitchTracker);
        proxyArp.setDataBroker(dataBroker);

    }

    @Override
    public void close() throws Exception {
        this.listener.close();
        dataChangeListener.close();
        dataListenerForUserData.close();

        listener = null;
        dataChangeListener = null;
        salFlowService = null;
        dataListenerForUserData = null;
    }

    public void populateStaticData() {

        // subinterface 1
        SubInterface subinterface1 = new SubInterfaceBuilder()
                .setInterface("veth100")
                .setVlan(100)
                .setIp("1.0.0.0")
                .setPort(Arrays.asList(new Integer(1)))
                .setMac("00:00:00:00:00:01").build();
        writeSubinteface(subinterface1,
                getIdentifier(subinterface1.getInterface()));

        // subinterface 2
        SubInterface subinterface2 = new SubInterfaceBuilder()
                .setInterface("veth200")
                .setVlan(200)
                .setIp("2.0.0.0")
                .setPort(Arrays.asList(new Integer(2)))
                .setMac("00:00:00:00:00:02").build();
        //        writeSubinteface(subinterface2,
        //                getIdentifier(subinterface2.getInterface()));
    }

    private void writeSubinteface(SubInterface subinteface,
            InstanceIdentifier<SubInterface> subinterfaceIID) {
        WriteTransaction wtx = dataBroker.newWriteOnlyTransaction();

        wtx.merge(LogicalDatastoreType.OPERATIONAL,
                subinterfaceIID,
                subinteface);
        CheckedFuture<Void, TransactionCommitFailedException> future = wtx.submit();
        Futures.addCallback(future, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                LOG.info("write is successful");

            }

            @Override
            public void onFailure(Throwable t) {
                LOG.info("write failed");

            }
        });
    }

    private InstanceIdentifier<SubInterface> getIdentifier(String interf) {
        return InstanceIdentifier.create(Subinterfaces.class)
                .child(SubInterface.class,
                        new SubInterfaceKey(interf)
                        );
    }
}



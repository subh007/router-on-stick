/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.router;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterProvider implements BindingAwareProvider, AutoCloseable, PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(RouterProvider.class);
    private ListenerRegistration<NotificationListener> listener;

    public RouterProvider(NotificationProviderService notificationProviderService) {
        listener = notificationProviderService.registerNotificationListener(this);
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("HelloProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        this.listener.close();
    }

    @Override
    public void onPacketReceived(PacketReceived packet) {
        LOG.debug("reveived the packet :" + packet.toString());
    }

}

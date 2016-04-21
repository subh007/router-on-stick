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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataHandler implements DataChangeListener{

    private static final Logger LOG = LoggerFactory.getLogger(UserDataHandler.class);

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> dataChangeNotification) {
        handleCreatedData(dataChangeNotification.getCreatedData());
    }

    public void handleCreatedData(Map<InstanceIdentifier<?>, DataObject> createdData) {
        LOG.info("some data got created : {}", createdData);
    }

    public void handleUpdatedData(Map<InstanceIdentifier<?>, DataObject> updatedData) {
        LOG.info("some data got for update: {}", updatedData);
    }

    public void handleDeletedData(Map<InstanceIdentifier<?>, DataObject> deletedData) {
        LOG.info("some data got for deletion: {}", deletedData);
    }

}

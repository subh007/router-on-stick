/*
 * Copyright (c) 2015 Yoyodyne, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.router;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.rev150105.subinterfaces.SubInterface;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class UserDataHandler implements DataChangeListener{

    private static final Logger LOG = LoggerFactory.getLogger(UserDataHandler.class);
    private DataBroker dataBroker;

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> dataChangeNotification) {
        handleCreatedData(dataChangeNotification.getCreatedData());
        handleUpdatedData(dataChangeNotification.getUpdatedData());
        handleDeletedData(dataChangeNotification.getRemovedPaths());
    }

    public void handleCreatedData(Map<InstanceIdentifier<?>, DataObject> createdData) {
        LOG.info("some data got created : {}", createdData);
        createOrUpdateOprData(createdData);
    }

    public void handleUpdatedData(Map<InstanceIdentifier<?>, DataObject> updatedData) {
        LOG.info("some data got for update: {}", updatedData);
        createOrUpdateOprData(updatedData);
    }

    public void handleDeletedData(Set<InstanceIdentifier<?>> setRemovedpath) {
        LOG.info("some data got for deletion: {}", setRemovedpath);
        setRemovedpath.forEach(p -> deleteDataFromOprDataStore(p));
    }

    private void createOrUpdateOprData(Map<InstanceIdentifier<?>, DataObject> createdOrUpdatedData) {
        for(Entry<InstanceIdentifier<?>, DataObject> entry: createdOrUpdatedData.entrySet()) {
            InstanceIdentifier<SubInterface> subInterfaceIID = (InstanceIdentifier<SubInterface>) entry.getKey();
            SubInterface subInterface = (SubInterface) entry.getValue();

            // write the info to the operational data store
            writeDataToOperationDataStore(subInterfaceIID, subInterface);
        }
    }
    public void writeDataToOperationDataStore(InstanceIdentifier<SubInterface> subInterfaceIID, SubInterface subInterface) {
        WriteTransaction wtx = dataBroker.newWriteOnlyTransaction();
        wtx.merge(LogicalDatastoreType.OPERATIONAL, subInterfaceIID, subInterface);
        CheckedFuture<Void, TransactionCommitFailedException> future = wtx.submit();

        Futures.addCallback(future, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                LOG.info("wrote successfully to operational data store.");
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.info("failed to write to operational datastore.");
            }
        });
    }

    public void deleteDataFromOprDataStore(InstanceIdentifier<?> iid) {
        WriteTransaction wtx = dataBroker.newWriteOnlyTransaction();
        wtx.delete(LogicalDatastoreType.OPERATIONAL, iid);
        CheckedFuture<Void, TransactionCommitFailedException> future = wtx.submit();

        Futures.addCallback(future, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                LOG.info("deleted the subinterface");
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.info("failed to delete the subinterface");
            }
        });
    }
}

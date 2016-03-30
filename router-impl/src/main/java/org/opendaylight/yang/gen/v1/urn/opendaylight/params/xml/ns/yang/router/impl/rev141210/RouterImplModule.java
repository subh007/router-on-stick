package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.impl.rev141210;

import org.opendaylight.router.RouterProvider;

public class RouterImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.impl.rev141210.AbstractRouterImplModule {
    public RouterImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public RouterImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.router.impl.rev141210.RouterImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        RouterProvider provider = new RouterProvider(getNotificationServiceDependency());
        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}

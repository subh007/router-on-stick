/*
* Generated file
*
* Generated from: yang module name: hello-impl yang module local name: hello-impl
* Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
* Generated at: Wed Mar 02 01:16:37 IST 2016
*
* Do not modify this file unless it is present under src/main directory
*/
package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210;
@org.opendaylight.yangtools.yang.binding.annotations.ModuleQName(namespace = "urn:opendaylight:params:xml:ns:yang:hello:impl", name = "hello-impl", revision = "2014-12-10")

public abstract class AbstractHelloImplModuleFactory implements org.opendaylight.controller.config.spi.ModuleFactory {
    public static final java.lang.String NAME = "hello-impl";

    private static final java.util.Set<Class<? extends org.opendaylight.controller.config.api.annotations.AbstractServiceInterface>> serviceIfcs;

    @Override
    public final String getImplementationName() {
        return NAME;
    }

    static {
        serviceIfcs = java.util.Collections.emptySet();
    }

    @Override
    public final boolean isModuleImplementingServiceInterface(Class<? extends org.opendaylight.controller.config.api.annotations.AbstractServiceInterface> serviceInterface) {
        for (Class<?> ifc: serviceIfcs) {
            if (serviceInterface.isAssignableFrom(ifc)){
                return true;
            }
        }
        return false;
    }

    @Override
    public java.util.Set<Class<? extends org.opendaylight.controller.config.api.annotations.AbstractServiceInterface>> getImplementedServiceIntefaces() {
        return serviceIfcs;
    }

    @Override
    public org.opendaylight.controller.config.spi.Module createModule(String instanceName, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.osgi.framework.BundleContext bundleContext) {
        return instantiateModule(instanceName, dependencyResolver, bundleContext);
    }

    @Override
    public org.opendaylight.controller.config.spi.Module createModule(String instanceName, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.api.DynamicMBeanWithInstance old, org.osgi.framework.BundleContext bundleContext) throws Exception {
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule oldModule;
        try {
            oldModule = (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule) old.getModule();
        } catch(Exception e) {
            return handleChangedClass(old);
        }
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule module = instantiateModule(instanceName, dependencyResolver, oldModule, old.getInstance(), bundleContext);
        module.setBroker(oldModule.getBroker());

        return module;
    }

    public org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule instantiateModule(String instanceName, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule oldModule, java.lang.AutoCloseable oldInstance, org.osgi.framework.BundleContext bundleContext) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule(new org.opendaylight.controller.config.api.ModuleIdentifier(NAME, instanceName), dependencyResolver, oldModule, oldInstance);
    }

    public org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule instantiateModule(String instanceName, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.osgi.framework.BundleContext bundleContext) {
        return new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule(new org.opendaylight.controller.config.api.ModuleIdentifier(NAME, instanceName), dependencyResolver);
    }

    public org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule handleChangedClass(org.opendaylight.controller.config.api.DynamicMBeanWithInstance old) throws Exception {
        throw new UnsupportedOperationException("Class reloading is not supported");
    }

    @Override
    public java.util.Set<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule> getDefaultModules(org.opendaylight.controller.config.api.DependencyResolverFactory dependencyResolverFactory, org.osgi.framework.BundleContext bundleContext) {
        return new java.util.HashSet<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl.rev141210.HelloImplModule>();
    }

}

package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;hello&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/hello.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * container output {
 *     leaf greating {
 *         type string;
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;hello/hello-world/output&lt;/i&gt;
 *
 * &lt;p&gt;To create instances of this class use {@link org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutputBuilder}.
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutputBuilder
 *
 */
public interface HelloWorldOutput
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutput>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(
        org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:hello", "2015-01-05", "output"));

    java.lang.String getGreating();

}


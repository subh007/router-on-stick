package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105;
import java.util.concurrent.Future;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;


/**
 * Interface for implementing the following YANG RPCs defined in module &lt;b&gt;hello&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/hello.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * rpc hello-world {
 *     input {
 *         leaf name {
 *             type string;
 *         }
 *     }
 *     
 *     output {
 *         leaf greating {
 *             type string;
 *         }
 *     }
 *     status CURRENT;
 * }
 * &lt;/pre&gt;
 *
 */
public interface HelloService
    extends
    RpcService
{




    Future<RpcResult<HelloWorldOutput>> helloWorld(HelloWorldInput input);

}


# router-on-stick

## Wikipedia Defination
---
> In computing, a "one-armed router", also known as a "router on a stick", is a router that has a single physical or logical connection to a network. A one-armed router is often used to forward traffic between locally attached hosts on separate logical routing domains or to facilitate routing table administration, distribution and relay.

## Traditional setup for the vlan routing
---
![alt text](https://github.com/subh007/router-on-stick/blob/master/diagram/router-on-stick.png)

## Open Flow based solution for the vlan routing
---

## Build and run the application
---
Compile the code:

```shell
$ mvn clean install
```

Running the application:

```shell
$ ./router-karaf/target/assembly/bin/karaf (required java 8)
```

## Test Topology
---
![alt text](https://github.com/subh007/router-on-stick/blob/master/diagram/test-topology.png)

```shell
$ sudo ./mininet-scripts/test-topology.sh
```

This script will create the test topology to provide the mininet based infrastructure for the vlan routing test as per the test topology.

```sh
$ sudo ip netns exec blue /bin/sh
(blue)$ ifconfig
veth1     Link encap:Ethernet  HWaddr 56:b6:69:d8:8d:d9
          inet addr:1.0.0.1  Bcast:1.0.0.255  Mask:255.255.255.0
          inet6 addr: fe80::54b6:69ff:fed8:8dd9/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:30 errors:0 dropped:0 overruns:0 frame:0
          TX packets:9 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000
          RX bytes:5226 (5.2 KB)  TX bytes:738 (738.0 B)
          
  (blue)$ route -n
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         1.0.0.11        0.0.0.0         UG    0      0        0 veth1
1.0.0.0         0.0.0.0         255.255.255.0   U     0      0        0 veth1

(red)$ ifconfig
veth3     Link encap:Ethernet  HWaddr 56:fa:96:63:5a:64
          inet addr:2.0.0.1  Bcast:2.255.255.255  Mask:255.0.0.0
          inet6 addr: fe80::54fa:96ff:fe63:5a64/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:27 errors:0 dropped:2 overruns:0 frame:0
          TX packets:9 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000
          RX bytes:4991 (4.9 KB)  TX bytes:738 (738.0 B)

(red) $ route -n
Kernel IP routing table
Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
0.0.0.0         2.0.0.11        0.0.0.0         UG    0      0        0 veth3
2.0.0.0         0.0.0.0         255.0.0.0       U     0      0        0 veth3
```

Start ping from both the host. Router application first discover the host using the ARP packets and does the proxy arp. Then router application writes some flow to the switch to route the packets.

```shell
(blue)$ ping 2.0.0.1
PING 2.0.0.1 (2.0.0.1) 56(84) bytes of data.
64 bytes from 2.0.0.1: icmp_req=69 ttl=64 time=0.460 ms
64 bytes from 2.0.0.1: icmp_req=70 ttl=64 time=0.127 ms
64 bytes from 2.0.0.1: icmp_req=71 ttl=64 time=0.068 ms
64 bytes from 2.0.0.1: icmp_req=72 ttl=64 time=0.073 ms
64 bytes from 2.0.0.1: icmp_req=73 ttl=64 time=0.088 ms
64 bytes from 2.0.0.1: icmp_req=74 ttl=64 time=0.088 ms

(red)$ ping 1.0.0.1
PING 1.0.0.1 (1.0.0.1) 56(84) bytes of data.
64 bytes from 1.0.0.1: icmp_req=1 ttl=64 time=145 ms
64 bytes from 1.0.0.1: icmp_req=2 ttl=64 time=0.000 ms
64 bytes from 1.0.0.1: icmp_req=3 ttl=64 time=0.079 ms
64 bytes from 1.0.0.1: icmp_req=4 ttl=64 time=0.075 ms
64 bytes from 1.0.0.1: icmp_req=5 ttl=64 time=0.082 ms
```
Flows installed in OF switch (br0) from router-application:

```shell
$ sudo ovs-ofctl dump-flows br0
NXST_FLOW reply (xid=0x4):
 cookie=0x0, duration=1.947s, table=0, n_packets=1, n_bytes=102, idle_timeout=30, idle_age=0, priority=100,ip,in_port=1,dl_vlan=100,nw_src=1.0.0.1,nw_dst=2.0.0.1 actions=mod_vlan_vid:200,output:2
 cookie=0x0, duration=1.943s, table=0, n_packets=1, n_bytes=102, idle_timeout=30, idle_age=0, priority=100,ip,in_port=2,dl_vlan=200,nw_src=2.0.0.1,nw_dst=1.0.0.1 actions=mod_vlan_vid:100,output:1
 cookie=0x0, duration=103.651s, table=0, n_packets=20, n_bytes=1872, idle_age=1, priority=0 actions=CONTROLLER:0
```

## License
---
Copyright (c) 2016 Subhash Kumar Singh

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.



# router-on-stick

## Wikipedia Defination
> In computing, a "one-armed router", also known as a "router on a stick", is a router that has a single physical or logical connection to a network. A one-armed router is often used to forward traffic between locally attached hosts on separate logical routing domains or to facilitate routing table administration, distribution and relay.

---

## Traditional setup for the vlan routing

![alt text](https://github.com/subh007/router-on-stick/blob/master/diagram/router-on-stick.png)

## Open Flow based solution for the vlan routing


## Build and run the application

Compile the code:

```shell
$ mvn clean install
```

Running the application:

```shell
$ ./router-karaf/target/assembly/bin/karaf (required java 8)
```

## Test Topology

```shell
$ sudo ./mininet-scripts/test-topology.sh
```

This script will create the test topology to provide the mininet based infrastructure for the vlan routing test.

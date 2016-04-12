#!/bin/sh

# blue ------ br0 (ovs) --------- red
# 1.0.0.1--(vlan 100)---- ovs ----(vlan 200)------ 1.0.0.2
# add namespaces
ip netns add red
ip netns add blue

# create the veth pairs
ip link add veth0 type veth peer name veth1
ip link add veth2 type veth peer name veth3
ip link add veth4 type veth peer name veth5 # for br1 <----> br0
ip link add veth6 type veth peer name veth7 # for br2 <----> br0

ifconfig veth0 up
ifconfig veth1 up
ifconfig veth2 up
ifconfig veth3 up
ifconfig veth4 up
ifconfig veth5 up
ifconfig veth6 up
ifconfig veth7 up

ip link set veth1 netns blue
ip link set veth3 netns red

ovs-vsctl add-br br0
ovs-vsctl add-br br1
ovs-vsctl add-br br2

ovs-vsctl add-port br1 veth0 # vlan 100
ovs-vsctl add-port br2 veth2 # vlan 200

ovs-vsctl add-port br1 veth4
ovs-vsctl add-port br0 veth5

ovs-vsctl add-port br2 veth6
ovs-vsctl add-port br0 veth7

ip netns exec blue ifconfig veth1 1.0.0.1 netmask 255.255.255.0
ip netns exec red ifconfig veth3 1.0.0.2 netmask 255.255.255.0

ip netns exec blue ifconfig veth1 up
ip netns exec red ifconfig veth3 up

# add the default flow entry
ovs-ofctl add-flow br0 "actions=CONTROLLER"

# set the port 1 and 2 as access port
ovs-vsctl set port veth0 tag=100
ovs-vsctl set port veth2 tag=200

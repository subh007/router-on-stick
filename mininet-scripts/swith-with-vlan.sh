#!/bin/sh

# blue ------ br0 (ovs) --------- red
# 1.0.0.1--(vlan 100)---- ovs ----(vlan 200)------ 1.0.0.2
# add namespaces
sudo ip netns add red
sduo ip netns add blue

# create the veth pairs
ip link add veth0 type veth peer name veth1
ip link add veth2 type veth peer name veth3

ifconfig veth0 up
ifconfig veth1 up
ifconfig veth2 up
ifconfig veth3 up

ip link set veth1 netns blue
ip link set veth3 netns red

ovs-vsctl add-br br0

ovs-vsctl add-port br0 veth0
ovs-vsctl add-port br0 veth2

ip netns exec blue ifconfig veth1 1.0.0.1 netmask 255.255.255.0
ip netns exec red ifconfig veth3 1.0.0.2 netmask 255.255.255.0

ip netns exec blue ifconfig veth1 up
ip netns exec blue ifconfig veth3 up

# add the default flow entry
ovs-ofctl add-flow br0 "actions=CONTROLLER"

# set the port 1 and 2 as access port
ovs-vsctl set port veth0 tag=100
ovs-vsctl set port veth2 tag=200

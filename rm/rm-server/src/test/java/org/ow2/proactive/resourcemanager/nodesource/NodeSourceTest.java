package org.ow2.proactive.resourcemanager.nodesource;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Permission;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;
import org.ow2.proactive.resourcemanager.frontend.topology.pinging.HostsPinger;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.rmnode.RMNodeImpl;
import org.ow2.proactive.resourcemanager.selection.topology.TopologyManager;
import org.ow2.proactive.utils.Subjects;


/**
 * @author ActiveEon Team
 * @since 06/03/17
 */
public class NodeSourceTest {

    private static final String PROACTIVE_PROGRAMMING_NODE_URL = "protocol://authoritypart/";

    private Client client;

    private InfrastructureManager infrastructureManager;

    private NodeSource nodeSource;

    @Before
    public void setUp() {
        PAResourceManagerProperties.RM_TOPOLOGY_ENABLED.updateProperty("false");

        infrastructureManager = mock(InfrastructureManager.class);

        NodeSourcePolicy nodeSourcePolicy = mock(NodeSourcePolicy.class);

        when(nodeSourcePolicy.getProviderAccessType()).thenReturn(AccessType.ALL);

        client = new Client(Subjects.create("user"), false);

        nodeSource = createNodeSource(infrastructureManager, nodeSourcePolicy, client);

        RMCore.topologyManager = new TopologyManager(HostsPinger.class);
    }

    @Test
    public void testDetectedPingedDownNodeCallingInfrastructureManagerInternalRemoveNodeTrueFlag()
            throws RMException, ClassNotFoundException {

        Node node = createNode(PROACTIVE_PROGRAMMING_NODE_URL);

        nodeSource.internalAddNode(node);

        nodeSource.detectedPingedDownNode(node.getNodeInformation().getURL());

        verify(infrastructureManager).internalNotifyDownNode(any(Node.class));
    }

    @Test
    public void testSetNodeAvailableKnownNode() throws RMException {
        Node node = createNode(PROACTIVE_PROGRAMMING_NODE_URL);

        RMNode rmNode = new RMNodeImpl(node, nodeSource, client, mock(Permission.class));

        nodeSource.internalAddNode(node);
        nodeSource.detectedPingedDownNode(node.getNodeInformation().getURL());

        assertThat(nodeSource.getDownNodes()).hasSize(1);

        boolean result = nodeSource.setNodeAvailable(rmNode);

        assertThat(result).isTrue();
        assertThat(nodeSource.getDownNodes()).hasSize(0);
    }

    @Test
    public void testSetNodeAvailableUnknownNode() throws RMException {
        Node node = createNode(PROACTIVE_PROGRAMMING_NODE_URL);

        RMNode rmNode = new RMNodeImpl(node, nodeSource, client, mock(Permission.class));

        nodeSource.internalAddNode(node);

        assertThat(nodeSource.getDownNodes()).hasSize(0);

        boolean result = nodeSource.setNodeAvailable(rmNode);

        assertThat(result).isFalse();
        assertThat(nodeSource.getDownNodes()).hasSize(0);
    }

    private Node createNode(String nodeUrl) {
        Node node = mock(Node.class);

        NodeInformation nodeInformation = mock(NodeInformation.class);
        when(node.getNodeInformation()).thenReturn(nodeInformation);
        when(node.getProActiveRuntime()).thenReturn(mock(ProActiveRuntime.class));
        when(nodeInformation.getURL()).thenReturn(nodeUrl);
        when(nodeInformation.getVMInformation()).thenReturn(mock(VMInformation.class));

        return node;
    }

    private NodeSource createNodeSource(InfrastructureManager infrastructureManager, NodeSourcePolicy nodeSourcePolicy,
            Client client) {
        return new NodeSource("registrationURL",
                              "name",
                              client,
                              infrastructureManager,
                              nodeSourcePolicy,
                              mock(RMCore.class),
                              mock(RMMonitoringImpl.class));
    }

}

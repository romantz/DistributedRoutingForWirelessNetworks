package projects.WirelessRouting.nodes.timers;

import projects.WirelessRouting.nodes.messages.DominatingSetPathSearchMessage;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.timers.Timer;

import java.util.HashSet;

/**
 * Created by Roman_ on 2018-06-30.
 */
public class DominatingSetPathSearchTimer extends Timer {
    GraphNode node;

    public DominatingSetPathSearchTimer(GraphNode n){
        node = n;
    }

    public void fire() {
        if(node.isInDominatingSet()) {
            for(Edge e: node.outgoingConnections) {
                node.send(new DominatingSetPathSearchMessage(node, new HashSet<GraphNode>(), 2), e.endNode);
            }
        }
    }
}

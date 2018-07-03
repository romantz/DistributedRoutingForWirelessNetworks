package projects.WirelessRouting.nodes.timers;

import projects.WirelessRouting.nodes.messages.DominatingSetPathSearchMessage;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.timers.Timer;

import java.util.HashSet;

/**
 * Created by Roman_ on 2018-06-30.
 * A timer set to start calculating a dominating set out of an independent set
 */
public class DominatingSetPathSearchTimer extends Timer {

    // The origin of the path calculation
    GraphNode origin;

    public DominatingSetPathSearchTimer(GraphNode n){
        origin = n;
    }

    public void fire() {
        // If the origin is indeed in the dominating set, then send a message to every neighbor
        // of the node where the distance the message has left to travel is 2
        if(origin.isInDominatingSet()) {
            for(Edge e: origin.outgoingConnections) {
                origin.send(new DominatingSetPathSearchMessage(origin, new HashSet<GraphNode>(), 2), e.endNode);
            }
        }
    }
}

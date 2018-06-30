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
    GraphNode origin;

    public DominatingSetPathSearchTimer(GraphNode n){
        origin = n;
    }

    public void fire() {
        if(origin.isInDominatingSet()) {
            for(Edge e: origin.outgoingConnections) {
                origin.send(new DominatingSetPathSearchMessage(origin, new HashSet<GraphNode>(), 2), e.endNode);
            }
        }
    }
}
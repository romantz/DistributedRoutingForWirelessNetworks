package projects.WirelessRouting.nodes.timers;

import edu.stanford.nlp.graph.Graph;
import projects.WirelessRouting.nodes.messages.BFSMessage;
import projects.WirelessRouting.nodes.messages.DominatingSetPathSearchMessage;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.timers.Timer;

import java.util.HashSet;

/**
 * Created by Roman_ on 2018-06-30.
 */
public class BFSTimer extends Timer {
    public GraphNode origin;

    public BFSTimer(GraphNode o){
        origin = o;
    }

    public void fire() {
        if(origin.isInDominatingSet()) {
            for(Edge e: origin.outgoingConnections) {
                GraphNode endNode = (GraphNode)e.endNode;
                if(endNode.isInDominatingSet()) {
                    origin.send(new BFSMessage(origin.ID, origin), endNode);
                }
            }
        }
    }
}

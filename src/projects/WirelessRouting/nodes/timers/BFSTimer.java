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
 * A timer to start the BFS process from a given node
 */
public class BFSTimer extends Timer {

    // The root of the BFS
    public GraphNode origin;

    public BFSTimer(GraphNode o){
        origin = o;
    }

    public void fire() {
        // Start BFS only if the given node is in the dominating set
        if(origin.isInDominatingSet()) {
            // Send a BFS message to every neighbor of the origin in the dominating set
            for(Edge e: origin.outgoingConnections) {
                GraphNode endNode = (GraphNode)e.endNode;
                if(endNode.isInDominatingSet()) {
                    origin.send(new BFSMessage(origin.ID, origin), endNode);
                }
            }
        }
    }
}

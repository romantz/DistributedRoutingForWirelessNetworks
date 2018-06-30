package projects.WirelessRouting.nodes.messages;

import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

import java.util.HashSet;

/**
 * Created by Roman_ on 2018-06-30.
 */
public class DominatingSetPathSearchMessage extends Message {
    public GraphNode origin;
    public int distanceLeft;
    public HashSet<GraphNode> nodesInPath;

    public DominatingSetPathSearchMessage(GraphNode o, HashSet<GraphNode> nodesInPath, int dist){
        origin = o;
        distanceLeft = dist;
        this.nodesInPath = nodesInPath;
    }

    @Override
    public Message clone() {
        return this; // read-only policy
    }
}

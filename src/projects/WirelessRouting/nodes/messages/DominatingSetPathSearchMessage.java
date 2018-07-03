package projects.WirelessRouting.nodes.messages;

import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

import java.util.HashSet;

/**
 * Created by Roman_ on 2018-06-30.
 * A message that is used in the phase of constructing a dominating set out of
 * a an independent set. It is sent to all nodes with distance up to 3 from every node in the
 * independent set, and all nodes which lie on one of the paths between two nodes in the independent
 * set which have distance <= 3 join the set to form a connected dominating set
 */
public class DominatingSetPathSearchMessage extends Message {

    // The node in the independent set which is the origin of
    public GraphNode origin;

    // The distance this message is allowed to continue travel. Used to avoid reaching nodes
    // further than 3 from the origin
    public int distanceLeft;

    // All the nodes that this message has passed in the current path
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

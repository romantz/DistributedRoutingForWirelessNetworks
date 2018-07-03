package projects.WirelessRouting.nodes.messages;

import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-30.
 * Represents a message in the BFS stage which is when the routing tables are calculated
 */
public class BFSMessage extends Message {

    // The source of this message (The root of the this instance of BFS since every node
    // starts its own BFS process)
    public int originId;

    // The node that sent this message
    public GraphNode sender;

    public BFSMessage(int o, GraphNode s){
        originId = o;
        sender = s;
    }

    @Override
    public Message clone() {
        return this; // read-only policy
    }
}

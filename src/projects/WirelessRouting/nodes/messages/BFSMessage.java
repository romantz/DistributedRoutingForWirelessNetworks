package projects.WirelessRouting.nodes.messages;

import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-30.
 */
public class BFSMessage extends Message{
    public int originId;
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

package projects.WirelessRouting.nodes.messages;

import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-29.
 * Used in the independent set creation phase to let neighbor nodes to know that
 * the current node joined the independent set
 */
public class JoiningIndependentSetMessage extends Message {

    // The node which is joining the independent set
    public GraphNode data;

    public JoiningIndependentSetMessage(GraphNode n){
        data = n;
    }

    @Override
    public Message clone() {
        return this; // read-only policy
    }

    public String toString(){
        return "node " + data.ID + " JoiningIndependentSetMessage";
    }
}

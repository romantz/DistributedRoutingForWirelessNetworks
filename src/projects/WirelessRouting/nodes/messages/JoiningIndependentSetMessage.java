package projects.WirelessRouting.nodes.messages;

import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-29.
 */
public class JoiningIndependentSetMessage extends Message {
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

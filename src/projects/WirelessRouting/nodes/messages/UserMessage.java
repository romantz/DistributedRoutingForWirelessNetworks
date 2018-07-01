package projects.WirelessRouting.nodes.messages;

import edu.stanford.nlp.graph.Graph;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-07-01.
 */
public class UserMessage extends Message {
    public String message;
    public GraphNode target;
    public GraphNode adjacentToTarget;

    public UserMessage(String m, GraphNode t, GraphNode a) {
        message = m;
        target = t;
        adjacentToTarget = a;
    }

    @Override
    public Message clone() {
        return this; // read-only policy
    }
}

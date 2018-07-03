package projects.WirelessRouting.nodes.messages;

import edu.stanford.nlp.graph.Graph;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-07-01.
 * A message which holds the user's input and needs to be routed to the destination the user chose
 */
public class UserMessage extends Message {

    // The text message the user sends
    public String message;

    // The target of the message
    public GraphNode target;

    // A node adjacent to the target node which is in the dominating set
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

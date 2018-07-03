package projects.WirelessRouting.nodes.timers;

import projects.WirelessRouting.nodes.messages.UserMessage;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.timers.Timer;

/**
 * Created by Roman_ on 2018-07-01.
 * A timer used to send a new user message to the given target. This timer is needed since
 * a user message is sent from a handler, but a handler has to synchronize the message somehow
 */
public class UserMessageTimer extends Timer {

    // The content of the message
    public String message;

    // The message source node
    public GraphNode source;

    // A node in the dominating set adjacent to the source
    public GraphNode adjacentToSource;

    // The message target node
    public GraphNode target;

    // A node in the dominating set adjacent to the target
    public GraphNode adjacentToTarget;


    public UserMessageTimer(String m, GraphNode s, GraphNode a1, GraphNode t, GraphNode a2) {
        message = m;
        source = s;
        adjacentToSource = a1;
        target = t;
        adjacentToTarget = a2;
    }


    public void fire() {
        // Send the given message to the neighbor of the source
        source.send(new UserMessage(message, target, adjacentToTarget), adjacentToSource);
    }
}

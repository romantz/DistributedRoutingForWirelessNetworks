package projects.WirelessRouting.nodes.timers;

import projects.WirelessRouting.nodes.messages.UserMessage;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.timers.Timer;

/**
 * Created by Roman_ on 2018-07-01.
 */
public class UserMessageTimer extends Timer {

    public String message;
    public GraphNode source;
    public GraphNode adjacentToSource;
    public GraphNode target;
    public GraphNode adjacentToTarget;


    public UserMessageTimer(String m, GraphNode s, GraphNode a1, GraphNode t, GraphNode a2) {
        message = m;
        source = s;
        adjacentToSource = a1;
        target = t;
        adjacentToTarget = a2;
    }

    public void fire() {
        source.send(new UserMessage(message, target, adjacentToTarget), adjacentToSource);
    }
}

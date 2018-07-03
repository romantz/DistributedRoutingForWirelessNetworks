package projects.WirelessRouting.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-30.
 * Used to inform nodes that they should join the dominating set since they were
 * in a path between two close (distance <= 3) nodes in the dominating set
 */
public class ShouldJoinDominatingSetMessage extends Message{
    @Override
    public Message clone() {
        return this; // read-only policy
    }
}

package projects.WirelessRouting.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-30.
 */
public class ShouldJoinDominatingSetMessage extends Message{
    @Override
    public Message clone() {
        return this; // read-only policy
    }
}

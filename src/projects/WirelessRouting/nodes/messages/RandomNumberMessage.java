package projects.WirelessRouting.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-29.
 */
public class RandomNumberMessage extends Message {
    public double data;

    public RandomNumberMessage(double n){
        data = n;
    }

    @Override
    public Message clone() {
        return this; // read-only policy
    }
}

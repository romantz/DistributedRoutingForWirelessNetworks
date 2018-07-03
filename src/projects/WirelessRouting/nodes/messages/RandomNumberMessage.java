package projects.WirelessRouting.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Created by Roman_ on 2018-06-29.
 * A message containing the random number a node has generated in the
 * independent set creation phase
 */
public class RandomNumberMessage extends Message {

    // The number that was generated
    public double data;

    public RandomNumberMessage(double n){
        data = n;
    }

    @Override
    public Message clone() {
        return this; // read-only policy
    }

    public String toString(){
        return "Random number: " + data;
    }
}

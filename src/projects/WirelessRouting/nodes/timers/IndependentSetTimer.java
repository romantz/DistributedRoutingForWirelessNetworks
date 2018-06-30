package projects.WirelessRouting.nodes.timers;

import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.timers.Timer;

/**
 * Created by Roman_ on 2018-06-29.
 */
public class IndependentSetTimer extends Timer{
    // The node that started this timer
    GraphNode node;
    int iterations;

    public IndependentSetTimer(GraphNode n, int i){
        node = n;
        iterations = i;
    }

    public void fire() {
        if(node.isActive()) {
            node.independentSetIteration();
            iterations--;
            if(iterations > 0)
                this.startRelative(2, node);
        }
    }

}

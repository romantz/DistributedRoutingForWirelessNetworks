package projects.WirelessRouting.nodes.timers;

import projects.WirelessRouting.CustomGlobal;
import projects.WirelessRouting.nodes.nodeImplementations.GraphNode;
import sinalgo.nodes.timers.Timer;

/**
 * Created by Roman_ on 2018-06-29.
 * A timer to start calculating an independent set
 */
public class IndependentSetTimer extends Timer{

    // The node that started this timer
    GraphNode node;

    // The number of the current iteration in the calculation
    int iterations;

    public IndependentSetTimer(GraphNode n, int i){
        node = n;
        iterations = i;
    }

    public void fire() {
        if(node.isActive()) {
            // If the node is active than perform an independent set calculation iteration
            node.independentSetIteration();
            iterations--;
            // If there are more iterations to perform, restart this timer
            if(iterations > 0)
                this.startRelative(CustomGlobal.INDEPENDENT_SET_CALCULATION_ROUNDS_PER_ITERATION, node);
        }
    }

}

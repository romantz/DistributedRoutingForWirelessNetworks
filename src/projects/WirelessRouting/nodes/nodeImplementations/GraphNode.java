package projects.WirelessRouting.nodes.nodeImplementations;

import projects.WirelessRouting.nodes.messages.JoiningIndependentSetMessage;
import projects.WirelessRouting.nodes.messages.RandomNumberMessage;
import projects.WirelessRouting.nodes.timers.IndependentSetTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.awt.*;
import java.util.Comparator;

/**
 * Created by Roman_ on 2018-06-29.
 */
public class GraphNode extends Node {

    private boolean active;
    private double currentRandomNumber;
    private static int graphSize = 0;
    private GraphNode independentSetNeighbor;
    private boolean isInIndependentSet;
    private boolean hasNeighborWithHigherRandomNumber;
    private boolean receivedMessageFromNeighbor = false;

    private static int radius;
    { try {
        radius = Configuration.getIntegerParameter("GeometricNodeCollection/rMax");
    } catch(CorruptConfigurationEntryException e) {
        Tools.fatalError(e.getMessage());
    }}

    public static void setGraphSize(int n){
        graphSize = n;
    }

    public boolean isActive(){
        return active;
    }

    public void setActive(boolean a){
        active = a;
    }

    @Override
    public void init() {
        active = true;
        independentSetNeighbor = null;
        isInIndependentSet = false;

        IndependentSetTimer ist = new IndependentSetTimer(this, 1);
        ist.startRelative(1, this);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (msg instanceof RandomNumberMessage) {
                double num = ((RandomNumberMessage)msg).data;
                System.out.println(num + ", " + currentRandomNumber);
                if(num >= currentRandomNumber){
                    hasNeighborWithHigherRandomNumber = true;
                }
            }

            if (msg instanceof JoiningIndependentSetMessage) {
                isInIndependentSet = false;
                independentSetNeighbor = ((JoiningIndependentSetMessage)msg).data;
                active = false;
            }

            receivedMessageFromNeighbor = true;
        }
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {
    }


    @Override
    public void neighborhoodChange() {
    }

    @Override
    public void preStep() {
        if(active) {
            currentRandomNumber = Math.floor(Math.random() * Math.pow(graphSize, 10)) + 1;
            hasNeighborWithHigherRandomNumber = false;
        }
        System.out.println("test " + currentRandomNumber);
    }

    @Override
    public void postStep() {
        if(
                (receivedMessageFromNeighbor || outgoingConnections.size() == 0)
                        && active && !hasNeighborWithHigherRandomNumber) {
            isInIndependentSet = true;
            active = false;
            for(Edge e: outgoingConnections){
                send(new JoiningIndependentSetMessage(this), e.endNode);
            }
        }

        System.out.println(isInIndependentSet);
    }

    public void independentSetIteration() {
        for(Edge e: outgoingConnections){
            GraphNode endNode = (GraphNode)e.endNode;
            if(endNode.isActive()) {
                send(new RandomNumberMessage(currentRandomNumber), e.endNode);
            }
        }
        /*boolean isMaxAmongNeighbors = true;
        for(Edge e: outgoingConnections){
            GraphNode endNode = (GraphNode)e.endNode;
            if(endNode.isActive() && endNode.currentRandomNumber >= currentRandomNumber) {
                isMaxAmongNeighbors = false;
            }
        }
        if(isMaxAmongNeighbors) {
            for(Edge e: outgoingConnections){
                isInIndependentSet = true;
                active = false;
                send(new JoiningIndependentSetMessage(this), e.endNode);
            }
        }*/
    }

    // Copied from projects.sample3.nodes.nodeImplementations.Antenna.draw
    public void draw(Graphics g, PositionTransformation pt, boolean highlight){
        Color bckup = g.getColor();
        if(isInIndependentSet)
            this.setColor(Color.RED);
        else
            this.setColor(Color.BLACK);
        g.setColor(Color.BLACK);
        this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor());
        super.drawAsDisk(g, pt, highlight, drawingSizeInPixels);
        g.setColor(Color.LIGHT_GRAY);
        pt.translateToGUIPosition(this.getPosition());
        int r = (int) (radius * pt.getZoomFactor());
        g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
        g.setColor(bckup);
    }

    /**
     * Helper class to compare two nodes by their ID
     */
    class NodeComparer implements Comparator<Node> {
        public int compare(Node n1, Node n2) {
            return n1.ID < n2.ID ? -1 : n1.ID == n2.ID ? 0 : 1;
        }
    }
}

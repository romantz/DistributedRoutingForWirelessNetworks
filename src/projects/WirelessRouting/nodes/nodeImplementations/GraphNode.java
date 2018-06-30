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

    private int numberOfActiveNeighbors;
    private int numberOfMessagesReceivedFromActiveNeighbors;

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

        IndependentSetTimer ist = new IndependentSetTimer(this, (int)Math.ceil(Math.log(graphSize)));
        ist.startRelative(1, this);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            System.out.println("node " + ID + " received message: " + msg);

            if (msg instanceof JoiningIndependentSetMessage) {
                isInIndependentSet = false;
                independentSetNeighbor = ((JoiningIndependentSetMessage)msg).data;
                active = false;
            }

            if (msg instanceof RandomNumberMessage && isActive()) {
                double num = ((RandomNumberMessage)msg).data;
                System.out.println(ID +": " + currentRandomNumber + ", " + num);
                if(num >= currentRandomNumber){
                    hasNeighborWithHigherRandomNumber = true;
                }

                numberOfMessagesReceivedFromActiveNeighbors++;
                if(numberOfMessagesReceivedFromActiveNeighbors == numberOfActiveNeighbors &&
                        !hasNeighborWithHigherRandomNumber) {
                    joinIndependentSet();
                }
            }

            receivedMessageFromNeighbor = true;
        }
    }

    public void joinIndependentSet(){
        System.out.println("Node " + ID + " decided to join the independent set");
        isInIndependentSet = true;
        active = false;
        for(Edge e: outgoingConnections) {
            GraphNode n = (GraphNode)e.endNode;
            send(new JoiningIndependentSetMessage(this), n);
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

    }

    @Override
    public void postStep() {
        if(isActive() && numberOfActiveNeighbors == 0){
            joinIndependentSet();
        }
    }

    public void independentSetIteration() {
        numberOfActiveNeighbors = 0;
        numberOfMessagesReceivedFromActiveNeighbors = 0;
        currentRandomNumber = Math.floor(Math.random() * Math.pow(graphSize, 10)) + 1;
        System.out.println("node " + ID + " generated the number " + currentRandomNumber);
        hasNeighborWithHigherRandomNumber = false;
        for(Edge e: outgoingConnections) {
            GraphNode n = (GraphNode)e.endNode;
            if (n.isActive()) {
                numberOfActiveNeighbors++;
                send(new RandomNumberMessage(currentRandomNumber), n);
            }
        }
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

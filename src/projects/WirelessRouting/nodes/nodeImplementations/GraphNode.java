package projects.WirelessRouting.nodes.nodeImplementations;

import projects.WirelessRouting.CustomGlobal;
import projects.WirelessRouting.nodes.messages.*;
import projects.WirelessRouting.nodes.timers.BFSTimer;
import projects.WirelessRouting.nodes.timers.DominatingSetPathSearchTimer;
import projects.WirelessRouting.nodes.timers.IndependentSetTimer;
import projects.WirelessRouting.nodes.timers.UserMessageTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Roman_ on 2018-06-29.
 */
public class GraphNode extends Node {

    private boolean active;
    private double currentRandomNumber;
    private static int graphSize = 0;
    private boolean isInDominatingSet;
    private boolean hasNeighborWithHigherRandomNumber;

    private int numberOfActiveNeighbors;
    private int numberOfMessagesReceivedFromActiveNeighbors;

    private HashSet<Integer> hasPathToNodes;

    // A mapping S -> A where S is a destination node ID which is in the dominating set and an adjacent node A
    // such that a message from the current node to S should be sent through A
    private HashMap<Integer, GraphNode> routingTable;

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

    public boolean isInDominatingSet(){
        return isInDominatingSet;
    }

    @Override
    public void init() {
        active = true;
        //independentSetNeighbor = null;
        hasPathToNodes = new HashSet<Integer>();
        isInDominatingSet = false;

        routingTable = new HashMap<Integer, GraphNode>();

        int numberOfIterationsForIndependentSetCalculation = (int)Math.ceil(Math.log(graphSize));

        IndependentSetTimer ist = new IndependentSetTimer(this,
                numberOfIterationsForIndependentSetCalculation);
        ist.startRelative(1, this);

        DominatingSetPathSearchTimer dsps = new DominatingSetPathSearchTimer(this);
        dsps.startRelative(numberOfIterationsForIndependentSetCalculation *
                CustomGlobal.INDEPENDENT_SET_CALCULATION_ROUNDS_PER_ITERATION + 1, this);

        BFSTimer bfst = new BFSTimer(this);
        bfst.startRelative(numberOfIterationsForIndependentSetCalculation *
                CustomGlobal.INDEPENDENT_SET_CALCULATION_ROUNDS_PER_ITERATION + 6, this);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof JoiningIndependentSetMessage) {
                isInDominatingSet = false;
                //independentSetNeighbor = ((JoiningIndependentSetMessage)msg).data;
                active = false;
            }

            if (msg instanceof RandomNumberMessage && isActive()) {
                double num = ((RandomNumberMessage)msg).data;
                if(num >= currentRandomNumber){
                    hasNeighborWithHigherRandomNumber = true;
                }

                numberOfMessagesReceivedFromActiveNeighbors++;
                if(numberOfMessagesReceivedFromActiveNeighbors == numberOfActiveNeighbors &&
                        !hasNeighborWithHigherRandomNumber) {
                    joinIndependentSet();
                }
            }

            if(msg instanceof ShouldJoinDominatingSetMessage) {
                isInDominatingSet = true;
            }

            if (msg instanceof DominatingSetPathSearchMessage) {
                DominatingSetPathSearchMessage m = (DominatingSetPathSearchMessage)msg;
                if(m.origin.ID != ID) {
                    if(!hasPathToNodes.contains(m.origin.ID)){
                        hasPathToNodes.add(m.origin.ID);
                        for(GraphNode n: m.nodesInPath)
                            hasPathToNodes.add(n.ID);
                        if(isInDominatingSet){
                            for(GraphNode n: m.nodesInPath)
                                send(new ShouldJoinDominatingSetMessage(), n);
                        } else {
                            if(m.distanceLeft > 0) {
                                HashSet<GraphNode> newPath = new HashSet<GraphNode>();
                                newPath.addAll(m.nodesInPath);
                                newPath.add(this);
                                for (Edge e : outgoingConnections) {
                                    send(new DominatingSetPathSearchMessage(m.origin,
                                            newPath,
                                            m.distanceLeft - 1), e.endNode);
                                }
                            }
                        }
                    }
                }
            }

            if(msg instanceof BFSMessage) {
                BFSMessage m = (BFSMessage)msg;
                if(m.originId != ID && !routingTable.containsKey(m.originId)) {
                    routingTable.put(m.originId, m.sender);
                    for (Edge e : outgoingConnections) {
                        GraphNode endNode = (GraphNode)e.endNode;
                        if(endNode.isInDominatingSet()) {
                            send(new BFSMessage(m.originId, this), endNode);
                        }
                    }
                }
            }

            if(msg instanceof UserMessage) {
                UserMessage m = (UserMessage) msg;
                if(m.target.ID == ID){
                    System.out.println("Node " + ID + " received the message: " + m.message);
                } else if(m.adjacentToTarget.ID == ID){
                    send(new UserMessage(m.message, m.target, m.adjacentToTarget), m.target);
                } else {
                    GraphNode nextNode = routingTable.get(m.adjacentToTarget.ID);
                    if(nextNode != null) {
                        send(new UserMessage(m.message, m.target, m.adjacentToTarget), nextNode);
                    }
                }
            }
        }
    }

    public void joinIndependentSet(){
        isInDominatingSet = true;
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
        if(isInDominatingSet)
            this.setColor(Color.RED);
        else
            this.setColor(Color.BLACK);
        g.setColor(Color.BLACK);
        this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor());
        super.drawAsDisk(g, pt, highlight, drawingSizeInPixels);
        //g.setColor(Color.LIGHT_GRAY);
        //pt.translateToGUIPosition(this.getPosition());
        //int r = (int) (radius * pt.getZoomFactor());
        //g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
        g.setColor(bckup);
    }


    @NodePopupMethod(menuText = "Send Message To...")
    public void sendMessageTo() {

        final String msg = Tools.showQueryDialog("Please enter a message");
        final GraphNode source = this;

        Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
            public void handleNodeSelectedEvent(Node t) {
                if(t == null) {
                    return; // aborted
                }

                GraphNode target = (GraphNode) t;

                Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
                    public void handleNodeSelectedEvent(Node a) {
                        if (a == null) {
                            return; // aborted
                        }

                        GraphNode adjacentToTarget = (GraphNode) a;

                        if (!adjacentToTarget.isInDominatingSet()) {
                            Tools.showMessageDialog("An illegal node was chosen");
                        } else {
                            boolean isAdjacent = false;

                            for(Edge e: adjacentToTarget.outgoingConnections) {
                                GraphNode endNode = (GraphNode)e.endNode;
                                if(endNode.ID == target.ID){
                                    isAdjacent = true;
                                }
                                if(endNode.ID == source.ID){
                                    UserMessageTimer umt = new UserMessageTimer(
                                            msg,
                                            source,
                                            target,
                                            target,
                                            source);
                                    umt.startRelative(1, source);
                                    return;
                                }
                            }

                            if(!isAdjacent) {
                                Tools.showMessageDialog("The chosen node in the dominating set " +
                                        "is not adjacent to the target node");
                            } else {
                                GraphNode neighborInDominatingSet = null;

                                for(Edge e: source.outgoingConnections) {
                                    GraphNode endNode = (GraphNode)e.endNode;
                                    if(endNode.isInDominatingSet()) {
                                        neighborInDominatingSet = endNode;
                                    }
                                }

                                if(neighborInDominatingSet == null){
                                    Tools.showMessageDialog("The chosen source node has no neighbor " +
                                            "in the dominating set");
                                }
                                else {
                                    UserMessageTimer umt = new UserMessageTimer(
                                            msg,
                                            source,
                                            neighborInDominatingSet,
                                            target,
                                            adjacentToTarget);
                                    umt.startRelative(1, source);
                                }
                            }
                        }
                    }
                }, "Select a node in the dominating set which is adjacent to the target node");
            }
        }, "Select a target node to send a message to");
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

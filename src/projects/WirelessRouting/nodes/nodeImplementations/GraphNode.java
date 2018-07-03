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

    // true if the node is not in the independent set and has no neighbors in the independent set
    // Used during the creation of the independent set
    private boolean active;

    // The current random number that the node generated
    private double currentRandomNumber;

    // The size of the graph. Used to determine the range of the random number generated by nodes
    // and to determine the value of log(n) in order to decide the running time of the first step
    private static int graphSize = 0;

    // true if the node is in the independent set
    private boolean isInDominatingSet;

    // true if this node generated a lower random random number than one if its neighbors
    private boolean hasNeighborWithHigherRandomNumber;

    // Number of neighbors that are still generating random numbers
    private int numberOfActiveNeighbors;

    // Number of messages received in the current round from all active neighbors
    // This variable is used to indicate when we received all the random numbers that the neighbors generated
    private int numberOfMessagesReceivedFromActiveNeighbors;

    // The node IDs which this node has a path of length less than four to
    // This variable is used to calculate the dominating set
    private HashSet<Integer> hasPathToNodes;

    // A mapping S -> A where S is a destination node ID which is in the dominating set and an adjacent node A
    // such that a message from the current node to S should be sent through A
    private HashMap<Integer, GraphNode> routingTable;

    // Set the UDG radius according to the param rMax
    private static int radius;
    { try {
        radius = Configuration.getIntegerParameter("UDG/rMax");
    } catch(CorruptConfigurationEntryException e) {
        Tools.fatalError(e.getMessage());
    }}

    /**
     * Set the graph size
     * @param n the number of nodes in the graph
     */
    public static void setGraphSize(int n){
        graphSize = n;
    }

    /**
     * @return true if the node is not in the independent set and has no neighbor in the set
     */
    public boolean isActive(){
        return active;
    }

    /**
     * @return true if this node is in the dominating set
     */
    public boolean isInDominatingSet(){
        return isInDominatingSet;
    }

    @Override
    public void init() {
        active = true;
        hasPathToNodes = new HashSet<Integer>();
        isInDominatingSet = false;

        routingTable = new HashMap<Integer, GraphNode>();

        // Calculate the number of iterations in the first step
        int numberOfIterationsForIndependentSetCalculation = (int)Math.ceil(Math.log(graphSize));

        // Set a timer to start independent set calculations in the next round
        IndependentSetTimer ist = new IndependentSetTimer(this,
                numberOfIterationsForIndependentSetCalculation);
        ist.startRelative(1, this);

        // Set a timer to start dominating set calculations when the independent set calculations finish
        DominatingSetPathSearchTimer dsps = new DominatingSetPathSearchTimer(this);
        dsps.startRelative(numberOfIterationsForIndependentSetCalculation *
                CustomGlobal.INDEPENDENT_SET_CALCULATION_ROUNDS_PER_ITERATION + 1, this);

        // Set a timer to start running BFS once a dominating set is established
        BFSTimer bfst = new BFSTimer(this);
        bfst.startRelative(numberOfIterationsForIndependentSetCalculation *
                CustomGlobal.INDEPENDENT_SET_CALCULATION_ROUNDS_PER_ITERATION + 6, this);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof JoiningIndependentSetMessage) {
                // This message means that a neighbor has joined the independent set so the
                // current node should become inactive
                isInDominatingSet = false;
                active = false;
            }

            if (msg instanceof RandomNumberMessage && isActive()) {
                // Check if a neighbor generated a random number higher than the current node
                double num = ((RandomNumberMessage)msg).data;
                if(num >= currentRandomNumber){
                    hasNeighborWithHigherRandomNumber = true;
                }

                // If all random number messages were received from all active neighbors in the current round
                // and the current node generated the highest random number, it joins the independent set
                numberOfMessagesReceivedFromActiveNeighbors++;
                if(numberOfMessagesReceivedFromActiveNeighbors == numberOfActiveNeighbors &&
                        !hasNeighborWithHigherRandomNumber) {
                    joinIndependentSet();
                }
            }

            // A node in the independent set found out that the current node is on a path shorter than 3
            // to another node in the independent set, this means that the current node needs to join the
            // dominating set
            if(msg instanceof ShouldJoinDominatingSetMessage) {
                isInDominatingSet = true;
            }

            if (msg instanceof DominatingSetPathSearchMessage) {
                // Searching paths of length <= 3 to other nodes in the independent set
                DominatingSetPathSearchMessage m = (DominatingSetPathSearchMessage)msg;
                if(m.origin.ID != ID) {
                    // check if the current node has not yet found a path to the node in the message
                    if(!hasPathToNodes.contains(m.origin.ID)){
                        hasPathToNodes.add(m.origin.ID);
                        // Add all nodes in the path as nodes that the current node has paths to
                        for(GraphNode n: m.nodesInPath)
                            hasPathToNodes.add(n.ID);
                        // If the current node is in the dominating set, add all nodes in the path
                        // to the dominating set too
                        if(isInDominatingSet){
                            for(GraphNode n: m.nodesInPath)
                                send(new ShouldJoinDominatingSetMessage(), n);
                        } else {
                            // Otherwise forward this message further, but only if the path length until
                            // now is < 3
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
                // For every BFS message we check if the routing table already contains the origin
                // if not, it is inserted into the table and the message is forwarded to all neighbors
                // in the dominating set
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
                // A user message is sent to the destination node using the routing tables
                UserMessage m = (UserMessage) msg;
                if(m.target.ID == ID){
                    System.out.println("Node " + ID + " received the message: " + m.message);
                } else if(m.adjacentToTarget.ID == ID){
                    send(new UserMessage(m.message, m.target, m.adjacentToTarget), m.target);
                } else {
                    // Check if the node that is adjacent to the destination is in the routing table
                    // If so, forward it using the table
                    GraphNode nextNode = routingTable.get(m.adjacentToTarget.ID);
                    if(nextNode != null) {
                        send(new UserMessage(m.message, m.target, m.adjacentToTarget), nextNode);
                    }
                }
            }
        }
    }

    /**
     * Join the independent set and let all neighbors know
     */
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

    /**
     * A single iteration of the process of producing an independent set
     */
    public void independentSetIteration() {
        numberOfActiveNeighbors = 0;
        numberOfMessagesReceivedFromActiveNeighbors = 0;
        // generate a random number and send it to all active neighbors
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

    // Right click on a node to send message
    @NodePopupMethod(menuText = "Send Message To...")
    public void sendMessageTo() {

        final String msg = Tools.showQueryDialog("Please enter a message");
        final GraphNode source = this;

        // Select a destination node
        Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
            public void handleNodeSelectedEvent(Node t) {
                if(t == null) {
                    return; // aborted
                }

                GraphNode target = (GraphNode) t;

                // Select a node that is adjacent to the destination
                Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
                    public void handleNodeSelectedEvent(Node a) {
                        if (a == null) {
                            return; // aborted
                        }

                        GraphNode adjacentToTarget = (GraphNode) a;

                        // Check if the last node selected is in the dominating set
                        if (!adjacentToTarget.isInDominatingSet()) {
                            Tools.showMessageDialog("An illegal node was chosen. Need to choose a node " +
                                    "in the dominating set");
                        } else {
                            boolean isAdjacent = false;

                            // Check if the last chosen node is really adjacent to the target node
                            for(Edge e: adjacentToTarget.outgoingConnections) {
                                GraphNode endNode = (GraphNode)e.endNode;
                                if(endNode.ID == target.ID){
                                    isAdjacent = true;
                                }

                                // If the source is adjacent to the node that is adjacent to the target,
                                // then start a timer to send the message in the next round
                                if(endNode.ID == source.ID){
                                    UserMessageTimer umt = new UserMessageTimer(
                                            msg,
                                            source,
                                            adjacentToTarget,
                                            target,
                                            adjacentToTarget);
                                    umt.startRelative(1, source);
                                    return;
                                }
                            }

                            if(!isAdjacent) {
                                Tools.showMessageDialog("The chosen node in the dominating set " +
                                        "is not adjacent to the target node");
                            } else {
                                GraphNode neighborInDominatingSet = null;

                                // Find a neighbor in the dominating set to use its routing table to send the message
                                for (Edge e : source.outgoingConnections) {
                                    GraphNode endNode = (GraphNode) e.endNode;
                                    if (endNode.isInDominatingSet()) {
                                        neighborInDominatingSet = endNode;
                                    }
                                }


                                if(neighborInDominatingSet == null){
                                    Tools.showMessageDialog("The chosen source node has no neighbor " +
                                            "in the dominating set");
                                }
                                else {
                                    // Send the message to a nearby node which is in the dominating set
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

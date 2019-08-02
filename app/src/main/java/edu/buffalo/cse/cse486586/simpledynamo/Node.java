package edu.buffalo.cse.cse486586.simpledynamo;
public class Node implements Comparable<Node> {
    String portNo;
    String hash;
    //String successor;
    // String predecessor;

    public Node(String portNo, String hash) {
        this.portNo = portNo;
        this.hash = hash;


    }

    public String geth() {
        return hash;
    }

    public String getPortNo() {
        return portNo;
    }



    @Override
    public int compareTo(Node o) {
        if ((this.geth().compareTo(o.geth()) > 0))
            return 1;
        else if ((this.geth().compareTo(o.geth()) < 0))
            return -1;


        else {
            return 0;
        }
    }
}

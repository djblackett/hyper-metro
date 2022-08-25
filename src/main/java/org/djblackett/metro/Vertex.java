package org.djblackett.metro;

import org.djblackett.metro.Edge;
import org.djblackett.metro.Transfer;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

    private String line;
    private String name;

    private List<String> next;

    private List<String> prev;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    int time;
    private List<Edge> edges = new ArrayList<>();
    private List<Transfer> transfer = new ArrayList<>();

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getName() {
        return this.name;
    }

    // Constructor
    Vertex(String d)
    {
        name = d;
        next = null;
        prev = null;
        edges = new ArrayList<>();
    }

    public Vertex() {};

    public void addEdge(Vertex end, Integer weight) {
        this.edges.add(new Edge(this, end, weight));
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    @Override
    public String toString() {
        return "Vertex: " +
                name;
    }

    public List<String> getNext() {
        return next;
    }


    public List<String> getPrev() {
        return prev;
    }

    public List<Transfer> getTransfer() {
        return transfer;
    }

    public void addTransfer(Transfer transfer) {
        this.transfer.add(transfer);
    }
}

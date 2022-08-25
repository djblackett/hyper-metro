package org.djblackett.metro;

public class Edge {
    private final Vertex start;
    private final Vertex end;
    private final int weight;

    public Edge(Vertex startV, Vertex endV, int inputWeight) {
        this.start = startV;
        this.end = endV;
        this.weight = inputWeight;
    }

    public Vertex getStart() {
        return this.start;
    }

    public Vertex getEnd() {
        return this.end;
    }

    @Override
    public String toString() {
        return start.getName() +
                " to " + end.getName() +
                ": " + weight;
    }

    public int getWeight() {
        return this.weight;
    }
}

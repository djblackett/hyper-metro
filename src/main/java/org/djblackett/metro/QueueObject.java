package org.djblackett.metro;
public class QueueObject implements Comparable<QueueObject> {
    public Vertex vertex;
    public int priority;

    public QueueObject(Vertex v, int p){
        this.vertex = v;
        this.priority = p;
    }

    @Override
    public int compareTo(QueueObject o) {
        return Integer.compare(this.priority, o.priority);
    }
}

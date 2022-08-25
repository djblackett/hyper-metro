package org.djblackett.metro;


import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.LinkedList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) {
        String file;
        String filePath = new File("").getAbsolutePath();
        if (args.length > 0) {
            file = args[0];


            if (args[0] == null) {
                System.out.println("No CLI arguments present");
                return;
            }

            // local environment testing real files
            // uncomment following line to use local files
            // filePath = filePath.concat("\\HyperMetro\\task").concat(file);
            //System.out.println(filePath);

            try {
                // for Hyperskill testing suite
                // comment out below line if not using Hyperskill's tests
                filePath = file;


                // gets the map of metro lines with the line names as keys and LinkedLists of station nodes as values
                Map<String, java.util.LinkedList<Vertex>> metroLines = JSONFixer.parseStationsFile(filePath);

                if (metroLines == null) {
                    System.out.println("Error reading file");
                    return;
                }


                // Iterate through each station to add Edge objects for every connected station.
                // If a station has a transfer to another line, the same station vertex on the other line is not
                // counted as a node. The edge from the current node is directly connected to the other node's
                // next and prev lists. An additional weight of 5 mins is added to account for line switching
                for (Map.Entry<String, LinkedList<Vertex>> entry : metroLines.entrySet()) {

                    LinkedList<Vertex> currentLine = entry.getValue();

                    currentLine.forEach(current -> {
                        current.setLine(entry.getKey());

                        if (current.getTransfer().size() != 0) {

                            for (Transfer transfer : current.getTransfer()) {
                                LinkedList<Vertex> transferLine = metroLines.get(transfer.getLine());
                                Vertex transferConnection = getNodeByName(transferLine, transfer.getStation());

                                for (String s : transferConnection.getNext()) {
                                    Vertex v = getNodeByName(transferLine, s);
                                    current.addEdge(v, current.getTime() + 5);
                                }

                                for (String s : transferConnection.getPrev()) {
                                    Vertex v = getNodeByName(transferLine, s);
                                    current.addEdge(v, v.getTime() + 5);
                                }
                            }
                        }

                        if (current.getNext().size() != 0) {
                            for (String s : current.getNext()) {
                                LinkedList<Vertex> line = metroLines.get(current.getLine());
                                current.addEdge(getNodeByName(line, s), current.getTime());
                            }
                        }

                        if (current.getPrev() != null) {
                            for (String s : current.getPrev()) {
                                LinkedList<Vertex> line = metroLines.get(current.getLine());
                                Vertex prevVertex = getNodeByName(line, s);
                                current.addEdge(prevVertex, prevVertex.getTime());
                            }
                        }
                    });
                }


                // process input commands
                Scanner sc = new Scanner(System.in);
                String input;
                input = sc.nextLine();
                while (!input.equals("exit")) {

                    List<String> arguments = getArguments(input);

                    String command = arguments.get(0);
                    String argument = arguments.get(1);


                    String argument2;

                    switch (command) {
                        case "/output" -> {
                            if (metroLines.containsKey(argument)) {
                                metroLines.get(argument).forEach(System.out::println);
                            } else {
                                System.out.println("Metro line not found");
                            }
                        }
                        case "/remove" -> {
                            if (metroLines.containsKey(argument)) {
                                argument2 = arguments.get(2);
                                metroLines.get(argument).remove(getNodeByName(metroLines.get(argument), argument2));
                            } else {
                                System.out.println("Metro line not found");
                            }
                        }

                        // input may or may not include a value for "time"
                        case "/append" -> {
                            if (metroLines.containsKey(argument)) {
                                argument2 = arguments.get(2);
                                String time;
                                try {
                                    time = arguments.get(3);
                                } catch (IndexOutOfBoundsException e) {
                                    time = "0";
                                }
                                Vertex newVertex = new Vertex(argument2);
                                newVertex.setTime(Integer.parseInt(time));
                                metroLines.get(argument).add(newVertex);
                            } else {
                                System.out.println("Metro line not found");
                            }
                        }
                        case "/add-head" -> {
                            if (metroLines.containsKey(argument)) {
                                argument2 = arguments.get(2);
                                Vertex newVertex = new Vertex(argument2);

                                String time;
                                try {
                                    time = arguments.get(3);
                                } catch (IndexOutOfBoundsException e) {
                                    time = "0";
                                }

                                newVertex.setTime(Integer.parseInt(time));
                                metroLines.get(argument).addFirst(newVertex);
                            } else {
                                System.out.println("Metro line not found");
                            }
                        }
                        case "/exit" -> System.exit(0);
                        case "/connect" -> {
                            String line1 = arguments.get(1);
                            String line2 = arguments.get(3);
                            String station1 = arguments.get(2);
                            String station2 = arguments.get(4);

                            connect(metroLines, line1, station1, line2, station2);
                        }

                        // gets the fastest route WITHOUT considering weights
                        case "/route" -> {
                            String startLine = arguments.get(1);
                            String endLine = arguments.get(3);
                            String startStation = arguments.get(2);
                            String endStation = arguments.get(4);

                            shortestPathBetween(metroLines, getNodeByName(metroLines.get(startLine), startStation), getNodeByName(metroLines.get(endLine), endStation), false);
                        }

                        // gets the fastest route
                        case "/fastest-route" -> {
                            String startLine = arguments.get(1);
                            String endLine = arguments.get(3);
                            String startStation = arguments.get(2);
                            String endStation = arguments.get(4);

                            shortestPathBetween(metroLines, getNodeByName(metroLines.get(startLine), startStation), getNodeByName(metroLines.get(endLine), endStation), true);
                        }
                        default -> System.out.println("Invalid command");
                    }
                    input = sc.nextLine();
                }

            } catch (IllegalStateException e) {
                System.out.println("Text parsing error");
                e.printStackTrace();
            } catch (FileNotFoundException | NoSuchFileException e) {
                System.out.println("Error! Such a file doesn't exist!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // connects two lines together, usually with the same station name for both
    public static void connect(Map<String, LinkedList<Vertex>> map, String line1, String station1, String line2, String station2) {

        try {
            LinkedList<Vertex> list1 = map.get(line1);
            LinkedList<Vertex> list2 = map.get(line2);
            Vertex stationVertex1 = list1.getFirst();
            Vertex stationVertex2 = list2.getFirst();

            Transfer transfer = new Transfer(line2, station2);
            Transfer transfer2 = new Transfer(line1, station1);

            if (stationVertex1 != null && stationVertex2 != null) {
                stationVertex1.addTransfer(transfer);
                stationVertex2.addTransfer(transfer2);
            }
        } catch (Exception e) {
            System.out.println("List could not be found");
        }
    }

    public static List<String> getArguments(String s) {
        Pattern pattern = Pattern.compile("(/[\\w-]*)|\"([^\"]*)\"| ([^\" ]+)", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher m = pattern.matcher(s);
        List<String> arguments = new ArrayList<>();
        List<MatchResult> results = m.results().toList();

        results.forEach(r -> {
            if (r.group(1) != null) {
                arguments.add(r.group(1));
            } else if (r.group(2) != null) {
                arguments.add(r.group(2));
            } else if (r.group(3) != null) {
                arguments.add(r.group(3));
            }
        });

        return arguments;
    }

    static Vertex getNodeByName(LinkedList<Vertex> list, String name) {

        Optional<Vertex> vertex = list.stream().filter(v -> v.getName().equals(name)).findFirst();
        return vertex.orElse(null);
    }


    public static Dictionary[] dijkstra(Map<String, LinkedList<Vertex>> g, Vertex startingVertex, boolean fastestRoute) {

        Dictionary<String, Integer> distances = new Hashtable<>();
        Dictionary<String, Vertex> previous = new Hashtable<>();
        PriorityQueue<QueueObject> queue = new PriorityQueue<>();
        queue.add(new QueueObject(startingVertex, 0));


        for (Vertex v : getAllNodes(g)) {
            if (v != startingVertex) {
                distances.put(v.getName(), Integer.MAX_VALUE);
            }
            previous.put(v.getName(), new Vertex("Null"));
        }

        distances.put(startingVertex.getName(), 0);

        while (queue.size() != 0) {

            Vertex current = queue.poll().vertex;

            List<Edge> edges = current.getEdges();
            for (Edge e : edges) {

                Integer alternate;


                // /route command uses 1 as weight. /fastest-route uses weighted values.
                if (fastestRoute) {
                    alternate = e.getWeight() + distances.get(current.getName());
                } else {
                    alternate = 1 + distances.get(current.getName());
                }

                String neighborValue = e.getEnd().getName();

                if (alternate < distances.get(neighborValue)) {
                    distances.put(neighborValue, alternate);
                    previous.put(neighborValue, current);
                    queue.add(new QueueObject(e.getEnd(), distances.get(neighborValue)));
                }
            }
        }
        return new Dictionary[]{distances, previous};
    }

    public static List<Vertex> getAllNodes(Map<String, LinkedList<Vertex>> map) {
        List<Vertex> allVertices = new ArrayList<>();
        for (Map.Entry<String, LinkedList<Vertex>> line : map.entrySet()) {
            allVertices.addAll(line.getValue());
        }
        return allVertices;
    }

    public static void dijkstraResultPrinter(Dictionary[] d) {
        System.out.println("Distances:\n");
        for (Enumeration keys = d[0].keys(); keys.hasMoreElements(); ) {
            String nextKey = keys.nextElement().toString();
            System.out.println(nextKey + ": " + d[0].get(nextKey));
        }
        System.out.println("\nPrevious:\n");
        for (Enumeration keys = d[1].keys(); keys.hasMoreElements(); ) {
            String nextKey = keys.nextElement().toString();
            Vertex nextVertex = (Vertex) d[1].get(nextKey);
            System.out.println(nextKey + ": " + nextVertex.getName());
        }
    }

    public static void shortestPathBetween(Map<String, LinkedList<Vertex>> g, Vertex startingVertex, Vertex targetVertex, boolean fastestRoute) {
        Dictionary[] dijkstraDicts;

        if (fastestRoute) {
            dijkstraDicts = dijkstra(g, startingVertex, true);
        } else {
            dijkstraDicts = dijkstra(g, startingVertex, false);
        }

        Dictionary distances = dijkstraDicts[0];
        Dictionary previous = dijkstraDicts[1];

        ArrayList<Vertex> path = new ArrayList<>();
        Vertex v = targetVertex;


        while (!Objects.equals(v.getName(), "Null")) {
            path.add(0, v);
            v = (Vertex) previous.get(v.getName());
        }

        int time = (int) distances.get(targetVertex.getName());
        for (Vertex pathVertex : path) {
            int currentPathIndex = path.indexOf(pathVertex);

            try {

                Vertex nextVertex = path.get(currentPathIndex + 1);
                String currentLine = getLineByNode(g, pathVertex);
                String nextNodeLine = getLineByNode(g, nextVertex);

                if (pathVertex.getTransfer().size() != 0 && !currentLine.equals(nextNodeLine)) {
                    System.out.println(pathVertex.getName());
                    System.out.println("Transition to line " + nextNodeLine);
                    System.out.println(pathVertex.getName());
                    continue;
                }
            } catch (IndexOutOfBoundsException ignore) {

            }

            System.out.println(pathVertex.getName());

        }

        if (fastestRoute) {
            System.out.println("Total: " + time + " minutes in the way");
        }
    }

    public static String getLineByNode(Map<String, LinkedList<Vertex>> map, Vertex vertex) {
        for (Map.Entry<String, LinkedList<Vertex>> entry : map.entrySet()) {
            if (entry.getValue().contains(vertex)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // utility method for debugging the edges weight values
    static void printWeights(LinkedList<Vertex> list) {
        list.forEach(current -> {
            List<Edge> edges = current.getEdges();
            for (Edge e : edges) {
                System.out.println(e.getStart().getName() + " to " + e.getEnd().getName() + ": " + e.getWeight());
            }
        });
    }
}

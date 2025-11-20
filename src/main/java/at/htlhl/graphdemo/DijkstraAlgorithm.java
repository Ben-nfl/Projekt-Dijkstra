package at.htlhl.graphdemo;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;

import java.util.*;

/**
 * Implementierung des Dijkstra-Algorithmus zur Berechnung des kürzesten Pfades
 * zwischen zwei Knoten in einem Graphen.
 */
public class DijkstraAlgorithm {

    private final Graph<VertaxData, EdgeData> graph;

    /**
     * Konstruktor für den Dijkstra-Algorithmus.
     * @param graph Der Graph, auf dem der Algorithmus arbeitet
     */
    public DijkstraAlgorithm(Graph<VertaxData, EdgeData> graph) {
        this.graph = graph;
    }

    /**
     * Berechnet den kürzesten Pfad zwischen zwei Knoten.
     * @param startVertex Startknoten
     * @param endVertex Zielknoten
     * @return DijkstraResult mit Pfad, Distanz und verwendeten Kanten
     */
    public DijkstraResult findShortestPath(Vertex<VertaxData> startVertex, Vertex<VertaxData> endVertex) {
        // Validierung
        if (startVertex == null || endVertex == null) {
            return new DijkstraResult(null, Double.POSITIVE_INFINITY, null);
        }

        // Initialisierung
        Map<Vertex<VertaxData>, Double> distances = new HashMap<>();
        Map<Vertex<VertaxData>, Vertex<VertaxData>> predecessors = new HashMap<>();
        Map<Vertex<VertaxData>, Edge<EdgeData, VertaxData>> predecessorEdges = new HashMap<>();
        PriorityQueue<VertexDistancePair> queue = new PriorityQueue<>();
        Set<Vertex<VertaxData>> visited = new HashSet<>();

        // Setze alle Distanzen auf unendlich
        for (Vertex<VertaxData> vertex : graph.vertices()) {
            distances.put(vertex, Double.POSITIVE_INFINITY);
        }

        // Distanz zum Startknoten ist 0
        distances.put(startVertex, 0.0);
        queue.add(new VertexDistancePair(startVertex, 0.0));

        // Dijkstra-Algorithmus
        while (!queue.isEmpty()) {
            VertexDistancePair current = queue.poll();
            Vertex<VertaxData> currentVertex = current.vertex;

            // Überspringe bereits besuchte Knoten
            if (visited.contains(currentVertex)) {
                continue;
            }

            visited.add(currentVertex);

            // Wenn wir den Zielknoten erreicht haben, können wir aufhören
            if (currentVertex.equals(endVertex)) {
                break;
            }

            // Betrachte alle ausgehenden Kanten
            Collection<Edge<EdgeData, VertaxData>> outboundEdges = graph.incidentEdges(currentVertex);

            for (Edge<EdgeData, VertaxData> edge : outboundEdges) {
                // Bestimme den Nachbarknoten
                Vertex<VertaxData> neighbor = null;
                Vertex<VertaxData>[] vertices = edge.vertices();

                if (vertices[0].equals(currentVertex)) {
                    neighbor = vertices[1];
                } else if (vertices[1].equals(currentVertex)) {
                    neighbor = vertices[0];
                }

                if (neighbor == null || visited.contains(neighbor)) {
                    continue;
                }

                // Berechne neue Distanz
                double edgeWeight = edge.element().getDistance();
                double newDistance = distances.get(currentVertex) + edgeWeight;

                // Wenn die neue Distanz kürzer ist, aktualisiere sie
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    predecessors.put(neighbor, currentVertex);
                    predecessorEdges.put(neighbor, edge);
                    queue.add(new VertexDistancePair(neighbor, newDistance));
                }
            }
        }

        // Prüfe, ob ein Pfad zum Zielknoten existiert
        if (distances.get(endVertex) == Double.POSITIVE_INFINITY) {
            return new DijkstraResult(null, Double.POSITIVE_INFINITY, null);
        }

        // Rekonstruiere den Pfad
        List<Vertex<VertaxData>> path = reconstructPath(predecessors, startVertex, endVertex);
        List<Edge<EdgeData, VertaxData>> edgesInPath = reconstructEdges(predecessorEdges, startVertex, endVertex);
        double totalDistance = distances.get(endVertex);

        return new DijkstraResult(path, totalDistance, edgesInPath);
    }

    /**
     * Rekonstruiert den Pfad vom Start zum Ziel anhand der Vorgänger-Map.
     */
    private List<Vertex<VertaxData>> reconstructPath(
            Map<Vertex<VertaxData>, Vertex<VertaxData>> predecessors,
            Vertex<VertaxData> start,
            Vertex<VertaxData> end) {

        List<Vertex<VertaxData>> path = new ArrayList<>();
        Vertex<VertaxData> current = end;

        while (current != null) {
            path.add(0, current);
            current = predecessors.get(current);
        }

        return path;
    }

    /**
     * Rekonstruiert die Liste der Kanten im kürzesten Pfad.
     */
    private List<Edge<EdgeData, VertaxData>> reconstructEdges(
            Map<Vertex<VertaxData>, Edge<EdgeData, VertaxData>> predecessorEdges,
            Vertex<VertaxData> start,
            Vertex<VertaxData> end) {

        List<Edge<EdgeData, VertaxData>> edges = new ArrayList<>();
        Vertex<VertaxData> current = end;

        while (!current.equals(start)) {
            Edge<EdgeData, VertaxData> edge = predecessorEdges.get(current);
            if (edge != null) {
                edges.add(0, edge);
            }

            Vertex<VertaxData> prev = null;
            for (Map.Entry<Vertex<VertaxData>, Edge<EdgeData, VertaxData>> entry : predecessorEdges.entrySet()) {
                if (entry.getKey().equals(current) && entry.getValue() != null) {
                    Vertex<VertaxData>[] vertices = entry.getValue().vertices();
                    prev = vertices[0].equals(current) ? vertices[1] :
                           vertices[1].equals(current) ? vertices[0] : null;
                    break;
                }
            }

            if (prev == null) break;
            current = prev;
        }

        return edges;
    }

    /**
     * Hilfsklasse für die Priority Queue.
     */
    private static class VertexDistancePair implements Comparable<VertexDistancePair> {
        Vertex<VertaxData> vertex;
        double distance;

        VertexDistancePair(Vertex<VertaxData> vertex, double distance) {
            this.vertex = vertex;
            this.distance = distance;
        }

        @Override
        public int compareTo(VertexDistancePair other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    /**
     * Container-Klasse für das Ergebnis des Dijkstra-Algorithmus.
     */
    public static class DijkstraResult {
        private final List<Vertex<VertaxData>> path;
        private final double totalDistance;
        private final List<Edge<EdgeData, VertaxData>> edges;

        public DijkstraResult(List<Vertex<VertaxData>> path, double totalDistance,
                            List<Edge<EdgeData, VertaxData>> edges) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.edges = edges;
        }

        public boolean pathExists() {
            return path != null && !path.isEmpty() && totalDistance != Double.POSITIVE_INFINITY;
        }

        public List<Vertex<VertaxData>> getPath() {
            return path;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public List<Edge<EdgeData, VertaxData>> getEdges() {
            return edges;
        }

        public String getPathAsString() {
            if (!pathExists()) {
                return "Kein Pfad gefunden";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).element().getName());
                if (i < path.size() - 1) {
                    sb.append(" → ");
                }
            }
            return sb.toString();
        }
    }
}

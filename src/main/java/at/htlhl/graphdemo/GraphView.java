package at.htlhl.graphdemo;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartStylableNode;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphView ist die Benutzeroberfläche für die Dijkstra-Visualisierung.
 * Enthält den Graphen und die Steuerungselemente für die Pfadberechnung.
 */
public class GraphView extends BorderPane {

    private SmartGraphPanel<VertaxData, EdgeData> smartGraphPanel;
    private ContentZoomScrollPane contentZoomScrollPane;
    private final GraphControl graphControl;

    // UI-Komponenten
    private ComboBox<Vertex<VertaxData>> startNodeComboBox;
    private ComboBox<Vertex<VertaxData>> endNodeComboBox;
    private Button calculateButton;
    private Button resetButton;

    // Dijkstra-Algorithmus
    private DijkstraAlgorithm dijkstraAlgorithm;

    // Speichert die hervorgehobenen Elemente für das Zurücksetzen
    private List<Edge<EdgeData, VertaxData>> highlightedEdges = new ArrayList<>();

    public GraphView(GraphControl graphControl) {
        super();

        this.graphControl = graphControl;
        this.dijkstraAlgorithm = new DijkstraAlgorithm(graphControl.getGraph());

        // Graphen-Visualisierung einrichten
        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        smartGraphPanel = new SmartGraphPanel<>(graphControl.getGraph(), strategy);
        smartGraphPanel.setAutomaticLayout(true);

        // CSS-Styles für Hervorhebung definieren
        String css = """
            .highlighted-edge {
                -fx-stroke: #4CAF50;
                -fx-stroke-width: 4;
            }
            .highlighted-vertex {
                -fx-fill: #90EE90;
                -fx-stroke: #2E7D32;
                -fx-stroke-width: 3;
            }
            """;
        smartGraphPanel.setStyle(css);

        contentZoomScrollPane = new ContentZoomScrollPane(smartGraphPanel);
        setCenter(contentZoomScrollPane);

        // Toolbar mit Steuerungselementen erstellen
        ToolBar toolBar = createToolbar();
        setTop(toolBar);
    }

    /**
     * Erstellt die Toolbar mit allen Steuerungselementen.
     */
    private ToolBar createToolbar() {
        // Labels
        Label titleLabel = new Label("Dijkstra-Algorithmus:");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label startLabel = new Label("Start:");
        Label endLabel = new Label("Ziel:");

        // ComboBoxen für Start- und Endknoten
        startNodeComboBox = new ComboBox<>();
        endNodeComboBox = new ComboBox<>();

        // Fülle ComboBoxen mit allen Knoten
        for (Vertex<VertaxData> vertex : graphControl.getGraph().vertices()) {
            startNodeComboBox.getItems().add(vertex);
            endNodeComboBox.getItems().add(vertex);
        }

        // Verwende den Namen der Stadt für die Anzeige
        startNodeComboBox.setCellFactory(param -> new VertexListCell());
        startNodeComboBox.setButtonCell(new VertexListCell());
        endNodeComboBox.setCellFactory(param -> new VertexListCell());
        endNodeComboBox.setButtonCell(new VertexListCell());

        startNodeComboBox.setPromptText("Wählen...");
        endNodeComboBox.setPromptText("Wählen...");

        // Buttons
        calculateButton = new Button("Kürzesten Pfad berechnen");
        calculateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        calculateButton.setOnAction(e -> calculateShortestPath());

        resetButton = new Button("Zurücksetzen");
        resetButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        resetButton.setOnAction(e -> resetVisualization());

        // HBox für besseres Layout
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));
        hbox.getChildren().addAll(
            titleLabel,
            new Separator(),
            startLabel, startNodeComboBox,
            endLabel, endNodeComboBox,
            calculateButton, resetButton
        );

        return new ToolBar(hbox);
    }

    /**
     * Berechnet den kürzesten Pfad und zeigt das Ergebnis an.
     */
    private void calculateShortestPath() {
        // Validierung: Prüfe, ob beide Knoten ausgewählt wurden
        Vertex<VertaxData> startVertex = startNodeComboBox.getValue();
        Vertex<VertaxData> endVertex = endNodeComboBox.getValue();

        if (startVertex == null || endVertex == null) {
            showAlert(Alert.AlertType.WARNING, "Unvollständige Eingabe",
                    "Bitte wählen Sie sowohl einen Startknoten als auch einen Zielknoten aus.");
            return;
        }

        // Prüfe, ob Start und Ziel unterschiedlich sind
        if (startVertex.equals(endVertex)) {
            showAlert(Alert.AlertType.INFORMATION, "Gleicher Knoten",
                    "Startknoten und Zielknoten sind identisch.\n\nDistanz: 0");
            return;
        }

        // Führe den Dijkstra-Algorithmus aus
        DijkstraAlgorithm.DijkstraResult result = dijkstraAlgorithm.findShortestPath(startVertex, endVertex);

        // Prüfe, ob ein Pfad gefunden wurde
        if (!result.pathExists()) {
            showAlert(Alert.AlertType.ERROR, "Kein Pfad gefunden",
                    "Es existiert kein Pfad zwischen " +
                    startVertex.element().getName() + " und " +
                    endVertex.element().getName() + ".");
            return;
        }

        // Zeige das Ergebnis in einem Alert
        String message = "Kürzester Pfad:\n" + result.getPathAsString() +
                "\n\nGesamtdistanz: " + (int)result.getTotalDistance() + " km";
        showAlert(Alert.AlertType.INFORMATION, "Ergebnis - Kürzester Pfad", message);

        // Hebe den gefundenen Pfad visuell hervor
        highlightPath(result);
    }

    /**
     * Hebt den gefundenen Pfad visuell hervor.
     */
    private void highlightPath(DijkstraAlgorithm.DijkstraResult result) {
        // Setze zuerst die Auswahl nicht zurück, nur die Styles
        resetStyles();

        if (result == null || !result.pathExists()) {
            return;
        }

        // Speichere die hervorgehobenen Kanten
        highlightedEdges = result.getEdges();

        // Hebe die Kanten des Pfades hervor
        if (highlightedEdges != null) {
            for (Edge<EdgeData, VertaxData> edge : highlightedEdges) {
                SmartStylableNode graphEdge = smartGraphPanel.getStylableEdge(edge);
                if (graphEdge != null) {
                    // Füge die CSS-Klasse für hervorgehobene Kanten hinzu
                    graphEdge.addStyleClass("highlighted-edge");
                }
            }
        }

        // Hebe die Knoten des Pfades hervor
        if (result.getPath() != null) {
            for (Vertex<VertaxData> vertex : result.getPath()) {
                SmartStylableNode graphVertex = smartGraphPanel.getStylableVertex(vertex);
                if (graphVertex != null) {
                    // Füge die CSS-Klasse für hervorgehobene Knoten hinzu
                    graphVertex.addStyleClass("highlighted-vertex");
                }
            }
        }

        // Aktualisiere die Darstellung
        smartGraphPanel.update();
    }

    /**
     * Setzt nur die Styles zurück (entfernt Hervorhebungen), aber nicht die Auswahl.
     */
    private void resetStyles() {
        // Setze alle Kanten-Styles zurück
        for (Edge<EdgeData, VertaxData> edge : graphControl.getGraph().edges()) {
            SmartStylableNode graphEdge = smartGraphPanel.getStylableEdge(edge);
            if (graphEdge != null) {
                graphEdge.removeStyleClass("highlighted-edge");
            }
        }

        // Setze alle Knoten-Styles zurück
        for (Vertex<VertaxData> vertex : graphControl.getGraph().vertices()) {
            SmartStylableNode graphVertex = smartGraphPanel.getStylableVertex(vertex);
            if (graphVertex != null) {
                graphVertex.removeStyleClass("highlighted-vertex");
            }
        }

        // Leere die Liste der hervorgehobenen Kanten
        highlightedEdges.clear();
    }

    /**
     * Setzt die Visualisierung zurück (entfernt Hervorhebungen und Auswahl).
     */
    private void resetVisualization() {
        // Setze Styles zurück
        resetStyles();

        // Setze die Auswahl zurück
        startNodeComboBox.setValue(null);
        endNodeComboBox.setValue(null);

        // Aktualisiere die Darstellung
        smartGraphPanel.update();
    }

    /**
     * Zeigt ein Alert-Fenster an.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Wird aufgerufen, nachdem die Szene sichtbar ist.
     * Initialisiert den SmartGraphPanel.
     */
    public void initAfterVisible() {
        smartGraphPanel.init();
    }

    /**
     * Custom ListCell für die Darstellung von Vertices in ComboBoxen.
     */
    private static class VertexListCell extends ListCell<Vertex<VertaxData>> {
        @Override
        protected void updateItem(Vertex<VertaxData> vertex, boolean empty) {
            super.updateItem(vertex, empty);
            if (empty || vertex == null) {
                setText(null);
            } else {
                setText(vertex.element().getName());
            }
        }
    }
}

package at.htlhl.graphdemo;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private Label infoLabel;

    // Dijkstra-Algorithmus
    private DijkstraAlgorithm dijkstraAlgorithm;

    // Speichert die hervorgehobenen Elemente für das Zurücksetzen
    private List<Edge<EdgeData, VertaxData>> highlightedEdges = new ArrayList<>();

    // Doppelklick-State: true = nächster Klick setzt Start, false = nächster Klick setzt Ziel
    private boolean nextClickIsStart = true;

    public GraphView(GraphControl graphControl) {
        super();

        this.graphControl = graphControl;
        this.dijkstraAlgorithm = new DijkstraAlgorithm(graphControl.getGraph());

        // Graphen-Visualisierung einrichten
        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        smartGraphPanel = new SmartGraphPanel<>(graphControl.getGraph(), strategy);
        smartGraphPanel.setAutomaticLayout(true);

        contentZoomScrollPane = new ContentZoomScrollPane(smartGraphPanel);
        setCenter(contentZoomScrollPane);

        // Toolbar mit Steuerungselementen erstellen
        ToolBar toolBar = createToolbar();
        setTop(toolBar);

        // Info-Panel am unteren Rand
        infoLabel = new Label("Tipp: Doppelklick auf einen Knoten setzt ihn als Start, ein weiterer Doppelklick auf einen anderen Knoten setzt ihn als Ziel und berechnet den Pfad.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-padding: 8px 12px; -fx-background-color: #ECEFF1; -fx-text-fill: #546E7A;");
        infoLabel.setMaxWidth(Double.MAX_VALUE);
        infoLabel.setAlignment(Pos.CENTER_LEFT);
        HBox infoBox = new HBox(infoLabel);
        infoBox.setStyle("-fx-border-color: #CFD8DC; -fx-border-width: 1 0 0 0;");
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(infoLabel, Insets.EMPTY);
        setBottom(infoBox);
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
            updateInfoLabel("Bitte wählen Sie sowohl einen Startknoten als auch einen Zielknoten aus.", "#FF9800");
            return;
        }

        // Prüfe, ob Start und Ziel unterschiedlich sind
        if (startVertex.equals(endVertex)) {
            updateInfoLabel("Startknoten und Zielknoten sind identisch. Distanz: 0", "#FF9800");
            return;
        }

        // Führe den Dijkstra-Algorithmus aus
        DijkstraAlgorithm.DijkstraResult result = dijkstraAlgorithm.findShortestPath(startVertex, endVertex);

        // Prüfe, ob ein Pfad gefunden wurde
        if (!result.pathExists()) {
            updateInfoLabel("Kein Pfad gefunden zwischen " +
                    startVertex.element().getName() + " und " +
                    endVertex.element().getName() + ".", "#F44336");
            return;
        }

        // Zeige das Ergebnis im Info-Panel
        String message = result.getPathAsString() + "  (" + (int) result.getTotalDistance() + " km)";
        updateInfoLabel(message, "#2E7D32");

        // Hebe den gefundenen Pfad visuell hervor
        highlightPath(result);
    }

    /**
     * Aktualisiert das Info-Label am unteren Rand.
     */
    private void updateInfoLabel(String text, String color) {
        infoLabel.setText(text);
        infoLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 12px; "
                + "-fx-background-color: #ECEFF1; -fx-text-fill: " + color + ";");
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
                    graphEdge.addStyleClass("highlighted-edge");
                }
                // Hebe auch das Kanten-Label hervor
                SmartStylableNode edgeLabel = smartGraphPanel.getStylableLabel(edge);
                if (edgeLabel != null) {
                    edgeLabel.setStyleInline("-fx-font: bold 8pt \"sans-serif\"; -fx-background-color: #4CAF50; -fx-text-fill: white;");
                }
            }
        }

        // Hebe die Knoten des Pfades hervor
        if (result.getPath() != null) {
            List<Vertex<VertaxData>> path = result.getPath();
            for (int i = 0; i < path.size(); i++) {
                Vertex<VertaxData> vertex = path.get(i);
                SmartStylableNode graphVertex = smartGraphPanel.getStylableVertex(vertex);
                if (graphVertex != null) {
                    if (i == 0) {
                        // Startknoten: orange
                        graphVertex.addStyleClass("start-vertex");
                    } else if (i == path.size() - 1) {
                        // Zielknoten: rot
                        graphVertex.addStyleClass("end-vertex");
                    } else {
                        // Zwischenknoten: hellgrün
                        graphVertex.addStyleClass("highlighted-vertex");
                    }
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
            SmartStylableNode edgeLabel = smartGraphPanel.getStylableLabel(edge);
            if (edgeLabel != null) {
                edgeLabel.setStyleInline("");
            }
        }

        // Setze alle Knoten-Styles zurück
        for (Vertex<VertaxData> vertex : graphControl.getGraph().vertices()) {
            SmartStylableNode graphVertex = smartGraphPanel.getStylableVertex(vertex);
            if (graphVertex != null) {
                graphVertex.removeStyleClass("highlighted-vertex");
                graphVertex.removeStyleClass("start-vertex");
                graphVertex.removeStyleClass("end-vertex");
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

        // Doppelklick-State zurücksetzen
        nextClickIsStart = true;

        // Info-Label zurücksetzen
        infoLabel.setText("Tipp: Doppelklick auf einen Knoten setzt ihn als Start, ein weiterer Doppelklick auf einen anderen Knoten setzt ihn als Ziel und berechnet den Pfad.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-padding: 8px 12px; -fx-background-color: #ECEFF1; -fx-text-fill: #546E7A;");

        // Aktualisiere die Darstellung
        smartGraphPanel.update();
    }

    /**
     * Wird aufgerufen, nachdem die Szene sichtbar ist.
     * Initialisiert den SmartGraphPanel und richtet Kontextmenüs und Doppelklick-Handler ein.
     */
    public void initAfterVisible() {
        smartGraphPanel.init();

        // Doppelklick-Handler einrichten
        setupDoubleClickHandler();

        // Kontextmenüs auf alle Knoten setzen
        setupContextMenus();
    }

    /**
     * Richtet den Doppelklick-Handler für Knoten ein.
     */
    private void setupDoubleClickHandler() {
        smartGraphPanel.setVertexDoubleClickAction(smartVertex -> {
            Vertex<VertaxData> vertex = smartVertex.getUnderlyingVertex();

            if (nextClickIsStart) {
                // Setze als Startknoten
                startNodeComboBox.setValue(vertex);
                nextClickIsStart = false;

                // Vorherige Hervorhebung zurücksetzen
                resetStyles();

                // Startknoten visuell markieren
                SmartStylableNode graphVertex = smartGraphPanel.getStylableVertex(vertex);
                if (graphVertex != null) {
                    graphVertex.addStyleClass("start-vertex");
                }
                smartGraphPanel.update();

                updateInfoLabel("Startknoten: " + vertex.element().getName() + "  —  Doppelklick auf Zielknoten...", "#E65100");
            } else {
                // Setze als Zielknoten
                endNodeComboBox.setValue(vertex);
                nextClickIsStart = true;

                // Automatisch Pfad berechnen
                calculateShortestPath();
            }
        });
    }

    /**
     * Erstellt Kontextmenüs für alle Knoten im Graphen.
     */
    private void setupContextMenus() {
        for (SmartGraphVertex<VertaxData> smartVertex : smartGraphPanel.getSmartVertices()) {
            if (smartVertex instanceof Node node) {
                ContextMenu contextMenu = createVertexContextMenu(smartVertex);
                node.setOnContextMenuRequested(event -> {
                    contextMenu.show(node, event.getScreenX(), event.getScreenY());
                    event.consume();
                });
            }
        }
    }

    /**
     * Erstellt ein Kontextmenü für einen Knoten.
     */
    private ContextMenu createVertexContextMenu(SmartGraphVertex<VertaxData> smartVertex) {
        Vertex<VertaxData> vertex = smartVertex.getUnderlyingVertex();
        String name = vertex.element().getName();

        ContextMenu menu = new ContextMenu();

        MenuItem setStart = new MenuItem("Als Startknoten setzen (" + name + ")");
        setStart.setOnAction(e -> {
            startNodeComboBox.setValue(vertex);
            resetStyles();
            SmartStylableNode gv = smartGraphPanel.getStylableVertex(vertex);
            if (gv != null) gv.addStyleClass("start-vertex");
            smartGraphPanel.update();
            nextClickIsStart = false;
            updateInfoLabel("Startknoten: " + name + "  —  Wählen Sie einen Zielknoten.", "#E65100");
        });

        MenuItem setEnd = new MenuItem("Als Zielknoten setzen (" + name + ")");
        setEnd.setOnAction(e -> {
            endNodeComboBox.setValue(vertex);
            nextClickIsStart = true;

            // Wenn Start bereits gewählt, Zielknoten markieren
            if (startNodeComboBox.getValue() != null) {
                SmartStylableNode gv = smartGraphPanel.getStylableVertex(vertex);
                if (gv != null) gv.addStyleClass("end-vertex");
                smartGraphPanel.update();
                updateInfoLabel("Zielknoten: " + name + "  —  Klicken Sie \"Kürzesten Pfad berechnen\".", "#B71C1C");
            }
        });

        MenuItem calcPath = new MenuItem("Kürzesten Pfad von hier berechnen");
        calcPath.setOnAction(e -> {
            startNodeComboBox.setValue(vertex);
            if (endNodeComboBox.getValue() != null && !endNodeComboBox.getValue().equals(vertex)) {
                calculateShortestPath();
            } else {
                resetStyles();
                SmartStylableNode gv = smartGraphPanel.getStylableVertex(vertex);
                if (gv != null) gv.addStyleClass("start-vertex");
                smartGraphPanel.update();
                nextClickIsStart = false;
                updateInfoLabel("Startknoten: " + name + "  —  Wählen Sie einen Zielknoten.", "#E65100");
            }
        });

        menu.getItems().addAll(setStart, setEnd, new SeparatorMenuItem(), calcPath);
        return menu;
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

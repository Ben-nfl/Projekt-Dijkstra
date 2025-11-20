# GraphDemo - Dijkstra-Algorithmus Visualisierung

Eine JavaFX-Anwendung zur Visualisierung des Dijkstra-Algorithmus mit der SmartGraph-Bibliothek.

## Funktionen

✓ **Datensatz**: Graph mit 4 Städten (Houston, Dallas, Chicago, Boston) und Distanzen
✓ **Dijkstra-Implementierung**: Algorithmus zur Berechnung des kürzesten Wegs
✓ **Startknoten wählbar**: Auswahl über Dropdown-Menü
✓ **Endknoten wählbar**: Auswahl über Dropdown-Menü
✓ **Fehlermeldungen als Alert**: Bei fehlenden Eingaben oder nicht existierendem Pfad
✓ **Ergebnis als Alert + Visuell**: Distanz als Popup + Hervorhebung des Pfades im Graphen

## Graph-Datensatz

Die Anwendung enthält einen Graphen mit **15 US-Städten**:

- Seattle, San Francisco, Los Angeles, Riverside, Phoenix
- Chicago, Boston, New York, Atlanta, Miami
- Dallas, Houston, Detroit, Philadelphia, Washington

Mit **26 Verbindungen** zwischen den Städten, z.B.:
- Seattle ↔ Chicago: 1737 km
- Dallas ↔ Houston: 225 km
- Boston ↔ New York: 190 km
- Los Angeles ↔ Riverside: 50 km

## Projekt ausführen

### Mit Maven (empfohlen):

```bash
mvn clean javafx:run
```

### Mit IDE (IntelliJ IDEA / Eclipse):

1. Öffnen Sie das Projekt als Maven-Projekt
2. Führen Sie die Klasse `at.htlhl.graphdemo.App` aus

## Verwendung

1. Wählen Sie im Dropdown-Menü **Start** eine Startstadt aus
2. Wählen Sie im Dropdown-Menü **Ziel** eine Zielstadt aus
3. Klicken Sie auf **"Kürzesten Pfad berechnen"**
4. Das Ergebnis wird als Alert-Fenster angezeigt
5. Der kürzeste Pfad wird im Graphen grün hervorgehoben
6. Mit **"Zurücksetzen"** können Sie die Auswahl zurücksetzen

## Projektstruktur

```
src/main/java/at/htlhl/graphdemo/
├── App.java                  - Hauptklasse (Entry Point)
├── GraphControl.java         - Graph-Modell mit Datensatz
├── GraphView.java            - UI-Komponente mit Dijkstra-Integration
├── DijkstraAlgorithm.java    - Dijkstra-Implementierung
├── VertaxData.java           - Knotendaten (Stadt)
└── EdgeData.java             - Kantendaten (Distanz)
```

## Technologien

- **Java 21**
- **JavaFX 21.0.6**
- **SmartGraph 2.3.0** - Graph-Visualisierungsbibliothek
- **Maven** - Build-Tool

## Architektur

Das Projekt folgt dem MVC-Muster:
- **Model**: `GraphControl`, `VertaxData`, `EdgeData`
- **View**: `GraphView` mit SmartGraph-Panel
- **Controller**: `DijkstraAlgorithm`

Die klare Trennung zwischen Geschäftslogik (Dijkstra) und Darstellung (GraphView) ermöglicht einfache Wartung und Erweiterbarkeit.

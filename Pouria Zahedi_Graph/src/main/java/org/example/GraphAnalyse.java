package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Graphen-Analyse-Programm für das Abgabeprojekt.
 * Das Programm berechnet Distanzmatrix, Exzentrizität, Radius, Durchmesser und Zentrum.
 */
public class GraphAnalyse {

    private int[][] adjazenzMatrix;
    private int anzahlKnoten;
    // Unendlich-Wert für die Distanzberechnung (Floyd-Warshall)
    private final int UNENDLICH = 999999;

    public static void main(String[] args) {
        // Prüfung, ob eine Datei beim Start angegeben wurde
        if (args.length < 1) {
            System.out.println("Fehler: Bitte geben Sie eine CSV-Datei als Argument an.");
            System.out.println("Beispiel: java at.graphproject.GraphAnalyse graph.csv");
            return;
        }

        GraphAnalyse programm = new GraphAnalyse();
        programm.ausfuehren(args[0]);
    }

    public void ausfuehren(String dateiname) {
        if (ladeCSV(dateiname)) {
            berechneUndZeigeErgebnisse();
        }
    }

    /**
     * Liest die Adjazenzmatrix aus einer CSV-Datei ein.
     * Unterstützt Semikolons (;) als Trennzeichen.
     */
    private boolean ladeCSV(String dateiname) {
        List<int[]> zeilen = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(dateiname))) {
            String zeile;
            while ((zeile = br.readLine()) != null) {
                if (zeile.trim().isEmpty()) continue;

                String[] werte = zeile.split(";");
                int[] zahlen = new int[werte.length];
                for (int i = 0; i < werte.length; i++) {
                    zahlen[i] = Integer.parseInt(werte[i].trim());
                }
                zeilen.add(zahlen);
            }

            this.anzahlKnoten = zeilen.size();
            this.adjazenzMatrix = zeilen.toArray(new int[anzahlKnoten][]);
            System.out.println("Graph geladen. Anzahl der Knoten: " + anzahlKnoten);
            return true;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hauptlogik zur Berechnung der Graphen-Metriken.
     */
    private void berechneUndZeigeErgebnisse() {
        // 1. Distanzmatrix berechnen (kürzeste Pfade zwischen allen Knoten)
        int[][] distanzen = berechneDistanzen();

        // 2. Exzentrizitäten berechnen (maximaler Abstand eines Knotens zu allen anderen)
        int[] exzentrizitaeten = new int[anzahlKnoten];
        for (int i = 0; i < anzahlKnoten; i++) {
            int maxDistanz = 0;
            for (int j = 0; j < anzahlKnoten; j++) {
                if (distanzen[i][j] != UNENDLICH) {
                    maxDistanz = Math.max(maxDistanz, distanzen[i][j]);
                }
            }
            exzentrizitaeten[i] = maxDistanz;
        }

        // 3. Radius und Durchmesser bestimmen
        int radius = UNENDLICH;
        int durchmesser = 0;
        for (int ex : exzentrizitaeten) {
            if (ex < radius) radius = ex;
            if (ex > durchmesser) durchmesser = ex;
        }

        // 4. Zentrum bestimmen (Knoten, deren Exzentrizität gleich dem Radius ist)
        List<Integer> zentrum = new ArrayList<>();
        for (int i = 0; i < anzahlKnoten; i++) {
            if (exzentrizitaeten[i] == radius) {
                zentrum.add(i);
            }
        }

        ausgabe(distanzen, exzentrizitaeten, radius, durchmesser, zentrum);
    }

    /**
     * Implementierung des Floyd-Warshall Algorithmus.
     * Sucht schrittweise nach kürzeren Wegen über Zwischenknoten.
     */
    private int[][] berechneDistanzen() {
        int[][] dist = new int[anzahlKnoten][anzahlKnoten];

        for (int i = 0; i < anzahlKnoten; i++) {
            for (int j = 0; j < anzahlKnoten; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else if (adjazenzMatrix[i][j] > 0) {
                    dist[i][j] = adjazenzMatrix[i][j];
                } else {
                    dist[i][j] = UNENDLICH;
                }
            }
        }

        // Floyd-Warshall Kern-Algorithmus
        for (int k = 0; k < anzahlKnoten; k++) {
            for (int i = 0; i < anzahlKnoten; i++) {
                for (int j = 0; j < anzahlKnoten; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        return dist;
    }

    private void ausgabe(int[][] dist, int[] ex, int rad, int dm, List<Integer> zentrum) {
        System.out.println("\n--- Distanzmatrix ---");
        for (int[] reihe : dist) {
            System.out.println(Arrays.toString(reihe).replace(String.valueOf(UNENDLICH), "INF"));
        }

        System.out.println("\n--- Knotenkennzahlen ---");
        for (int i = 0; i < anzahlKnoten; i++) {
            System.out.println("Knoten " + i + ": Exzentrizität = " + ex[i]);
        }

        System.out.println("\nRadius: " + rad);
        System.out.println("Durchmesser: " + dm);
        System.out.println("Zentrum (Knoten-IDs): " + zentrum);
    }
}
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// Clase principal MapaGrafosApp
public class MapaGrafosApp extends JFrame {

    public MapaGrafosApp() {
        setTitle("Mapa de Grafos");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Instanciamos el panel de grafo
        GrafoPanel grafoPanel = new GrafoPanel();
        add(grafoPanel, BorderLayout.CENTER);

        // Agregamos botones de acción
        JPanel controlPanel = new JPanel();
        JButton findPathButton = new JButton("Encontrar Camino");
        JButton editModeButton = new JButton("Modo Editor");
        JButton connectNodesButton = new JButton("Conectar Nodos");

        findPathButton.addActionListener(e -> grafoPanel.buscarCamino());
        editModeButton.addActionListener(e -> grafoPanel.cambiarModoEditor());
        connectNodesButton.addActionListener(e -> grafoPanel.conectarNodos());

        controlPanel.add(findPathButton);
        controlPanel.add(editModeButton);
        controlPanel.add(connectNodesButton);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    class GrafoPanel extends JPanel {
        private java.util.List<Nodo> nodos;
        private java.util.List<Arista> aristas;
        private boolean modoEditor = false;
        private Nodo primerNodoSeleccionado = null;

        public GrafoPanel() {
            nodos = new ArrayList<>();
            aristas = new ArrayList<>();

            // Configurar eventos de clic para agregar o seleccionar nodos en modo editor
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (modoEditor) {
                        Nodo nodoSeleccionado = obtenerNodoEnPosicion(e.getX(), e.getY());
                        if (nodoSeleccionado == null) {
                            // Si no hay un nodo en la posición, agregamos un nuevo nodo
                            Nodo nodo = new Nodo(e.getX(), e.getY(), "Nodo" + (nodos.size() + 1));
                            nodos.add(nodo);
                            repaint();
                        } else {
                            // Si ya se seleccionó un primer nodo, conectar al segundo nodo seleccionado
                            if (primerNodoSeleccionado == null) {
                                primerNodoSeleccionado = nodoSeleccionado;
                                JOptionPane.showMessageDialog(GrafoPanel.this, "Primer nodo seleccionado: " + primerNodoSeleccionado.getNombre());
                            } else {
                                int distancia = Integer.parseInt(JOptionPane.showInputDialog("Distancia entre " + primerNodoSeleccionado.getNombre() + " y " + nodoSeleccionado.getNombre() + ":"));
                                aristas.add(new Arista(primerNodoSeleccionado, nodoSeleccionado, distancia));
                                primerNodoSeleccionado = null;
                                repaint();
                            }
                        }
                    }
                }
            });
        }

        // Método para cambiar el modo editor
        public void cambiarModoEditor() {
            modoEditor = !modoEditor;
            String modo = modoEditor ? "Editor Activado" : "Editor Desactivado";
            JOptionPane.showMessageDialog(this, modo);
        }

        // Método para conectar nodos seleccionados manualmente
        public void conectarNodos() {
            if (!modoEditor) {
                JOptionPane.showMessageDialog(this, "Debes estar en el modo editor para conectar nodos.");
                return;
            }
            primerNodoSeleccionado = null;  // Reset para permitir nueva selección de nodos
            JOptionPane.showMessageDialog(this, "Haz clic en dos nodos para conectarlos.");
        }

        // Método para buscar el camino más corto entre nodos
        public void buscarCamino() {
            if (nodos.size() < 2) {
                JOptionPane.showMessageDialog(this, "Agrega al menos dos nodos.");
                return;
            }

            String inicio = JOptionPane.showInputDialog("Nodo de inicio:");
            String fin = JOptionPane.showInputDialog("Nodo de destino:");

            Nodo nodoInicio = obtenerNodoPorNombre(inicio);
            Nodo nodoFin = obtenerNodoPorNombre(fin);

            if (nodoInicio == null || nodoFin == null) {
                JOptionPane.showMessageDialog(this, "Nodos no encontrados.");
                return;
            }

            List<Nodo> camino = dijkstra(nodoInicio, nodoFin);
            if (camino != null) {
                int distanciaTotal = calcularDistanciaTotal(camino);
                JOptionPane.showMessageDialog(this, "Camino: " + camino + "\nDistancia total: " + distanciaTotal);
            } else {
                JOptionPane.showMessageDialog(this, "No hay camino entre los nodos seleccionados.");
            }
        }

        private List<Nodo> dijkstra(Nodo inicio, Nodo fin) {
            Map<Nodo, Nodo> anterior = new HashMap<>();
            Map<Nodo, Integer> distancias = new HashMap<>();
            PriorityQueue<Nodo> cola = new PriorityQueue<>(Comparator.comparingInt(distancias::get));

            for (Nodo nodo : nodos) {
                distancias.put(nodo, Integer.MAX_VALUE);
            }
            distancias.put(inicio, 0);
            cola.add(inicio);

            while (!cola.isEmpty()) {
                Nodo actual = cola.poll();

                if (actual.equals(fin)) break;

                for (Arista arista : aristas) {
                    if (arista.getNodoA().equals(actual) || arista.getNodoB().equals(actual)) {
                        Nodo vecino = arista.getOtroNodo(actual);
                        int nuevaDistancia = distancias.get(actual) + arista.getDistancia();

                        if (nuevaDistancia < distancias.get(vecino)) {
                            distancias.put(vecino, nuevaDistancia);
                            anterior.put(vecino, actual);
                            cola.add(vecino);
                        }
                    }
                }
            }

            List<Nodo> camino = new ArrayList<>();
            Nodo paso = fin;

            if (anterior.get(paso) == null) return null;

            while (paso != null) {
                camino.add(0, paso);
                paso = anterior.get(paso);
            }
            return camino;
        }

        private int calcularDistanciaTotal(List<Nodo> camino) {
            int distanciaTotal = 0;
            for (int i = 0; i < camino.size() - 1; i++) {
                Nodo nodoActual = camino.get(i);
                Nodo nodoSiguiente = camino.get(i + 1);
                for (Arista arista : aristas) {
                    if ((arista.getNodoA().equals(nodoActual) && arista.getNodoB().equals(nodoSiguiente)) ||
                            (arista.getNodoA().equals(nodoSiguiente) && arista.getNodoB().equals(nodoActual))) {
                        distanciaTotal += arista.getDistancia();
                        break;
                    }
                }
            }
            return distanciaTotal;
        }

        private Nodo obtenerNodoPorNombre(String nombre) {
            return nodos.stream().filter(n -> n.getNombre().equals(nombre)).findFirst().orElse(null);
        }

        private Nodo obtenerNodoEnPosicion(int x, int y) {
            for (Nodo nodo : nodos) {
                if (Math.hypot(nodo.x - x, nodo.y - y) < 20) {
                    return nodo;
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (Arista arista : aristas) {
                arista.dibujar(g);
            }

            for (Nodo nodo : nodos) {
                nodo.dibujar(g);
            }
        }
    }

    class Nodo {
        private int x, y;
        private String nombre;

        public Nodo(int x, int y, String nombre) {
            this.x = x;
            this.y = y;
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }

        public void dibujar(Graphics g) {
            g.setColor(Color.BLUE);
            g.fillOval(x - 10, y - 10, 20, 20);
            g.setColor(Color.BLACK);
            g.drawString(nombre, x - 10, y - 15);
        }
    }

    class Arista {
        private Nodo nodoA, nodoB;
        private int distancia;

        public Arista(Nodo nodoA, Nodo nodoB, int distancia) {
            this.nodoA = nodoA;
            this.nodoB = nodoB;
            this.distancia = distancia;
        }

        public int getDistancia() {
            return distancia;
        }

        public Nodo getNodoA() {
            return nodoA;
        }

        public Nodo getNodoB() {
            return nodoB;
        }

        public Nodo getOtroNodo(Nodo nodo) {
            return nodo.equals(nodoA) ? nodoB : nodoA;
        }

        public void dibujar(Graphics g) {
            g.setColor(Color.GRAY);
            g.drawLine(nodoA.x, nodoA.y, nodoB.x, nodoB.y);
            int midX = (nodoA.x + nodoB.x) / 2;
            int midY = (nodoA.y + nodoB.y) / 2;
            g.drawString(String.valueOf(distancia), midX, midY);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MapaGrafosApp::new);
    }
}

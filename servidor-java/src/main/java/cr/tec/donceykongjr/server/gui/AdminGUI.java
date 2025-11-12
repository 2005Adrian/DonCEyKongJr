package cr.tec.donceykongjr.server.gui;

import cr.tec.donceykongjr.server.logic.GameManager;

import javax.swing.*;
import java.awt.*;

/**
 * Interfaz gráfica simple para administrar el servidor.
 */
public class AdminGUI extends JFrame {
    private GameManager gameManager;
    private JTextArea logArea;
    //private JLabel statusLabel;

    public AdminGUI(GameManager gameManager) {
        this.gameManager = gameManager;
        inicializarVentana();
    }

    private void inicializarVentana() {
        setTitle("DonCEy Kong Jr - Admin");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel superior con título
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(44, 62, 80));
        JLabel titleLabel = new JLabel("SERVIDOR DONCEY KONG JR");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH);

        // Panel central con botones
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de control del juego
        JPanel controlPanel = crearPanelControl();
        centerPanel.add(controlPanel);

        // Panel de entidades
        JPanel entidadesPanel = crearPanelEntidades();
        centerPanel.add(entidadesPanel);

        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior con log
        JPanel bottomPanel = crearPanelLog();
        add(bottomPanel, BorderLayout.SOUTH);

        // Centrar ventana
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Control del Juego"));

        JButton pauseBtn = crearBoton("PAUSAR", new Color(231, 76, 60));
        pauseBtn.addActionListener(e -> {
            gameManager.setPausado(true);
            agregarLog("Juego PAUSADO");
        });

        JButton resumeBtn = crearBoton("REANUDAR", new Color(46, 204, 113));
        resumeBtn.addActionListener(e -> {
            gameManager.setPausado(false);
            agregarLog("Juego REANUDADO");
        });

        JButton listBtn = crearBoton("LISTAR", new Color(155, 89, 182));
        listBtn.addActionListener(e -> {
            String lista = gameManager.listarEntidades();
            agregarLog("ENTIDADES:\n" + lista);
        });

        panel.add(pauseBtn);
        panel.add(resumeBtn);
        panel.add(listBtn);

        return panel;
    }

    private JPanel crearPanelEntidades() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Crear Entidades"));

        JButton redBtn = crearBoton("COCODRILO ROJO", new Color(192, 57, 43));
        redBtn.addActionListener(e -> crearCocodriloRojo());

        JButton blueBtn = crearBoton("COCODRILO AZUL", new Color(41, 128, 185));
        blueBtn.addActionListener(e -> crearCocodriloAzul());

        JButton fruitBtn = crearBoton("FRUTA", new Color(243, 156, 18));
        fruitBtn.addActionListener(e -> crearFruta());

        panel.add(redBtn);
        panel.add(blueBtn);
        panel.add(fruitBtn);

        return panel;
    }

    private JPanel crearPanelLog() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log del Servidor"));

        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 60));

        // Efecto hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    private void crearCocodriloRojo() {
        JTextField lianaField = new JTextField("0");
        JTextField yField = new JTextField("100.0");

        Object[] message = {
            "Liana (0-4):", lianaField,
            "Posición Y (0.0-500.0):", yField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
            "Crear Cocodrilo Rojo", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int liana = Integer.parseInt(lianaField.getText());
                double y = Double.parseDouble(yField.getText());

                String error = gameManager.agregarCocodriloRojo(liana, y);
                if (error != null) {
                    JOptionPane.showMessageDialog(this, error,
                        "Error al crear cocodrilo", JOptionPane.ERROR_MESSAGE);
                } else {
                    agregarLog("Cocodrilo ROJO creado en liana " + liana + ", y=" + y);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: Valores numéricos inválidos",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void crearCocodriloAzul() {
        JTextField lianaField = new JTextField("0");
        JTextField yField = new JTextField("100.0");

        Object[] message = {
            "Liana (0-4):", lianaField,
            "Posición Y (0.0-500.0):", yField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
            "Crear Cocodrilo Azul", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int liana = Integer.parseInt(lianaField.getText());
                double y = Double.parseDouble(yField.getText());

                String error = gameManager.agregarCocodriloAzul(liana, y);
                if (error != null) {
                    JOptionPane.showMessageDialog(this, error,
                        "Error al crear cocodrilo", JOptionPane.ERROR_MESSAGE);
                } else {
                    agregarLog("Cocodrilo AZUL creado en liana " + liana + ", y=" + y);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: Valores numéricos inválidos",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void crearFruta() {
        JTextField lianaField = new JTextField("0");
        JTextField yField = new JTextField("200.0");
        JTextField puntosField = new JTextField("10");

        Object[] message = {
            "Liana (0-4):", lianaField,
            "Posición Y (0.0-500.0):", yField,
            "Puntos:", puntosField
        };

        int option = JOptionPane.showConfirmDialog(this, message,
            "Crear Fruta", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int liana = Integer.parseInt(lianaField.getText());
                double y = Double.parseDouble(yField.getText());
                int puntos = Integer.parseInt(puntosField.getText());

                String error = gameManager.agregarFruta(liana, y, puntos);
                if (error != null) {
                    JOptionPane.showMessageDialog(this, error,
                        "Error al crear fruta", JOptionPane.ERROR_MESSAGE);
                } else {
                    agregarLog("Fruta creada en liana " + liana + ", y=" + y + ", puntos=" + puntos);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: Valores numéricos inválidos",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensaje + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void mostrar() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            agregarLog("Servidor iniciado");
            agregarLog("Esperando conexiones en puerto 5000...");
        });
    }
}

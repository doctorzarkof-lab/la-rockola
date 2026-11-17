import componentes.PanelRedondeado;
import estilo.Estilo;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.io.File;
import java.util.function.Consumer;

public class BarraReproductor extends PanelRedondeado {

    private enum Tipo { ANTERIOR, SIGUIENTE, PLAY, PAUSA }

    private final JLabel portada = new JLabel("♪", SwingConstants.CENTER);
    private final JLabel titulo = new JLabel("");
    private final JLabel artista = new JLabel("");
    private final JLabel tiempo = new JLabel("00:00 / 00:00");
    private final Progreso progreso = new Progreso();
    private final BotonControl btnAnterior = new BotonControl(Tipo.ANTERIOR);
    private final BotonControl btnPlay = new BotonControl(Tipo.PAUSA);
    private final BotonControl btnSiguiente = new BotonControl(Tipo.SIGUIENTE);

    // alBuscar recibe un número entre 0 y 1 (la posición a la que se hizo clic)
    public BarraReproductor(Runnable alAnterior, Runnable alAlternar,
                            Runnable alSiguiente, Consumer<Double> alBuscar) {
        super(Estilo.CAFE_OSCURO, 25);
        setLayout(new BorderLayout(15, 0));
        setPreferredSize(new Dimension(0, 90));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));

        portada.setOpaque(true);
        portada.setBackground(Estilo.CAFE_OSCURO);
        portada.setForeground(Estilo.ARENA_CLARO);
        portada.setFont(Estilo.TITULO);
        portada.setPreferredSize(new Dimension(70, 70));

        titulo.setFont(Estilo.NEGRITA);
        titulo.setForeground(Estilo.ARENA_CLARO);
        artista.setFont(Estilo.NORMAL);
        artista.setForeground(Estilo.ARENA_CLARO);
        tiempo.setFont(Estilo.NEGRITA);
        tiempo.setForeground(Estilo.ARENA_CLARO);

        progreso.setAlSoltar(alBuscar);

        JPanel centro = new JPanel(new GridLayout(3, 1, 0, 2));
        centro.setOpaque(false);
        centro.add(titulo);
        centro.add(progreso);
        centro.add(artista);

        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 6, 0, 6);
        derecha.add(tiempo, gc);
        derecha.add(btnAnterior, gc);
        derecha.add(btnPlay, gc);
        derecha.add(btnSiguiente, gc);

        btnAnterior.addActionListener(e -> alAnterior.run());
        btnPlay.addActionListener(e -> alAlternar.run());
        btnSiguiente.addActionListener(e -> alSiguiente.run());

        add(portada, BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);
        add(derecha, BorderLayout.EAST);
    }

    public void mostrar(Cancion c) {
        titulo.setText(c.getTitulo());
        artista.setText(c.getArtista() + " - " + c.getAlbum());
        portada.setIcon(null);
        portada.setText("♪");
        if (c.getRutaFoto() != null) {
            try {
                Image img = ImageIO.read(new File(c.getRutaFoto()));
                if (img != null) {
                    portada.setIcon(new ImageIcon(img.getScaledInstance(70, 70, Image.SCALE_SMOOTH)));
                    portada.setText("");
                }
            } catch (Exception e) { }
        }
        actualizar(0, c.getDuracionSegundos());
        setPausado(false);
    }

    public void actualizar(int seg, int total) {
        if (progreso.isArrastrando()) return;   // no pelear con el mouse
        seg = Math.min(seg, total);
        tiempo.setText(formato(seg) + " / " + formato(total));
        progreso.setValor(total > 0 ? (double) seg / total : 0);
    }

    public void setPausado(boolean pausado) {
        btnPlay.setTipo(pausado ? Tipo.PLAY : Tipo.PAUSA);
    }

    private String formato(int s) {
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    // ---------- Barra de progreso (clic y arrastre) ----------
    private static class Progreso extends JComponent {
        private double valor = 0;
        private boolean arrastrando = false;
        private Consumer<Double> alSoltar;

        Progreso() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            MouseAdapter raton = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    arrastrando = true;
                    setValor(fraccion(e));
                }
                @Override public void mouseDragged(MouseEvent e) {
                    setValor(fraccion(e));
                }
                @Override public void mouseReleased(MouseEvent e) {
                    arrastrando = false;
                    double f = fraccion(e);
                    setValor(f);
                    if (alSoltar != null) alSoltar.accept(f);
                }
            };
            addMouseListener(raton);
            addMouseMotionListener(raton);
        }

        private double fraccion(MouseEvent e) {
            return Math.max(0, Math.min(1, (double) e.getX() / Math.max(1, getWidth())));
        }

        void setAlSoltar(Consumer<Double> c) { alSoltar = c; }
        boolean isArrastrando() { return arrastrando; }

        void setValor(double v) {
            valor = Math.max(0, Math.min(1, v));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = 6, y = (getHeight() - h) / 2, w = getWidth();
            g2.setColor(Estilo.ARENA_CLARO);
            g2.fill(new RoundRectangle2D.Double(0, y, w, h, h, h));
            g2.setColor(Estilo.ROJO);
            g2.fill(new RoundRectangle2D.Double(0, y, w * valor, h, h, h));
            // perilla
            g2.setColor(Estilo.ARENA_CLARO);
            g2.fill(new Ellipse2D.Double(Math.min(w - 12, Math.max(0, w * valor - 6)),
                    (getHeight() - 12) / 2.0, 12, 12));
            g2.dispose();
        }
    }

    // ---------- Botones ⏮ ⏯ ⏭ dibujados con código ----------
    private static class BotonControl extends JButton {
        private Tipo tipo;

        BotonControl(Tipo tipo) {
            this.tipo = tipo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int lado = (tipo == Tipo.PLAY || tipo == Tipo.PAUSA) ? 52 : 36;
            setPreferredSize(new Dimension(lado, lado));
        }

        void setTipo(Tipo t) { this.tipo = t; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double w = getWidth(), h = getHeight(), cx = w / 2, cy = h / 2;

            switch (tipo) {
                case PLAY:
                case PAUSA:
                    g2.setColor(Estilo.ARENA_CLARO);
                    g2.fill(new Ellipse2D.Double(1, 1, w - 2, h - 2));
                    g2.setColor(Estilo.CAFE_OSCURO);
                    if (tipo == Tipo.PAUSA) {
                        g2.fill(new Rectangle2D.Double(cx - 9, cy - 10, 6, 20));
                        g2.fill(new Rectangle2D.Double(cx + 3, cy - 10, 6, 20));
                    } else {
                        Path2D.Double t = new Path2D.Double();
                        t.moveTo(cx - 6, cy - 11);
                        t.lineTo(cx + 11, cy);
                        t.lineTo(cx - 6, cy + 11);
                        t.closePath();
                        g2.fill(t);
                    }
                    break;
                case SIGUIENTE:
                    g2.setColor(Estilo.ARENA_CLARO);
                    Path2D.Double s = new Path2D.Double();
                    s.moveTo(cx - 9, cy - 10);
                    s.lineTo(cx + 5, cy);
                    s.lineTo(cx - 9, cy + 10);
                    s.closePath();
                    g2.fill(s);
                    g2.fill(new Rectangle2D.Double(cx + 7, cy - 10, 4, 20));
                    break;
                case ANTERIOR:
                    g2.setColor(Estilo.ARENA_CLARO);
                    Path2D.Double a = new Path2D.Double();
                    a.moveTo(cx + 9, cy - 10);
                    a.lineTo(cx - 5, cy);
                    a.lineTo(cx + 9, cy + 10);
                    a.closePath();
                    g2.fill(a);
                    g2.fill(new Rectangle2D.Double(cx - 11, cy - 10, 4, 20));
                    break;
            }
            g2.dispose();
        }
    }
}
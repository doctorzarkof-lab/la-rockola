import estilo.Estilo;
import componentes.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

public class PantallaReproduccion extends JPanel {

    private static final int LADO = 230;

    private enum Tipo { ANTERIOR, SIGUIENTE, PLAY, PAUSA }

    private final JLabel portada = new JLabel("♪", SwingConstants.CENTER);
    private final JLabel titulo = new JLabel("", SwingConstants.CENTER);
    private final JLabel artista = new JLabel("", SwingConstants.CENTER);
    private final JLabel album = new JLabel("", SwingConstants.CENTER);
    private final Barra barra = new Barra();
    private final Boton btnPlay = new Boton(Tipo.PAUSA);

    public PantallaReproduccion(Runnable alVolver, Runnable alAnterior, Runnable alAlternar,
                                Runnable alSiguiente, Consumer<Double> alBuscar) {
        setLayout(new BorderLayout());
        setBackground(Estilo.ARENA);

        // ---------- Cabecera: logo + título y "Volver al inicio" ----------
        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        marca.setOpaque(false);
        marca.add(new LogoDisco(90));
        JLabel nombreApp = new JLabel("La Rockola");
        nombreApp.setFont(Estilo.TITULO);
        nombreApp.setForeground(Estilo.CAFE_OSCURO);
        marca.add(nombreApp);

        BotonPestana btnVolver = new BotonPestana("←  Volver al inicio");
        btnVolver.setPreferredSize(new Dimension(240, 46));
        btnVolver.addActionListener(e -> alVolver.run());
        JPanel volver = new JPanel(new GridBagLayout());
        volver.setOpaque(false);
        volver.add(btnVolver);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        cabecera.add(marca, BorderLayout.WEST);
        cabecera.add(volver, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        // ---------- Textos ----------
        portada.setOpaque(true);
        portada.setBackground(Estilo.CAFE_OSCURO);
        portada.setForeground(Estilo.ARENA_CLARO);
        portada.setFont(Estilo.TITULO.deriveFont(80f));
        portada.setPreferredSize(new Dimension(LADO, LADO));

        titulo.setFont(Estilo.NEGRITA.deriveFont(32f));
        titulo.setForeground(Estilo.CAFE_OSCURO);
        artista.setFont(Estilo.NEGRITA.deriveFont(24f));
        artista.setForeground(Estilo.CAFE_OSCURO);
        album.setFont(Estilo.NORMAL.deriveFont(21f));
        album.setForeground(Estilo.ROJO);

        // ---------- Barra de progreso ----------
        barra.setPreferredSize(new Dimension(720, 26));
        barra.setAlSoltar(alBuscar);

        // ---------- Botones ----------
        Boton btnAnterior = new Boton(Tipo.ANTERIOR);
        Boton btnSiguiente = new Boton(Tipo.SIGUIENTE);
        btnAnterior.addActionListener(e -> alAnterior.run());
        btnPlay.addActionListener(e -> alAlternar.run());
        btnSiguiente.addActionListener(e -> alSiguiente.run());

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
        controles.setOpaque(false);
        controles.add(btnAnterior);
        controles.add(btnPlay);
        controles.add(btnSiguiente);

        // ---------- Todo centrado en una columna ----------
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.CENTER;

        gc.insets = new Insets(4, 0, 12, 0);  centro.add(portada, gc);   gc.gridy++;
        gc.insets = new Insets(0, 0, 2, 0);   centro.add(titulo, gc);    gc.gridy++;
        gc.insets = new Insets(0, 0, 2, 0);   centro.add(artista, gc);   gc.gridy++;
        gc.insets = new Insets(0, 0, 16, 0);  centro.add(album, gc);     gc.gridy++;
        gc.insets = new Insets(0, 0, 10, 0);  centro.add(barra, gc);     gc.gridy++;
        gc.insets = new Insets(0, 0, 0, 0);   centro.add(controles, gc);

        add(centro, BorderLayout.CENTER);
    }

    // Muestra los datos de la canción (portada, título, artista y álbum)
    public void mostrar(Cancion c) {
        titulo.setText(c.getTitulo());
        artista.setText(c.getArtista());
        album.setText(c.getAlbum());

        portada.setIcon(null);
        portada.setText("♪");
        portada.setPreferredSize(new Dimension(LADO, LADO));

        if (c.getRutaFoto() != null) {
            try {
                BufferedImage img = ImageIO.read(new File(c.getRutaFoto()));
                if (img != null) {
                    // se ajusta al cuadro sin deformar la imagen
                    double k = Math.min((double) LADO / img.getWidth(), (double) LADO / img.getHeight());
                    int w = Math.max(1, (int) (img.getWidth() * k));
                    int h = Math.max(1, (int) (img.getHeight() * k));
                    portada.setIcon(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
                    portada.setText("");
                    portada.setPreferredSize(new Dimension(w, h));
                }
            } catch (Exception e) { }
        }
        actualizar(0, c.getDuracionSegundos());
        setPausado(false);
        revalidate();
        repaint();
    }

    public void actualizar(int seg, int total) {
        if (barra.isArrastrando()) return;    // no pelear con el mouse
        barra.setValor(total > 0 ? (double) Math.min(seg, total) / total : 0);
    }

    public void setPausado(boolean pausado) {
        btnPlay.setTipo(pausado ? Tipo.PLAY : Tipo.PAUSA);
    }

    // ---------- Barra de progreso (clic y arrastre) ----------
    private static class Barra extends JComponent {
        private double valor = 0;
        private boolean arrastrando = false;
        private Consumer<Double> alSoltar;

        Barra() {
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
            int w = getWidth(), h = 8, y = (getHeight() - h) / 2;
            g2.setColor(Estilo.ARENA_CLARO);
            g2.fill(new RoundRectangle2D.Double(0, y, w, h, h, h));
            g2.setColor(Estilo.ROJO);
            g2.fill(new RoundRectangle2D.Double(0, y, w * valor, h, h, h));
            g2.setColor(Estilo.CAFE_OSCURO);   // perilla
            g2.fill(new Ellipse2D.Double(Math.min(w - 16, Math.max(0, w * valor - 8)),
                    (getHeight() - 16) / 2.0, 16, 16));
            g2.dispose();
        }
    }

    // ---------- Botones grandes dibujados con código ----------
    private static class Boton extends JButton {
        private Tipo tipo;

        Boton(Tipo tipo) {
            this.tipo = tipo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            int lado = (tipo == Tipo.PLAY || tipo == Tipo.PAUSA) ? 86 : 60;
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
                    g2.setColor(Estilo.CAFE_OSCURO);
                    g2.fill(new Ellipse2D.Double(1, 1, w - 2, h - 2));
                    g2.setColor(Estilo.ARENA_CLARO);
                    if (tipo == Tipo.PAUSA) {
                        g2.fill(new Rectangle2D.Double(cx - 15, cy - 17, 10, 34));
                        g2.fill(new Rectangle2D.Double(cx + 5, cy - 17, 10, 34));
                    } else {
                        Path2D.Double t = new Path2D.Double();
                        t.moveTo(cx - 10, cy - 19);
                        t.lineTo(cx + 19, cy);
                        t.lineTo(cx - 10, cy + 19);
                        t.closePath();
                        g2.fill(t);
                    }
                    break;
                case SIGUIENTE:
                    g2.setColor(Estilo.CAFE_OSCURO);
                    Path2D.Double s = new Path2D.Double();
                    s.moveTo(cx - 15, cy - 17);
                    s.lineTo(cx + 9, cy);
                    s.lineTo(cx - 15, cy + 17);
                    s.closePath();
                    g2.fill(s);
                    g2.fill(new Rectangle2D.Double(cx + 12, cy - 17, 6, 34));
                    break;
                case ANTERIOR:
                    g2.setColor(Estilo.CAFE_OSCURO);
                    Path2D.Double a = new Path2D.Double();
                    a.moveTo(cx + 15, cy - 17);
                    a.lineTo(cx - 9, cy);
                    a.lineTo(cx + 15, cy + 17);
                    a.closePath();
                    g2.fill(a);
                    g2.fill(new Rectangle2D.Double(cx - 18, cy - 17, 6, 34));
                    break;
            }
            g2.dispose();
        }
    }
}
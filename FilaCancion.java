import estilo.Estilo;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class FilaCancion extends JPanel implements ListCellRenderer<Cancion> {

    private static final int LADO = 56;

    // Medidas fijas: sirven para saber dónde cae el lápiz al hacer clic
    public static final int MARGEN = 15;
    public static final int ANCHO_DURACION = 70;
    public static final int ANCHO_LAPIZ = 24;
    public static final int ESPACIO = 10;

    // ¿El clic en x cayó sobre el lápiz? (anchoCelda = ancho de la lista)
    public static boolean enLapiz(int x, int anchoCelda) {
        int derecha = anchoCelda - MARGEN - ANCHO_DURACION - ESPACIO;
        return x >= derecha - ANCHO_LAPIZ - 6 && x <= derecha + 6;
    }
    public static boolean enBasura(int x) {
        return x >= 20 && x <= 58;
    }

    private final JLabel basura = new JLabel(new IconoBasura());
    private final JLabel portada = new JLabel("♪", SwingConstants.CENTER);
    private final JLabel titulo = new JLabel();
    private final JLabel detalle = new JLabel();
    private final JLabel lapiz = new JLabel(new IconoLapiz(), SwingConstants.CENTER);
    private final JLabel duracion = new JLabel("", SwingConstants.RIGHT);
    private final HashMap<String, ImageIcon> cache = new HashMap<>();

    public FilaCancion() { this(false); }
    public FilaCancion(boolean simple) {

        setLayout(new BorderLayout(15, 0));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, MARGEN, 5, MARGEN));

        portada.setOpaque(true);
        portada.setBackground(Estilo.CAFE_OSCURO);
        portada.setForeground(Estilo.ARENA_CLARO);
        portada.setFont(Estilo.TITULO);
        portada.setPreferredSize(new Dimension(LADO, LADO));

        titulo.setFont(Estilo.NEGRITA);
        titulo.setForeground(Estilo.TEXTO);
        detalle.setFont(Estilo.NORMAL);
        detalle.setForeground(Estilo.ROJO);
        duracion.setFont(Estilo.NEGRITA);
        duracion.setForeground(Estilo.ROJO);
        duracion.setPreferredSize(new Dimension(ANCHO_DURACION, 24));
        lapiz.setPreferredSize(new Dimension(ANCHO_LAPIZ, 24));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        izquierda.setOpaque(false);
        izquierda.add(basura);
        izquierda.add(portada);

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(detalle);

        JPanel derecha = new JPanel(new BorderLayout(ESPACIO, 0));
        derecha.setOpaque(false);
        derecha.add(lapiz, BorderLayout.WEST);
        derecha.add(duracion, BorderLayout.EAST);

        add(izquierda, BorderLayout.WEST);
        add(textos, BorderLayout.CENTER);
        add(derecha, BorderLayout.EAST);
        if (simple) {                  // en el Historial no hay basura ni lápiz
            basura.setVisible(false);
            lapiz.setVisible(false);
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Cancion> list,
            Cancion c, int index, boolean isSelected, boolean cellHasFocus) {

        titulo.setText(c.getTitulo());
        detalle.setText(c.getArtista() + " - " + c.getAlbum());
        duracion.setText(c.getDuracionTexto());

        ImageIcon foto = cargarFoto(c.getRutaFoto());
        if (foto != null) {
            portada.setIcon(foto);
            portada.setText("");
        } else {
            portada.setIcon(null);
            portada.setText("♪");
        }

        setBackground(isSelected ? Estilo.ARENA : Estilo.ARENA_CLARO);
        return this;
    }

    private ImageIcon cargarFoto(String ruta) {
        if (ruta == null) return null;
        if (cache.containsKey(ruta)) return cache.get(ruta);
        ImageIcon icono = null;
        try {
            Image img = ImageIO.read(new File(ruta));
            if (img != null) {
                icono = new ImageIcon(img.getScaledInstance(LADO, LADO, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) { }
        cache.put(ruta, icono);
        return icono;
    }

    // ---------- Íconos dibujados con código ----------
    private static class IconoBasura implements Icon {
        public int getIconWidth() { return 22; }
        public int getIconHeight() { return 24; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.ROJO);
            g2.translate(x, y);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(2, 6, 20, 6));
            g2.draw(new Line2D.Double(8, 6, 8, 3));
            g2.draw(new Line2D.Double(8, 3, 14, 3));
            g2.draw(new Line2D.Double(14, 3, 14, 6));
            Path2D.Double cuerpo = new Path2D.Double();
            cuerpo.moveTo(4, 9);
            cuerpo.lineTo(5.5, 22);
            cuerpo.lineTo(16.5, 22);
            cuerpo.lineTo(18, 9);
            g2.draw(cuerpo);
            g2.draw(new Line2D.Double(9, 11, 9, 19));
            g2.draw(new Line2D.Double(13, 11, 13, 19));
            g2.dispose();
        }
    }

    private static class IconoLapiz implements Icon {
        public int getIconWidth() { return 22; }
        public int getIconHeight() { return 22; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.ROJO);
            g2.translate(x, y);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(8, 14, 17, 5));                 // cuerpo
            Path2D.Double punta = new Path2D.Double();                // punta
            punta.moveTo(3, 19);
            punta.lineTo(5.5, 12);
            punta.lineTo(10, 16.5);
            punta.closePath();
            g2.fill(punta);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(3, 21, 19, 21));                // base
            g2.dispose();
        }
    }
}
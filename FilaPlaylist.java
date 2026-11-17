import estilo.Estilo;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class FilaPlaylist extends JPanel implements ListCellRenderer<Playlist> {

    private static final int LADO = 84;

    private final JLabel portada = new JLabel("♪", SwingConstants.CENTER);
    private final JLabel nombre = new JLabel();
    private final JLabel conteo = new JLabel();
    private final HashMap<String, ImageIcon> cache = new HashMap<>();

    public FilaPlaylist() {
        setLayout(new BorderLayout(18, 0));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 20));

        portada.setOpaque(true);
        portada.setBackground(Estilo.CAFE_OSCURO);
        portada.setForeground(Estilo.ARENA_CLARO);
        portada.setFont(Estilo.TITULO);
        portada.setPreferredSize(new Dimension(LADO, LADO));

        nombre.setFont(Estilo.NEGRITA.deriveFont(24f));
        nombre.setForeground(Estilo.TEXTO);
        conteo.setFont(Estilo.NORMAL);
        conteo.setForeground(Estilo.ROJO);

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(nombre);
        textos.add(conteo);

        add(portada, BorderLayout.WEST);
        add(textos, BorderLayout.CENTER);
        add(new JLabel(new IconoFlecha()), BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Playlist> list,
            Playlist p, int index, boolean isSelected, boolean cellHasFocus) {

        nombre.setText(p.getNombre());
        int n = p.getCanciones().getTamano();
        conteo.setText(n + (n == 1 ? " canción" : " canciones"));

        ImageIcon foto = cargarFoto(p.getRutaFoto());
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

    // Flecha "›" dibujada con código
    private static class IconoFlecha implements Icon {
        public int getIconWidth() { return 16; }
        public int getIconHeight() { return 24; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.TEXTO);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.translate(x, y);
            Path2D.Double flecha = new Path2D.Double();
            flecha.moveTo(4, 3);
            flecha.lineTo(12, 12);
            flecha.lineTo(4, 21);
            g2.draw(flecha);
            g2.dispose();
        }
    }
}
import estilo.Estilo;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class FilaRanking extends JPanel implements ListCellRenderer<Cancion> {

    private static final int LADO = 56;

    private final JLabel portada = new JLabel("♪", SwingConstants.CENTER);
    private final JLabel titulo = new JLabel();
    private final JLabel veces = new JLabel("", SwingConstants.RIGHT);
    private final HashMap<String, ImageIcon> cache = new HashMap<>();

    public FilaRanking() {
        setLayout(new BorderLayout(15, 0));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        portada.setOpaque(true);
        portada.setBackground(Estilo.CAFE_OSCURO);
        portada.setForeground(Estilo.ARENA_CLARO);
        portada.setFont(Estilo.TITULO);
        portada.setPreferredSize(new Dimension(LADO, LADO));

        titulo.setFont(Estilo.NEGRITA.deriveFont(20f));
        titulo.setForeground(Estilo.TEXTO);
        veces.setFont(Estilo.NEGRITA.deriveFont(20f));
        veces.setForeground(Estilo.ROJO);

        add(portada, BorderLayout.WEST);
        add(titulo, BorderLayout.CENTER);
        add(veces, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Cancion> list,
            Cancion c, int index, boolean isSelected, boolean cellHasFocus) {

        titulo.setText(c.getTitulo());
        int n = c.getReproducciones();
        veces.setText(n + (n == 1 ? " vez" : " veces"));

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
}
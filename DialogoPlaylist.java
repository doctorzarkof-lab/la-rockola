import estilo.Estilo;
import componentes.BotonPestana;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class DialogoPlaylist extends JDialog {

    private final JTextField campoNombre = new JTextField();
    private final JLabel vistaFoto = new JLabel("Sin imagen", SwingConstants.CENTER);
    private String rutaFoto;
    private boolean aceptado = false;

    // existente == null -> nueva playlist; si no -> editar esa playlist
    public DialogoPlaylist(Frame dueno, Playlist existente) {
        super(dueno, existente == null ? "Nueva playlist" : "Editar playlist", true);
        getContentPane().setBackground(Estilo.ARENA_CLARO);

        campoNombre.setFont(Estilo.NORMAL);

        vistaFoto.setOpaque(true);
        vistaFoto.setBackground(Estilo.CAFE_OSCURO);
        vistaFoto.setForeground(Estilo.ARENA_CLARO);
        vistaFoto.setPreferredSize(new Dimension(120, 120));

        if (existente != null) {
            campoNombre.setText(existente.getNombre());
            rutaFoto = existente.getRutaFotoPropia();
            if (rutaFoto != null) mostrarFoto(rutaFoto);
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        form.add(seccion("Nombre", campoNombre));
        form.add(Box.createVerticalStrut(14));

        BotonPestana btnFoto = new BotonPestana("Elegir imagen");
        btnFoto.setPreferredSize(new Dimension(190, 45));
        btnFoto.addActionListener(e -> elegirFoto());

        JLabel ayuda = new JLabel("<html>Si no eliges imagen,<br>se usa la de su primera canción.</html>");
        ayuda.setForeground(Estilo.TEXTO);

        JPanel derecha = new JPanel(new BorderLayout(0, 8));
        derecha.setOpaque(false);
        derecha.add(btnFoto, BorderLayout.NORTH);
        derecha.add(ayuda, BorderLayout.CENTER);

        JPanel filaFoto = new JPanel(new BorderLayout(15, 0));
        filaFoto.setOpaque(false);
        filaFoto.add(vistaFoto, BorderLayout.WEST);
        filaFoto.add(derecha, BorderLayout.CENTER);
        form.add(seccion("Imagen", filaFoto));

        BotonPestana cancelar = new BotonPestana("Cancelar");
        cancelar.addActionListener(e -> dispose());
        BotonPestana guardar = new BotonPestana("Guardar");
        guardar.setSeleccionado(true);
        guardar.addActionListener(e -> guardar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(guardar);

        add(form, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        pack();
        setSize(Math.max(getWidth(), 480), getHeight() + 20);
        setLocationRelativeTo(dueno);
    }

    private JPanel seccion(String texto, JComponent contenido) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.NEGRITA);
        l.setForeground(Estilo.TEXTO);
        p.add(l, BorderLayout.NORTH);
        p.add(contenido, BorderLayout.CENTER);
        return p;
    }

    private void elegirFoto() {
        JFileChooser sel = new JFileChooser();
        sel.setDialogTitle("Elige la imagen");
        sel.setFileFilter(new FileNameExtensionFilter("Imágenes (jpg, png, gif)",
                "jpg", "jpeg", "png", "gif"));
        if (sel.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        rutaFoto = sel.getSelectedFile().getAbsolutePath();
        mostrarFoto(rutaFoto);
    }

    private void mostrarFoto(String ruta) {
        try {
            Image img = ImageIO.read(new File(ruta));
            if (img != null) {
                vistaFoto.setIcon(new ImageIcon(img.getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
                vistaFoto.setText("");
                return;
            }
        } catch (Exception e) { }
        vistaFoto.setIcon(null);
        vistaFoto.setText("Sin imagen");
    }

    private void guardar() {
        if (campoNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ponle un nombre a la playlist.");
            return;
        }
        aceptado = true;
        dispose();
    }

    public boolean isAceptado() { return aceptado; }
    public String getNombre() { return campoNombre.getText().trim(); }
    public String getRutaFoto() { return rutaFoto; }
}
import estilo.Estilo;
import componentes.BotonPestana;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class DialogoCancion extends JDialog {

    private final JTextField campoTitulo = new JTextField();
    private final JTextField campoArtista = new JTextField();
    private final JTextField campoAlbum = new JTextField();
    private final JLabel etiquetaAudio = new JLabel("Ningún archivo seleccionado");
    private final JLabel etiquetaDuracion = new JLabel("--:--");
    private final JLabel vistaFoto = new JLabel("Sin imagen", SwingConstants.CENTER);

    private final boolean modoAnadir;
    private String rutaAudio;
    private String rutaFoto;
    private int duracion;
    private boolean aceptado = false;

    // existente == null -> añadir; si no -> editar esa canción
    public DialogoCancion(Frame dueno, Cancion existente) {
        super(dueno, existente == null ? "Añadir canción" : "Editar canción", true);
        modoAnadir = (existente == null);
        getContentPane().setBackground(Estilo.ARENA_CLARO);

        campoTitulo.setFont(Estilo.NORMAL);
        campoArtista.setFont(Estilo.NORMAL);
        campoAlbum.setFont(Estilo.NORMAL);
        campoTitulo.setEditable(!modoAnadir);   // al añadir, el título sale solo del MP3

        etiquetaAudio.setForeground(Estilo.TEXTO);
        etiquetaDuracion.setFont(Estilo.NEGRITA);
        etiquetaDuracion.setForeground(Estilo.ROJO);

        vistaFoto.setOpaque(true);
        vistaFoto.setBackground(Estilo.CAFE_OSCURO);
        vistaFoto.setForeground(Estilo.ARENA_CLARO);
        vistaFoto.setPreferredSize(new Dimension(120, 120));

        if (!modoAnadir) {
            campoTitulo.setText(existente.getTitulo());
            campoArtista.setText(existente.getArtista());
            campoAlbum.setText(existente.getAlbum());
            etiquetaDuracion.setText(existente.getDuracionTexto());
            rutaFoto = existente.getRutaFoto();
            if (rutaFoto != null) mostrarFoto(rutaFoto);
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        if (modoAnadir) {
            BotonPestana btnAudio = new BotonPestana("Elegir MP3");
            btnAudio.setPreferredSize(new Dimension(170, 45));
            btnAudio.addActionListener(e -> elegirAudio());
            JPanel filaAudio = new JPanel(new BorderLayout(10, 0));
            filaAudio.setOpaque(false);
            filaAudio.add(btnAudio, BorderLayout.WEST);
            filaAudio.add(etiquetaAudio, BorderLayout.CENTER);
            form.add(seccion("Archivo de audio", filaAudio));
            form.add(Box.createVerticalStrut(12));
        }

        form.add(seccion("Título", campoTitulo));
        form.add(Box.createVerticalStrut(12));
        form.add(seccion("Artista", campoArtista));
        form.add(Box.createVerticalStrut(12));
        form.add(seccion("Álbum", campoAlbum));
        form.add(Box.createVerticalStrut(12));
        form.add(seccion("Duración", etiquetaDuracion));
        form.add(Box.createVerticalStrut(12));

        BotonPestana btnFoto = new BotonPestana("Elegir imagen");
        btnFoto.setPreferredSize(new Dimension(190, 45));
        btnFoto.addActionListener(e -> elegirFoto());
        JPanel botonFoto = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botonFoto.setOpaque(false);
        botonFoto.add(btnFoto);
        JPanel filaFoto = new JPanel(new BorderLayout(15, 0));
        filaFoto.setOpaque(false);
        filaFoto.add(vistaFoto, BorderLayout.WEST);
        filaFoto.add(botonFoto, BorderLayout.CENTER);
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
        setSize(Math.max(getWidth(), 520), getHeight() + 30);
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

    private void elegirAudio() {
        JFileChooser sel = new JFileChooser();
        sel.setDialogTitle("Elige un archivo de audio");
        sel.setFileFilter(new FileNameExtensionFilter("Audio (mp3, wav)", "mp3", "wav"));
        if (sel.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = sel.getSelectedFile();
        LectorMp3 datos = new LectorMp3(f);
        rutaAudio = f.getAbsolutePath();
        duracion = datos.duracionSegundos;

        campoTitulo.setText(datos.titulo);          // salen solos, pero se pueden corregir
        campoArtista.setText(datos.artista);
        campoAlbum.setText(datos.album);
        etiquetaDuracion.setText(String.format("%02d:%02d", duracion / 60, duracion % 60));
        etiquetaAudio.setText(f.getName());
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
        if (modoAnadir && rutaAudio == null) {
            JOptionPane.showMessageDialog(this, "Elige primero el archivo MP3.");
            return;
        }
        if (campoTitulo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El título no puede estar vacío.");
            return;
        }
        aceptado = true;
        dispose();
    }

    public boolean isAceptado() { return aceptado; }
    public String getTitulo() { return campoTitulo.getText().trim(); }
    public String getArtista() {
        String a = campoArtista.getText().trim();
        return a.isEmpty() ? "Desconocido" : a;
    }
    public String getAlbum() {
        String a = campoAlbum.getText().trim();
        return a.isEmpty() ? "Sencillo" : a;
    }
    public String getRutaAudio() { return rutaAudio; }
    public String getRutaFoto() { return rutaFoto; }
    public int getDuracion() { return duracion; }
}
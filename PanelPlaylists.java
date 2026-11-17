import componentes.*;
import estilo.Estilo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelPlaylists extends JPanel {

    private final Frame dueno;
    private final ListaDoble biblioteca;
    private final ArrayList<Playlist> playlists;
    private final BiConsumer<Cancion, ListaDoble> alReproducir;   // (canción, lista de donde sale)
    private final Consumer<Cancion> alEditarCancion;
    private final Runnable alCambiar;                             // guarda playlists.txt

    private final CardLayout cartas = new CardLayout();
    private final DefaultListModel<Playlist> modeloPlaylists = new DefaultListModel<>();
    private final JList<Playlist> jlistPlaylists = new JList<>(modeloPlaylists);
    private final JLabel vacio = new JLabel(
            "Aún no tienes playlists. Pulsa \"Nueva playlist\" para crear la primera.",
            SwingConstants.CENTER);

    private final JLabel lblNombre = new JLabel();
    private final JLabel lblConteo = new JLabel();
    private final CampoBusqueda campoBusqueda = new CampoBusqueda();
    private final PanelLista panelCanciones = new PanelLista(new ListaDoble());
    private Playlist actual;                                      // la playlist abierta (o null)

    public PanelPlaylists(Frame dueno, ListaDoble biblioteca, ArrayList<Playlist> playlists,
            BiConsumer<Cancion, ListaDoble> alReproducir, Consumer<Cancion> alEditarCancion,
            Runnable alCambiar) {
        this.dueno = dueno;
        this.biblioteca = biblioteca;
        this.playlists = playlists;
        this.alReproducir = alReproducir;
        this.alEditarCancion = alEditarCancion;
        this.alCambiar = alCambiar;

        setLayout(cartas);
        setOpaque(false);
        add(construirLista(), "lista");
        add(construirDetalle(), "detalle");
        refrescar();
    }

    // ---------- VISTA 1: lista de playlists ----------
    private JPanel construirLista() {
        JLabel titulo = new JLabel("Playlists");
        titulo.setFont(Estilo.NEGRITA.deriveFont(26f));
        titulo.setForeground(Estilo.CAFE_OSCURO);

        BotonPestana btnNueva = new BotonPestana("Nueva playlist",
                new IconoPestana(IconoPestana.Tipo.MAS, 20));
        btnNueva.setSeleccionado(true);
        btnNueva.setPreferredSize(new Dimension(210, 42));
        btnNueva.addActionListener(e -> nuevaPlaylist());

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(btnNueva, BorderLayout.EAST);

        jlistPlaylists.setOpaque(false);
        jlistPlaylists.setCellRenderer(new FilaPlaylist());
        jlistPlaylists.setFixedCellHeight(100);
        jlistPlaylists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jlistPlaylists.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Playlist p = playlistBajo(e);
                if (p != null) abrir(p);
            }
        });
        jlistPlaylists.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                jlistPlaylists.setCursor(playlistBajo(e) != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });

        vacio.setFont(Estilo.NEGRITA);
        vacio.setForeground(Estilo.TEXTO);
        vacio.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JScrollPane scroll = new JScrollPane(jlistPlaylists);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        PanelRedondeado caja = new PanelRedondeado(Estilo.ARENA_CLARO, 25);
        caja.setLayout(new BorderLayout());
        caja.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        caja.add(vacio, BorderLayout.NORTH);
        caja.add(scroll, BorderLayout.CENTER);

        JPanel vista = new JPanel(new BorderLayout(0, 10));
        vista.setOpaque(false);
        vista.add(cabecera, BorderLayout.NORTH);
        vista.add(caja, BorderLayout.CENTER);
        return vista;
    }

    private Playlist playlistBajo(MouseEvent e) {
        int i = jlistPlaylists.locationToIndex(e.getPoint());
        if (i < 0 || jlistPlaylists.getCellBounds(i, i) == null) return null;
        if (!jlistPlaylists.getCellBounds(i, i).contains(e.getPoint())) return null;
        return modeloPlaylists.get(i);
    }

    // ---------- VISTA 2: detalle de una playlist ----------
    private JPanel construirDetalle() {
        BotonPestana btnVolver = new BotonPestana("← Volver");
        btnVolver.setPreferredSize(new Dimension(130, 42));
        btnVolver.addActionListener(e -> volver());

        lblNombre.setFont(Estilo.NEGRITA.deriveFont(26f));
        lblNombre.setForeground(Estilo.CAFE_OSCURO);
        lblConteo.setFont(Estilo.NORMAL);
        lblConteo.setForeground(Estilo.ROJO);

        JPanel titulos = new JPanel(new GridLayout(2, 1));
        titulos.setOpaque(false);
        titulos.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        titulos.add(lblNombre);
        titulos.add(lblConteo);

        BotonPestana btnAgregar = new BotonPestana("Agregar canciones",
                new IconoPestana(IconoPestana.Tipo.MAS, 20));
        btnAgregar.setSeleccionado(true);
        btnAgregar.setPreferredSize(new Dimension(230, 42));
        btnAgregar.addActionListener(e -> agregarCanciones());

        BotonPestana btnEditar = new BotonPestana("Editar");
        btnEditar.setPreferredSize(new Dimension(110, 42));
        btnEditar.addActionListener(e -> editarPlaylist());

        BotonPestana btnEliminar = new BotonPestana("Eliminar");
        btnEliminar.setPreferredSize(new Dimension(120, 42));
        btnEliminar.addActionListener(e -> eliminarPlaylist());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnAgregar);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(btnVolver, BorderLayout.WEST);
        arriba.add(titulos, BorderLayout.CENTER);
        arriba.add(acciones, BorderLayout.EAST);

        // Buscador de canciones dentro de la playlist
        campoBusqueda.setPreferredSize(new Dimension(0, 46));
        campoBusqueda.setAlCambiar(t -> { if (actual != null) pintarDetalle(); });

        // Lista de canciones: doble clic reproduce, lápiz edita, basura quita de la playlist
        panelCanciones.setAlReproducir(c -> {
            if (actual != null) alReproducir.accept(c, actual.getCanciones());
        });
        panelCanciones.setAlEditar(c -> alEditarCancion.accept(c));
        panelCanciones.setAlEliminar(this::quitarDeLaPlaylist);

        JPanel abajo = new JPanel(new BorderLayout(0, 10));
        abajo.setOpaque(false);
        abajo.add(campoBusqueda, BorderLayout.NORTH);
        abajo.add(panelCanciones, BorderLayout.CENTER);

        JPanel vista = new JPanel(new BorderLayout(0, 10));
        vista.setOpaque(false);
        vista.add(arriba, BorderLayout.NORTH);
        vista.add(abajo, BorderLayout.CENTER);
        return vista;
    }

    // ---------- Navegación ----------
    private void abrir(Playlist p) {
        actual = p;
        campoBusqueda.setText("");
        pintarDetalle();
        cartas.show(this, "detalle");
    }

    private void volver() {
        actual = null;
        campoBusqueda.setText("");
        refrescar();
        cartas.show(this, "lista");
    }

    private void pintarDetalle() {
        lblNombre.setText(actual.getNombre());
        int n = actual.getCanciones().getTamano();
        lblConteo.setText(n + (n == 1 ? " canción" : " canciones"));
        panelCanciones.actualizarCon(
                Filtro.filtrar(actual.getCanciones().aArrayList(), campoBusqueda.getText()));
    }

    // Vuelve a dibujar todo (se llama cuando algo cambia)
    public void refrescar() {
        modeloPlaylists.clear();
        for (Playlist p : playlists) modeloPlaylists.addElement(p);
        vacio.setVisible(playlists.isEmpty());
        if (actual != null) {
            if (playlists.contains(actual)) pintarDetalle();
            else volver();
        }
    }

    // La llama la ventana cuando se elimina una canción de la biblioteca
    public void cancionEliminada(Cancion c) {
        for (Playlist p : playlists) p.quitar(c);
        alCambiar.run();
        refrescar();
    }

    // ---------- Acciones ----------
    private void nuevaPlaylist() {
        DialogoPlaylist d = new DialogoPlaylist(dueno, null);
        d.setVisible(true);
        if (!d.isAceptado()) return;

        Playlist p = new Playlist(d.getNombre(), Almacen.copiarFoto(d.getRutaFoto()));
        playlists.add(p);
        alCambiar.run();
        refrescar();
        abrir(p);                                   // se abre para agregarle canciones
    }

    private void agregarCanciones() {
        if (biblioteca.estaVacia()) {
            JOptionPane.showMessageDialog(dueno, "Primero añade canciones en Inicio.");
            return;
        }
        ArrayList<Cancion> yaEstan = actual.getCanciones().aArrayList();
        ArrayList<Cancion> candidatas = new ArrayList<>();
        for (Cancion c : biblioteca.aArrayList()) {
            if (!yaEstan.contains(c)) candidatas.add(c);
        }
        if (candidatas.isEmpty()) {
            JOptionPane.showMessageDialog(dueno, "Todas tus canciones ya están en esta playlist.");
            return;
        }

        DialogoAgregar d = new DialogoAgregar(dueno, candidatas, actual.getNombre());
        d.setVisible(true);
        if (!d.isAceptado()) return;

        for (Cancion c : d.getSeleccionadas()) actual.agregar(c);
        alCambiar.run();
        refrescar();
    }

    private void editarPlaylist() {
        DialogoPlaylist d = new DialogoPlaylist(dueno, actual);
        d.setVisible(true);
        if (!d.isAceptado()) return;

        actual.setNombre(d.getNombre());
        actual.setRutaFoto(Almacen.copiarFoto(d.getRutaFoto()));
        alCambiar.run();
        refrescar();
    }

    private void eliminarPlaylist() {
        boolean ok = DialogoConfirmar.preguntar(dueno, "¿Eliminar playlist?",
                "Se eliminará \"" + actual.getNombre() + "\".\n"
                + "Tus canciones no se borran de la biblioteca.", "Eliminar");
        if (!ok) return;

        playlists.remove(actual);
        alCambiar.run();
        volver();
    }

    private void quitarDeLaPlaylist(Cancion c) {
        boolean ok = DialogoConfirmar.preguntar(dueno, "¿Quitar de la playlist?",
                "\"" + c.getTitulo() + "\" saldrá de esta playlist,\n"
                + "pero seguirá en tu biblioteca.", "Quitar");
        if (!ok) return;

        actual.quitar(c);
        alCambiar.run();
        refrescar();
    }
}
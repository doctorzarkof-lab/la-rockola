import estilo.Estilo;
import componentes.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class VentanaPrincipal extends JFrame {

    private final CardLayout raiz = new CardLayout();               // principal <-> pantalla grande
    private final JPanel principal = new JPanel(new BorderLayout());
    private PantallaReproduccion pantalla;

    private JPanel sur;
    private final ListaDoble lista;
    private ListaDoble listaActiva;                 // de dónde se está reproduciendo (biblioteca o playlist)
    private final Pila historial = new Pila();
    private TablaHash indice = new TablaHash(64);
    private String textoBusqueda = "";
    private PanelLista panelInicio;
    private PanelLista panelHistorial;
    private PanelLista panelEscuchadas;
    private PanelPlaylists panelPlaylists;
    private final Reproductor reproductor = new Reproductor();
    private BarraReproductor barra;
    private final Timer reloj;

    public VentanaPrincipal(ListaDoble lista, ArrayList<Playlist> playlists) {
        super("La Rockola");
        this.lista = lista;
        this.listaActiva = lista;
        setSize(1000, 650);
        LogoDisco iconoApp = new LogoDisco(128);
        iconoApp.setSize(128, 128);
        BufferedImage imgIcono = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gi = imgIcono.createGraphics();
        iconoApp.paint(gi);
        gi.dispose();
        setIconImage(imgIcono);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(raiz);
        getContentPane().setBackground(Estilo.ARENA);
        principal.setBackground(Estilo.ARENA);

        // ---------- NORTE: logo + título + buscador ----------
        JPanel norte = new JPanel(new BorderLayout());
        norte.setBackground(Estilo.ARENA);
        norte.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        marca.setOpaque(false);
        marca.add(new LogoDisco(90));

        JLabel titulo = new JLabel("La Rockola");
        titulo.setFont(Estilo.TITULO);
        titulo.setForeground(Estilo.CAFE_OSCURO);
        marca.add(titulo);

        CampoBusqueda campoBusqueda = new CampoBusqueda();
        JPanel buscador = new JPanel(new GridBagLayout());
        buscador.setOpaque(false);
        buscador.add(campoBusqueda);

        norte.add(marca, BorderLayout.WEST);
        norte.add(buscador, BorderLayout.EAST);
        principal.add(norte, BorderLayout.NORTH);

        // ---------- SECCIONES (CardLayout) ----------
        CardLayout cartas = new CardLayout();
        JPanel contenido = new JPanel(cartas);
        contenido.setOpaque(false);

        // Inicio
        panelInicio = new PanelLista(lista);
        panelInicio.setAlEditar(this::editarCancion);
        panelInicio.setAlReproducir(c -> clicEnCancion(c, lista));
        panelInicio.setAlEliminar(this::eliminarCancion);

        BotonPestana btnAnadir = new BotonPestana("Añadir canción",
                new IconoPestana(IconoPestana.Tipo.MAS, 22));
        btnAnadir.setSeleccionado(true);
        btnAnadir.setPreferredSize(new Dimension(0, 50));
        btnAnadir.addActionListener(e -> anadirCancion());

        JPanel inicio = new JPanel(new BorderLayout(0, 10));
        inicio.setOpaque(false);
        inicio.add(btnAnadir, BorderLayout.NORTH);
        inicio.add(panelInicio, BorderLayout.CENTER);

        // Historial
        panelHistorial = new PanelLista();
        panelHistorial.setAlReproducir(c -> clicEnCancion(c, lista));

        JLabel tituloHistorial = new JLabel("Historial de reproducción");
        tituloHistorial.setFont(Estilo.NEGRITA.deriveFont(24f));
        tituloHistorial.setForeground(Estilo.CAFE_OSCURO);

        BotonPestana btnBorrar = new BotonPestana("Borrar historial");
        btnBorrar.setSeleccionado(true);
        btnBorrar.setPreferredSize(new Dimension(190, 40));
        btnBorrar.addActionListener(e -> borrarHistorial());

        JPanel cabeceraHistorial = new JPanel(new BorderLayout());
        cabeceraHistorial.setOpaque(false);
        cabeceraHistorial.setBorder(BorderFactory.createEmptyBorder(12, 20, 0, 12));
        cabeceraHistorial.add(tituloHistorial, BorderLayout.WEST);
        cabeceraHistorial.add(btnBorrar, BorderLayout.EAST);

        PanelRedondeado historialCaja = new PanelRedondeado(Estilo.ARENA_CLARO, 25);
        historialCaja.setLayout(new BorderLayout());
        historialCaja.add(cabeceraHistorial, BorderLayout.NORTH);
        historialCaja.add(panelHistorial, BorderLayout.CENTER);

        // Más Escuchadas
        panelEscuchadas = new PanelLista(new FilaRanking());
        panelEscuchadas.setAlReproducir(c -> clicEnCancion(c, lista));

        JLabel tituloEscuchadas = new JLabel("Canciones Más Reproducidas");
        tituloEscuchadas.setFont(Estilo.NEGRITA.deriveFont(24f));
        tituloEscuchadas.setForeground(Estilo.CAFE_OSCURO);
        tituloEscuchadas.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        JPanel escuchadas = new JPanel(new BorderLayout(0, 8));
        escuchadas.setOpaque(false);
        escuchadas.add(tituloEscuchadas, BorderLayout.NORTH);
        escuchadas.add(panelEscuchadas, BorderLayout.CENTER);

        // Editar Playlists
        panelPlaylists = new PanelPlaylists(this, lista, playlists,
                this::clicEnCancion,
                this::editarCancion,
                () -> Almacen.guardarPlaylists(playlists));

        contenido.add(inicio, "inicio");
        contenido.add(historialCaja, "historial");
        contenido.add(escuchadas, "escuchadas");
        contenido.add(panelPlaylists, "editar");

        // ---------- PESTAÑAS ----------
        BotonPestana[] pestanas = {
            new BotonPestana("Inicio", new IconoPestana(IconoPestana.Tipo.CASA, 22)),
            new BotonPestana("Historial", new IconoPestana(IconoPestana.Tipo.RELOJ, 22)),
            new BotonPestana("Más Escuchadas", new IconoPestana(IconoPestana.Tipo.CORAZON, 22)),
            new BotonPestana("Editar Playlists", new IconoPestana(IconoPestana.Tipo.MAS, 22))
        };
        String[] nombres = {"inicio", "historial", "escuchadas", "editar"};

        JPanel barraPestanas = new JPanel(new GridLayout(1, 4, 4, 0));
        barraPestanas.setOpaque(false);
        pestanas[0].setSeleccionado(true);

        for (int i = 0; i < pestanas.length; i++) {
            final int idx = i;
            final BotonPestana boton = pestanas[i];
            barraPestanas.add(boton);
            boton.addActionListener(e -> {
                for (BotonPestana otro : pestanas) {
                    otro.setSeleccionado(otro == boton);
                }
                cartas.show(contenido, nombres[idx]);
            });
        }

        // Buscador de arriba: filtra la lista de Inicio mientras escribes
        campoBusqueda.setAlCambiar(texto -> {
            textoBusqueda = texto;
            refrescarInicio();
            if (!texto.trim().isEmpty()) {
                for (BotonPestana otro : pestanas) {
                    otro.setSeleccionado(otro == pestanas[0]);
                }
                cartas.show(contenido, "inicio");
            }
        });

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setBackground(Estilo.ARENA);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        centro.add(barraPestanas, BorderLayout.NORTH);
        centro.add(contenido, BorderLayout.CENTER);
        principal.add(centro, BorderLayout.CENTER);

        // ---------- SUR: barra del reproductor (oculta hasta que suene algo) ----------
        barra = new BarraReproductor(this::anterior, this::alternar,
                this::siguiente, this::saltar);

        sur = new JPanel(new BorderLayout());
        sur.setBackground(Estilo.ARENA);
        sur.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));
        sur.add(barra, BorderLayout.CENTER);
        sur.setVisible(false);
        principal.add(sur, BorderLayout.SOUTH);

        // ---------- Pantalla grande de reproducción ----------
        pantalla = new PantallaReproduccion(this::cerrarPantalla, this::anterior,
                this::alternar, this::siguiente, this::saltar);

        add(principal, "principal");
        add(pantalla, "reproduccion");

        reproductor.setAlTerminar(this::siguiente);

        reloj = new Timer(500, e -> refrescarBarra());
        reloj.start();

        reconstruirIndice();
        refrescarEscuchadas();
    }

    // ---------- PANTALLA GRANDE ----------
    private void abrirPantalla() {
        Cancion c = reproductor.getCancion();
        if (c == null) return;
        pantalla.mostrar(c);
        pantalla.actualizar(reproductor.segundosActuales(), c.getDuracionSegundos());
        pantalla.setPausado(!reproductor.estaSonando());
        raiz.show(getContentPane(), "reproduccion");
    }

    private void cerrarPantalla() {
        raiz.show(getContentPane(), "principal");
    }

    // Doble clic en una canción: si ya es la que suena, abre la pantalla grande;
    // si es otra, la reproduce
    private void clicEnCancion(Cancion c, ListaDoble origen) {
        if (c != null && c == reproductor.getCancion()) {
            abrirPantalla();
        } else {
            reproducirDe(c, origen);
        }
    }

    // ---------- BÚSQUEDA (tabla hash) ----------
    private void reconstruirIndice() {
        indice = new TablaHash(64);
        for (Cancion c : lista.aArrayList()) {
            indice.agregar(c);
        }
        refrescarInicio();
    }

    private void refrescarInicio() {
        ArrayList<Cancion> todas = lista.aArrayList();
        ArrayList<Cancion> encontradas = indice.buscar(textoBusqueda);
        if (encontradas == null) {
            panelInicio.actualizarCon(todas);
            return;
        }
        HashSet<Cancion> coinciden = new HashSet<>(encontradas);
        ArrayList<Cancion> filtradas = new ArrayList<>();
        for (Cancion c : todas) {
            if (coinciden.contains(c)) filtradas.add(c);
        }
        panelInicio.actualizarCon(filtradas);
    }

    // ---------- AÑADIR / EDITAR / ELIMINAR ----------
    private void anadirCancion() {
        DialogoCancion d = new DialogoCancion(this, null);
        d.setVisible(true);
        if (!d.isAceptado()) return;

        Cancion nueva = new Cancion(d.getTitulo(), d.getArtista(), d.getAlbum(), d.getDuracion());
        nueva.setRutaAudio(Almacen.copiarAudio(d.getRutaAudio()));
        nueva.setRutaFoto(Almacen.copiarFoto(d.getRutaFoto()));
        lista.agregar(nueva);
        reconstruirIndice();
        refrescarEscuchadas();
        Almacen.guardar(lista);
    }

    private void editarCancion(Cancion c) {
        DialogoCancion d = new DialogoCancion(this, c);
        d.setVisible(true);
        if (!d.isAceptado()) return;

        c.setTitulo(d.getTitulo());
        c.setArtista(d.getArtista());
        c.setAlbum(d.getAlbum());
        c.setRutaFoto(Almacen.copiarFoto(d.getRutaFoto()));
        reconstruirIndice();
        refrescarHistorial();
        refrescarEscuchadas();
        panelPlaylists.refrescar();
        Almacen.guardar(lista);
    }

    private void eliminarCancion(Cancion c) {
        boolean ok = DialogoConfirmar.preguntar(this, "¿Eliminar canción?",
                "Vas a quitar \"" + c.getTitulo() + "\" de tu lista.\n"
                + "El archivo MP3 no se borra de tu computador.",
                "Eliminar");
        if (!ok) return;

        if (reproductor.getCancion() == c) {
            reproductor.limpiar();
            sur.setVisible(false);
        }

        if (lista.seleccionar(c)) {
            lista.eliminarActual();
        }
        historial.quitar(c);
        panelPlaylists.cancionEliminada(c);
        reconstruirIndice();
        refrescarHistorial();
        refrescarEscuchadas();
        Almacen.guardar(lista);
        revalidate();
        repaint();
    }

    // ---------- HISTORIAL Y MÁS ESCUCHADAS ----------
    private void refrescarHistorial() {
        panelHistorial.actualizarCon(historial.aArrayList());
    }

    private void refrescarEscuchadas() {
        panelEscuchadas.actualizarCon(ListaPrioridad.masEscuchadas(lista.aArrayList()));
    }

    private void borrarHistorial() {
        if (historial.estaVacia()) return;
        boolean ok = DialogoConfirmar.preguntar(this, "¿Borrar historial?",
                "Se vaciará la lista de canciones escuchadas.\n"
                + "Tus canciones no se eliminan.",
                "Borrar");
        if (!ok) return;
        historial.vaciar();
        refrescarHistorial();
    }

    // ---------- REPRODUCCIÓN ----------
    // origen = la lista de donde sale la canción (biblioteca o una playlist);
    // siguiente y anterior se mueven dentro de esa lista
    private void reproducirDe(Cancion c, ListaDoble origen) {
        if (c == null) return;
        if (c.getRutaAudio() == null || !new File(c.getRutaAudio()).exists()) {
            JOptionPane.showMessageDialog(this, "No se encuentra el archivo de audio de esta canción.");
            return;
        }
        listaActiva = origen;
        origen.seleccionar(c);
        c.incrementarReproducciones();
        Almacen.guardar(lista);
        historial.quitar(c);
        historial.push(c);
        refrescarHistorial();
        refrescarEscuchadas();
        reproductor.reproducir(c);
        barra.mostrar(c);
        pantalla.mostrar(c);              // la pantalla grande siempre queda al día
        mostrarReproductor();
    }

    private void alternar() {
        if (reproductor.getCancion() == null) return;
        reproductor.alternar();
        boolean pausado = !reproductor.estaSonando();
        barra.setPausado(pausado);
        pantalla.setPausado(pausado);
    }

    // Deja 'listaActiva' apuntando a la canción que suena; si ya no está ahí, usa la biblioteca
    private boolean ubicarActual(Cancion actual) {
        if (listaActiva.seleccionar(actual)) return true;
        listaActiva = lista;
        return lista.seleccionar(actual);
    }

    private void siguiente() {
        Cancion actual = reproductor.getCancion();
        if (actual == null || !ubicarActual(actual)) return;
        listaActiva.siguiente();
        reproducirDe(listaActiva.getActual(), listaActiva);
    }

    private void anterior() {
        Cancion actual = reproductor.getCancion();
        if (actual == null || !ubicarActual(actual)) return;
        listaActiva.anterior();
        reproducirDe(listaActiva.getActual(), listaActiva);
    }

    private void saltar(double fraccion) {
        Cancion c = reproductor.getCancion();
        if (c == null) return;
        int segundo = (int) (fraccion * c.getDuracionSegundos());
        reproductor.saltarA(segundo);
        barra.actualizar(segundo, c.getDuracionSegundos());
        pantalla.actualizar(segundo, c.getDuracionSegundos());
    }

    private void refrescarBarra() {
        Cancion c = reproductor.getCancion();
        if (c != null) {
            int seg = reproductor.segundosActuales();
            barra.actualizar(seg, c.getDuracionSegundos());
            pantalla.actualizar(seg, c.getDuracionSegundos());
        }
    }

    public void mostrarReproductor() {
        sur.setVisible(true);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        ListaDoble lista = new ListaDoble();
        Almacen.cargar(lista);
        ArrayList<Playlist> playlists = Almacen.cargarPlaylists(lista);
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(lista, playlists).setVisible(true));
    }
}
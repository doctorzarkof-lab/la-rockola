import componentes.PanelRedondeado;
import estilo.Estilo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PanelLista extends PanelRedondeado {

    private final DefaultListModel<Cancion> modelo = new DefaultListModel<>();
    private final JList<Cancion> jlist = new JList<>(modelo);
    private Consumer<Cancion> alEditar;       // clic en el lápiz
    private Consumer<Cancion> alReproducir;   // doble clic en una fila
    private Consumer<Cancion> alEliminar;     // clic en la basura

    // Lista normal (Inicio): con basura y lápiz
    public PanelLista(ListaDoble lista) {
        this(new FilaCancion(false), false, lista.aArrayList());
    }

    // Lista simple (Historial): solo reproduce con doble clic
    public PanelLista() {
        this(new FilaCancion(true), true, new ArrayList<>());
    }

    // Lista con otra fila (Más Escuchadas): solo reproduce con doble clic
    public PanelLista(ListCellRenderer<Cancion> fila) {
        this(fila, true, new ArrayList<>());
    }

    private PanelLista(ListCellRenderer<Cancion> fila, boolean simple, List<Cancion> inicial) {
        super(Estilo.ARENA_CLARO, 25);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Cancion c : inicial) modelo.addElement(c);

        jlist.setOpaque(false);
        jlist.setCellRenderer(fila);
        jlist.setFixedCellHeight(70);
        jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Cancion c = cancionBajo(e);
                if (c == null) return;
                boolean enLapiz = !simple && FilaCancion.enLapiz(e.getX(), jlist.getWidth());
                boolean enBasura = !simple && FilaCancion.enBasura(e.getX());

                if (enBasura && alEliminar != null) {
                    alEliminar.accept(c);
                } else if (enLapiz && alEditar != null) {
                    alEditar.accept(c);
                } else if (e.getClickCount() == 2 && alReproducir != null) {
                    alReproducir.accept(c);
                }
            }
        });

        jlist.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean encima = !simple && cancionBajo(e) != null
                        && (FilaCancion.enLapiz(e.getX(), jlist.getWidth())
                            || FilaCancion.enBasura(e.getX()));
                jlist.setCursor(encima ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                                       : Cursor.getDefaultCursor());
            }
        });

        JScrollPane scroll = new JScrollPane(jlist);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(scroll, BorderLayout.CENTER);
    }

    // Devuelve la canción bajo el mouse, o null si no hay ninguna
    private Cancion cancionBajo(MouseEvent e) {
        int i = jlist.locationToIndex(e.getPoint());
        if (i < 0) return null;
        if (!jlist.getCellBounds(i, i).contains(e.getPoint())) return null;
        return modelo.get(i);
    }

    public void setAlEditar(Consumer<Cancion> accion) { this.alEditar = accion; }
    public void setAlReproducir(Consumer<Cancion> accion) { this.alReproducir = accion; }
    public void setAlEliminar(Consumer<Cancion> accion) { this.alEliminar = accion; }

    public JList<Cancion> getJList() { return jlist; }

    public void actualizar(ListaDoble lista) {
        actualizarCon(lista.aArrayList());
    }

    public void actualizarCon(List<Cancion> canciones) {
        modelo.clear();
        for (Cancion c : canciones) modelo.addElement(c);
    }
}
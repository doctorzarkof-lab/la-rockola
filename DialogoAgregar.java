import estilo.Estilo;
import componentes.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;

public class DialogoAgregar extends JDialog {

    private final ArrayList<Cancion> candidatas;
    private final HashSet<Cancion> marcadas = new HashSet<>();
    private final DefaultListModel<Cancion> modelo = new DefaultListModel<>();
    private final JList<Cancion> jlist = new JList<>(modelo);
    private final BotonPestana btnAgregar = new BotonPestana("Agregar");
    private boolean aceptado = false;

    public DialogoAgregar(Frame dueno, ArrayList<Cancion> candidatas, String nombrePlaylist) {
        super(dueno, "Agregar canciones", true);
        this.candidatas = candidatas;
        getContentPane().setBackground(Estilo.ARENA_CLARO);

        JLabel titulo = new JLabel("Agregar a \"" + nombrePlaylist + "\"");
        titulo.setFont(Estilo.NEGRITA.deriveFont(22f));
        titulo.setForeground(Estilo.CAFE_OSCURO);

        CampoBusqueda campo = new CampoBusqueda();
        campo.setPreferredSize(new Dimension(0, 46));
        campo.setAlCambiar(this::mostrar);

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        arriba.add(titulo, BorderLayout.NORTH);
        arriba.add(campo, BorderLayout.CENTER);

        jlist.setOpaque(false);
        jlist.setCellRenderer(new FilaMarcar());
        jlist.setFixedCellHeight(58);
        jlist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = jlist.locationToIndex(e.getPoint());
                if (i < 0 || jlist.getCellBounds(i, i) == null
                        || !jlist.getCellBounds(i, i).contains(e.getPoint())) return;
                Cancion c = modelo.get(i);
                if (!marcadas.remove(c)) marcadas.add(c);   // marca / desmarca
                jlist.repaint();
                actualizarBoton();
            }
        });

        JScrollPane scroll = new JScrollPane(jlist);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        centro.add(scroll, BorderLayout.CENTER);

        BotonPestana cancelar = new BotonPestana("Cancelar");
        cancelar.addActionListener(e -> dispose());
        btnAgregar.setSeleccionado(true);
        btnAgregar.setPreferredSize(new Dimension(180, 45));
        btnAgregar.addActionListener(e -> {
            aceptado = !marcadas.isEmpty();
            dispose();
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(btnAgregar);

        add(arriba, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        mostrar("");
        setSize(580, 560);
        setLocationRelativeTo(dueno);
    }

    private void mostrar(String texto) {
        modelo.clear();
        for (Cancion c : Filtro.filtrar(candidatas, texto)) modelo.addElement(c);
    }

    private void actualizarBoton() {
        btnAgregar.setText(marcadas.isEmpty() ? "Agregar" : "Agregar (" + marcadas.size() + ")");
    }

    public boolean isAceptado() { return aceptado; }

    // Las canciones marcadas, en el orden de la biblioteca
    public ArrayList<Cancion> getSeleccionadas() {
        ArrayList<Cancion> r = new ArrayList<>();
        for (Cancion c : candidatas) {
            if (marcadas.contains(c)) r.add(c);
        }
        return r;
    }

    // Fila con casilla: título y "Artista - Álbum"
    private class FilaMarcar extends JPanel implements ListCellRenderer<Cancion> {
        private final JCheckBox casilla = new JCheckBox();
        private final JLabel titulo = new JLabel();
        private final JLabel detalle = new JLabel();

        FilaMarcar() {
            setLayout(new BorderLayout(10, 0));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

            casilla.setOpaque(false);
            casilla.setFocusable(false);
            titulo.setFont(Estilo.NEGRITA);
            titulo.setForeground(Estilo.TEXTO);
            detalle.setFont(Estilo.NORMAL);
            detalle.setForeground(Estilo.ROJO);

            JPanel textos = new JPanel(new GridLayout(2, 1));
            textos.setOpaque(false);
            textos.add(titulo);
            textos.add(detalle);

            add(casilla, BorderLayout.WEST);
            add(textos, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Cancion> list,
                Cancion c, int index, boolean isSelected, boolean cellHasFocus) {
            casilla.setSelected(marcadas.contains(c));
            titulo.setText(c.getTitulo());
            detalle.setText(c.getArtista() + " - " + c.getAlbum());
            setBackground(isSelected ? Estilo.ARENA : Estilo.ARENA_CLARO);
            return this;
        }
    }
}
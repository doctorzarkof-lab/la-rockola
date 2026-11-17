import estilo.Estilo;
import componentes.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class DialogoConfirmar extends JDialog {

    private boolean confirmado = false;

    // Atajo: abre la ventana y devuelve true si el usuario confirmó
    public static boolean preguntar(Frame dueno, String titulo, String mensaje, String textoSi) {
        DialogoConfirmar d = new DialogoConfirmar(dueno, titulo, mensaje, textoSi);
        d.setVisible(true);
        return d.confirmado;
    }

    private DialogoConfirmar(Frame dueno, String titulo, String mensaje, String textoSi) {
        super(dueno, true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));   // esquinas transparentes

        // Marco café por fuera y caja arena por dentro
        PanelRedondeado marco = new PanelRedondeado(Estilo.CAFE_OSCURO, 32);
        marco.setLayout(new BorderLayout());
        marco.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        PanelRedondeado caja = new PanelRedondeado(Estilo.ARENA_CLARO, 29);
        caja.setLayout(new BorderLayout(0, 12));
        caja.setBorder(BorderFactory.createEmptyBorder(25, 35, 22, 35));

        JLabel icono = new JLabel(new IconoBasuraGrande(), SwingConstants.CENTER);

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(Estilo.NEGRITA.deriveFont(22f));
        lblTitulo.setForeground(Estilo.CAFE_OSCURO);

        JLabel lblMensaje = new JLabel("<html><div style='text-align:center;width:280px'>"
                + escapar(mensaje).replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        lblMensaje.setFont(Estilo.NORMAL);
        lblMensaje.setForeground(Estilo.TEXTO);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 8));
        textos.setOpaque(false);
        textos.add(lblTitulo);
        textos.add(lblMensaje);

        BotonPestana cancelar = new BotonPestana("Cancelar");
        cancelar.setPreferredSize(new Dimension(130, 42));
        cancelar.addActionListener(e -> dispose());

        BotonPestana aceptar = new BotonPestana(textoSi);
        aceptar.setSeleccionado(true);
        aceptar.setPreferredSize(new Dimension(130, 42));
        aceptar.addActionListener(e -> { confirmado = true; dispose(); });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(aceptar);

        caja.add(icono, BorderLayout.NORTH);
        caja.add(textos, BorderLayout.CENTER);
        caja.add(botones, BorderLayout.SOUTH);
        marco.add(caja, BorderLayout.CENTER);
        setContentPane(marco);

        // Esc cierra; Enter queda en "Cancelar" para no borrar sin querer
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().setDefaultButton(cancelar);

        pack();
        setLocationRelativeTo(dueno);
    }

    // Evita que un título con < o & rompa el HTML
    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Basura grande dibujada con código
    private static class IconoBasuraGrande implements Icon {
        public int getIconWidth() { return 56; }
        public int getIconHeight() { return 62; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.ROJO);
            g2.translate(x, y);
            g2.scale(2.5, 2.5);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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
}
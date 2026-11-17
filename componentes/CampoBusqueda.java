package componentes;

import estilo.Estilo;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class CampoBusqueda extends JTextField {

    private Consumer<String> alCambiar;

    public CampoBusqueda() {
        setPreferredSize(new Dimension(380, 46));
        setFont(Estilo.NEGRITA);
        setForeground(Estilo.TEXTO);
        setCaretColor(Estilo.TEXTO);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 52, 0, 16));   // espacio para la lupa

        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { avisar(); }
            public void removeUpdate(DocumentEvent e) { avisar(); }
            public void changedUpdate(DocumentEvent e) { avisar(); }
        });
    }

    public void setAlCambiar(Consumer<String> accion) { this.alCambiar = accion; }

    private void avisar() {
        if (alCambiar != null) alCambiar.accept(getText());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Fondo y borde redondeados
        g2.setColor(Estilo.ARENA_CLARO);
        g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 26, 26));
        g2.setColor(Estilo.CAFE_OSCURO);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(1, 1, w - 3, h - 3, 26, 26));

        // Lupa
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double cy = h / 2.0;
        g2.draw(new Ellipse2D.Double(17, cy - 13, 16, 16));
        g2.draw(new Line2D.Double(30.7, cy + 0.7, 37, cy + 7));

        // Texto de ayuda cuando está vacío
        if (getText().isEmpty()) {
            g2.setFont(getFont());
            g2.setColor(new Color(Estilo.TEXTO.getRed(), Estilo.TEXTO.getGreen(),
                    Estilo.TEXTO.getBlue(), 150));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("Buscar canciones", 52, (h + fm.getAscent() - fm.getDescent()) / 2);
        }
        g2.dispose();

        super.paintComponent(g);   // dibuja el texto que escribes
    }
}
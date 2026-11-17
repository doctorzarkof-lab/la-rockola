import estilo.Estilo;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class LogoDisco extends JComponent {

    public LogoDisco(int tam) {
        setPreferredSize(new Dimension(tam, tam));
        setOpaque(false); // sin fondo propio: se ve el color de atrás
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        double radio = Math.min(getWidth(), getHeight()) / 2.0 - 1;

        // 1. Vinilo
        g2.setColor(Estilo.CAFE_OSCURO);
        g2.fill(circulo(cx, cy, radio));

        // 2. Surcos (anillos finos)
        g2.setColor(new Color(130, 50, 30));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(circulo(cx, cy, radio * 0.85));
        g2.draw(circulo(cx, cy, radio * 0.70));

        // 3. Etiqueta central roja
        g2.setColor(Estilo.ROJO);
        g2.fill(circulo(cx, cy, radio * 0.45));

        // 4. Hueco
        g2.setColor(Estilo.ARENA);
        g2.fill(circulo(cx, cy, radio * 0.10));

        g2.dispose();
    }

    // Círculo a partir de su centro y su radio
    private Ellipse2D circulo(double cx, double cy, double r) {
        return new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
    }
}
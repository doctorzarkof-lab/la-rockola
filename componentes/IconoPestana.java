package componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class IconoPestana implements Icon {

    public enum Tipo { CASA, RELOJ, CORAZON, MAS }

    private final Tipo tipo;
    private final int tam;

    public IconoPestana(Tipo tipo, int tam) {
        this.tipo = tipo;
        this.tam = tam;
    }

    @Override public int getIconWidth()  { return tam; }
    @Override public int getIconHeight() { return tam; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getForeground());
        g2.translate(x, y);
        g2.setStroke(new BasicStroke(tam / 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double t = tam;

        switch (tipo) {
            case MAS:
                g2.draw(new Line2D.Double(t / 2, 3, t / 2, t - 3));
                g2.draw(new Line2D.Double(3, t / 2, t - 3, t / 2));
                break;

            case RELOJ:
                g2.draw(new Ellipse2D.Double(2, 2, t - 4, t - 4));
                g2.draw(new Line2D.Double(t / 2, t / 2, t / 2, t * 0.22));   // manecilla larga
                g2.draw(new Line2D.Double(t / 2, t / 2, t * 0.72, t / 2));   // manecilla corta
                break;

            case CASA:
                Path2D.Double casa = new Path2D.Double();
                casa.moveTo(t / 2, 2);
                casa.lineTo(t - 2, t * 0.5);
                casa.lineTo(t * 0.85, t * 0.5);
                casa.lineTo(t * 0.85, t - 3);
                casa.lineTo(t * 0.15, t - 3);
                casa.lineTo(t * 0.15, t * 0.5);
                casa.lineTo(2, t * 0.5);
                casa.closePath();
                g2.fill(casa);
                break;

            case CORAZON:
                Path2D.Double corazon = new Path2D.Double();
                corazon.moveTo(t / 2, t * 0.9);
                corazon.curveTo(0, t * 0.6, t * 0.05, t * 0.1, t / 2, t * 0.3);
                corazon.curveTo(t * 0.95, t * 0.1, t, t * 0.6, t / 2, t * 0.9);
                corazon.closePath();
                g2.fill(corazon);
                break;
        }
        g2.dispose();
    }
}
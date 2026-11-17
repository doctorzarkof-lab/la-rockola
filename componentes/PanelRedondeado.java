package componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PanelRedondeado extends JPanel {

    private final Color fondo;
    private final int radio;

    public PanelRedondeado(Color fondo, int radio) {
        this.fondo = fondo;
        this.radio = radio;
        setOpaque(false); // el fondo lo pintamos nosotros
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fondo);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radio, radio));
        g2.dispose();
        super.paintComponent(g);
    }
}
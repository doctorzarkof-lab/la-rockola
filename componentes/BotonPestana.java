package componentes;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;
import javax.swing.JButton;

import estilo.Estilo;

public class BotonPestana extends JButton {

    private boolean seleccionado;

    public BotonPestana(String texto) {
        super(texto);
        setFont(Estilo.NEGRITA);
        setForeground(Estilo.ARENA_CLARO);
        setContentAreaFilled(false); // nosotros pintamos el fondo
        setBorderPainted(false);
        setFocusPainted(false);
        setPreferredSize(new Dimension(150, 45));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public BotonPestana(String texto, Icon icono) {
    this(texto);
    setIcon(icono);
    setIconTextGap(10);
    }

    public void setSeleccionado(boolean s) {
        seleccionado = s;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(seleccionado ? Estilo.ROJO : Estilo.CAFE_OSCURO);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
        g2.dispose();
        super.paintComponent(g); // el texto va encima
    }
}
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class GeneradorIcono {

    static BufferedImage dibujar(int lado) {
        LogoDisco logo = new LogoDisco(lado);
        logo.setSize(lado, lado);
        BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        logo.paint(g);
        g.dispose();
        return img;
    }

    static byte[] aPng(BufferedImage img) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        File carpeta = new File("recursos");
        carpeta.mkdirs();

        // PNG grande (Linux y para armar el .icns en Mac)
        ImageIO.write(dibujar(256), "png", new File(carpeta, "LaRockola.png"));

        // ICO con varios tamaños (Windows)
        int[] tamanos = {16, 32, 48, 64, 128, 256};
        ArrayList<byte[]> pngs = new ArrayList<>();
        int total = 0;
        for (int t : tamanos) {
            byte[] p = aPng(dibujar(t));
            pngs.add(p);
            total += p.length;
        }

        int n = tamanos.length;
        int offset = 6 + 16 * n;
        ByteBuffer b = ByteBuffer.allocate(offset + total).order(ByteOrder.LITTLE_ENDIAN);
        b.putShort((short) 0);
        b.putShort((short) 1);
        b.putShort((short) n);
        for (int i = 0; i < n; i++) {
            int t = tamanos[i];
            b.put((byte) (t >= 256 ? 0 : t));
            b.put((byte) (t >= 256 ? 0 : t));
            b.put((byte) 0);
            b.put((byte) 0);
            b.putShort((short) 1);
            b.putShort((short) 32);
            b.putInt(pngs.get(i).length);
            b.putInt(offset);
            offset += pngs.get(i).length;
        }
        for (byte[] p : pngs) b.put(p);

        try (FileOutputStream f = new FileOutputStream(new File(carpeta, "LaRockola.ico"))) {
            f.write(b.array());
        }
        System.out.println("Listo: recursos/LaRockola.png y recursos/LaRockola.ico");
    }
}

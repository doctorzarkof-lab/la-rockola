import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class LectorMp3 {

    public String titulo;
    public String artista = "Desconocido";
    public String album = "Sencillo";
    public int duracionSegundos;

    public LectorMp3(File archivo) {
        // 1. Título desde el nombre del archivo
        String nombre = archivo.getName();
        int punto = nombre.lastIndexOf('.');
        if (punto > 0) nombre = nombre.substring(0, punto);
        nombre = nombre.replace('_', ' ').trim();

        if (nombre.contains(" - ")) {            // "Queen - Bohemian Rhapsody"
            String[] partes = nombre.split(" - ", 2);
            artista = partes[0].trim();
            titulo = partes[1].trim();
        } else {
            titulo = nombre;
        }

        // 2. Duración estimada (128 kbps = 16000 bytes por segundo)
        duracionSegundos = Math.max(1, (int) (archivo.length() / 16000));

        // 3. Si hay etiquetas ID3v1 o es WAV, se mejoran los datos
        leerId3(archivo);
        leerDuracionWav(archivo);
    }

    private void leerId3(File archivo) {
        try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {
            if (raf.length() < 128) return;
            raf.seek(raf.length() - 128);
            byte[] b = new byte[128];
            raf.readFully(b);
            if (b[0] != 'T' || b[1] != 'A' || b[2] != 'G') return;

            String t = texto(b, 3, 30);
            String a = texto(b, 33, 30);
            String al = texto(b, 63, 30);
            if (!t.isEmpty()) titulo = t;
            if (!a.isEmpty()) artista = a;
            if (!al.isEmpty()) album = al;
        } catch (Exception e) { }
    }

    private String texto(byte[] b, int inicio, int largo) {
        return new String(b, inicio, largo, StandardCharsets.ISO_8859_1)
                .replace("\0", "").trim();
    }

    private void leerDuracionWav(File archivo) {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(archivo)) {
            AudioFormat f = in.getFormat();
            long frames = in.getFrameLength();
            if (frames > 0 && f.getFrameRate() > 0) {
                duracionSegundos = Math.max(1, (int) (frames / f.getFrameRate()));
            }
        } catch (Exception e) { }   // no es WAV: se queda la estimación
    }
}
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.SwingUtilities;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Reproductor {

    // Una "sesión" = un hilo que decodifica y reproduce desde un punto de la canción
    private class Sesion implements Runnable {
        final String ruta;
        final long inicioMs;
        volatile boolean cancelada = false;
        volatile long posMs;
        volatile SourceDataLine linea;
        Thread hilo;

        Sesion(String ruta, long inicioMs) {
            this.ruta = ruta;
            this.inicioMs = inicioMs;
            this.posMs = inicioMs;
        }

        // Corta el sonido de inmediato
        void cancelar() {
            cancelada = true;
            SourceDataLine l = linea;
            if (l != null) {
                try { l.stop(); l.flush(); l.close(); } catch (Exception e) { }
            }
        }

        @Override
        public void run() {
            Bitstream bs = null;
            SourceDataLine line = null;
            boolean terminoBien = false;
            try {
                bs = new Bitstream(new BufferedInputStream(new FileInputStream(ruta)));
                Decoder decoder = new Decoder();
                double saltado = 0;     // ms de audio que se saltan hasta llegar al punto pedido
                long base = 0;
                byte[] bytes = new byte[0];

                while (!cancelada) {
                    Header h = bs.readFrame();
                    if (h == null) { terminoBien = true; break; }   // se acabó el archivo

                    if (saltado < inicioMs) {                        // adelantar sin decodificar
                        saltado += h.ms_per_frame();
                        bs.closeFrame();
                        continue;
                    }

                    SampleBuffer out = (SampleBuffer) decoder.decodeFrame(h, bs);
                    bs.closeFrame();

                    if (line == null) {                              // abre la salida de audio
                        AudioFormat f = new AudioFormat(out.getSampleFrequency(), 16,
                                out.getChannelCount(), true, false);
                        line = (SourceDataLine) AudioSystem.getLine(
                                new DataLine.Info(SourceDataLine.class, f));
                        line.open(f);
                        linea = line;
                        if (cancelada) { line.close(); return; }
                        line.start();
                        base = (long) saltado;
                    }

                    short[] s = out.getBuffer();
                    int n = out.getBufferLength();
                    if (bytes.length < n * 2) bytes = new byte[n * 2];
                    for (int i = 0; i < n; i++) {
                        bytes[2 * i] = (byte) (s[i] & 0xff);
                        bytes[2 * i + 1] = (byte) ((s[i] >> 8) & 0xff);
                    }
                    line.write(bytes, 0, n * 2);
                    posMs = base + line.getMicrosecondPosition() / 1000;
                }

                if (terminoBien && !cancelada && line != null) {
                    line.drain();                                    // deja sonar lo que falta
                }
            } catch (Exception e) {
                if (!cancelada) System.out.println("Error al reproducir: " + e.getMessage());
                terminoBien = false;
            } finally {
                try { if (bs != null) bs.close(); } catch (Exception e) { }
                try { if (line != null) line.close(); } catch (Exception e) { }
            }

            // Llegó sola al final: pasa a la siguiente
            if (terminoBien && !cancelada && this == sesion) {
                sonando = false;
                guardadoMs = 0;
                if (alTerminar != null) SwingUtilities.invokeLater(alTerminar);
            }
        }
    }

    private volatile Sesion sesion;
    private Cancion cancion;
    private volatile boolean sonando;
    private volatile long guardadoMs;       // posición guardada al pausar
    private Runnable alTerminar;

    public void setAlTerminar(Runnable r) { this.alTerminar = r; }
    public Cancion getCancion() { return cancion; }
    public boolean estaSonando() { return sonando; }

    public int segundosActuales() {
        Sesion s = sesion;
        long ms = (sonando && s != null) ? s.posMs : guardadoMs;
        return (int) (ms / 1000);
    }

    public void reproducir(Cancion c) {
        cancion = c;
        guardadoMs = 0;
        arrancar(0);
    }

    public void alternar() {
        if (cancion == null) return;
        if (sonando) {
            Sesion s = sesion;
            if (s != null) guardadoMs = s.posMs;
            parar();
            sonando = false;
        } else {
            arrancar(guardadoMs);
        }
    }

    // Adelantar / retroceder
    public void saltarA(int segundos) {
        if (cancion == null) return;
        long ms = segundos * 1000L;
        if (sonando) {
            arrancar(ms);
        } else {
            guardadoMs = ms;
        }
    }

    public void detener() {
        parar();
        sonando = false;
        guardadoMs = 0;
    }

    // Detiene todo y olvida la canción (para cuando se elimina la que suena)
    public void limpiar() {
        detener();
        cancion = null;
    }

    private void arrancar(long ms) {
        parar();                                    // primero se corta lo anterior
        Sesion s = new Sesion(cancion.getRutaAudio(), ms);
        s.hilo = new Thread(s);
        s.hilo.setDaemon(true);
        sesion = s;
        sonando = true;
        s.hilo.start();
    }

    // Cancela la sesión actual y ESPERA a que termine, para que nunca se superpongan
    private void parar() {
        Sesion s = sesion;
        if (s == null) return;
        s.cancelar();
        try {
            if (s.hilo != null) s.hilo.join(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sesion = null;
    }
}
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Almacen {

    private static final File BASE = calcularBase();
    private static final File CARPETA_MUSICA = new File(BASE, "musica");
    private static final File CARPETA_PORTADAS = new File(BASE, "portadas");
    private static final File ARCHIVO = new File(BASE, "canciones.txt");
    private static final File ARCHIVO_PLAYLISTS = new File(BASE, "playlists.txt");

    // Si corre desde un .jar, la base es la carpeta del .jar; si no, la carpeta de trabajo
    private static File calcularBase() {
        try {
            File f = new File(Almacen.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (f.isFile()) return f.getParentFile();
        } catch (Exception e) { }
        return new File(System.getProperty("user.dir"));
    }

    // Convierte una ruta guardada (relativa) en un File real
    public static File resolver(String ruta) {
        if (ruta == null || ruta.isEmpty()) return null;
        File f = new File(ruta);
        return f.isAbsolute() ? f : new File(BASE, ruta);
    }

    // Copia el MP3 a musica/ y devuelve la ruta relativa ("musica/nombre.mp3")
    public static String copiarAudio(String ruta) {
        return copiar(ruta, CARPETA_MUSICA, "musica");
    }

    // Copia la imagen a portadas/ y devuelve la ruta relativa
    public static String copiarFoto(String ruta) {
        return copiar(ruta, CARPETA_PORTADAS, "portadas");
    }

    private static String copiar(String ruta, File carpeta, String relativa) {
        File origen = resolver(ruta);
        if (origen == null || !origen.exists()) return ruta;
        try {
            carpeta.mkdirs();
            // Si ya está dentro de la carpeta, no se vuelve a copiar
            if (origen.getParentFile().getCanonicalFile().equals(carpeta.getCanonicalFile())) {
                return relativa + "/" + origen.getName();
            }
            String nombre = origen.getName();
            int punto = nombre.lastIndexOf('.');
            String sinExt = punto > 0 ? nombre.substring(0, punto) : nombre;
            String ext = punto > 0 ? nombre.substring(punto) : "";

            File destino = new File(carpeta, nombre);
            int n = 1;
            while (destino.exists()) {            // evita pisar otro archivo
                destino = new File(carpeta, sinExt + "-" + n + ext);
                n++;
            }
            Files.copy(origen.toPath(), destino.toPath());
            return relativa + "/" + destino.getName();
        } catch (IOException e) {
            System.out.println("No se pudo copiar el archivo: " + e.getMessage());
            return ruta;
        }
    }

    // ---------- Canciones: una por línea, campos separados por tabulador ----------
    public static void guardar(ListaDoble lista) {
        try (BufferedWriter w = Files.newBufferedWriter(ARCHIVO.toPath(), StandardCharsets.UTF_8)) {
            for (Cancion c : lista.aArrayList()) {
                w.write(String.join("\t",
                        limpio(c.getTitulo()),
                        limpio(c.getArtista()),
                        limpio(c.getAlbum()),
                        String.valueOf(c.getDuracionSegundos()),
                        String.valueOf(c.getReproducciones()),
                        String.valueOf(c.isFavorito()),
                        limpio(c.getRutaAudioGuardada()),
                        limpio(c.getRutaFotoGuardada())));
                w.newLine();
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
        }
    }

    public static void cargar(ListaDoble lista) {
        if (!ARCHIVO.exists()) return;
        try (BufferedReader r = Files.newBufferedReader(ARCHIVO.toPath(), StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = r.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split("\t", -1);
                if (p.length < 8) continue;
                try {
                    Cancion c = new Cancion(p[0], p[1], p[2], Integer.parseInt(p[3]));
                    c.setReproducciones(Integer.parseInt(p[4]));
                    c.setFavorito(Boolean.parseBoolean(p[5]));
                    c.setRutaAudio(p[6].isEmpty() ? null : p[6]);
                    c.setRutaFoto(p[7].isEmpty() ? null : p[7]);
                    lista.agregar(c);
                } catch (NumberFormatException e) { }   // línea dañada: se ignora
            }
        } catch (IOException e) {
            System.out.println("No se pudo cargar: " + e.getMessage());
        }
    }

    // ---------- Playlists: nombre, foto y las rutas de audio de sus canciones ----------
    public static void guardarPlaylists(List<Playlist> playlists) {
        try (BufferedWriter w = Files.newBufferedWriter(ARCHIVO_PLAYLISTS.toPath(),
                StandardCharsets.UTF_8)) {
            for (Playlist p : playlists) {
                StringBuilder sb = new StringBuilder();
                sb.append(limpio(p.getNombre())).append("\t").append(limpio(p.getRutaFotoGuardada()));
                for (Cancion c : p.getCanciones().aArrayList()) {
                    sb.append("\t").append(limpio(c.getRutaAudioGuardada()));
                }
                w.write(sb.toString());
                w.newLine();
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar las playlists: " + e.getMessage());
        }
    }

    // Las canciones se buscan en la biblioteca por su ruta de audio
    public static ArrayList<Playlist> cargarPlaylists(ListaDoble biblioteca) {
        ArrayList<Playlist> resultado = new ArrayList<>();
        if (!ARCHIVO_PLAYLISTS.exists()) return resultado;

        HashMap<String, Cancion> porRuta = new HashMap<>();
        for (Cancion c : biblioteca.aArrayList()) {
            if (c.getRutaAudioGuardada() != null) porRuta.put(c.getRutaAudioGuardada(), c);
        }

        try (BufferedReader r = Files.newBufferedReader(ARCHIVO_PLAYLISTS.toPath(),
                StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = r.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split("\t", -1);
                if (p.length < 2 || p[0].isEmpty()) continue;
                Playlist pl = new Playlist(p[0], p[1].isEmpty() ? null : p[1]);
                for (int i = 2; i < p.length; i++) {
                    Cancion c = porRuta.get(p[i]);
                    if (c != null) pl.agregar(c);
                }
                resultado.add(pl);
            }
        } catch (IOException e) {
            System.out.println("No se pudo cargar las playlists: " + e.getMessage());
        }
        return resultado;
    }

    private static String limpio(String s) {
        return s == null ? "" : s.replace("\t", " ").replace("\n", " ").replace("\r", " ");
    }
}
import javax.swing.*;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Main {

    // Acepta "3:30" o "210". Devuelve -1 si el formato es inválido.
    private static int parsearDuracion(String texto) {
        try {
            texto = texto.trim();
            int segundos;
            if (texto.contains(":")) {
                String[] partes = texto.split(":");
                if (partes.length != 2) return -1;
                int min = Integer.parseInt(partes[0].trim());
                int seg = Integer.parseInt(partes[1].trim());
                if (seg < 0 || seg > 59) return -1;
                segundos = min * 60 + seg;
            } else {
                segundos = Integer.parseInt(texto);
            }
            return segundos > 0 ? segundos : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Cancion pedirCancion() {
        String titulo = JOptionPane.showInputDialog(null, "Título de la canción:",
                "Añadir canción", JOptionPane.QUESTION_MESSAGE);
        if (titulo == null || titulo.trim().isEmpty()) return null;

        String artista = JOptionPane.showInputDialog(null, "Artista:",
                "Añadir canción", JOptionPane.QUESTION_MESSAGE);
        if (artista == null || artista.trim().isEmpty()) return null;

        String textoDur = JOptionPane.showInputDialog(null,
                "Duración (ej: 3:30 o 210 segundos):",
                "Añadir canción", JOptionPane.QUESTION_MESSAGE);
        if (textoDur == null) return null;

        int duracion = parsearDuracion(textoDur);
        if (duracion == -1) {
            JOptionPane.showMessageDialog(null,
                    "Duración inválida. Usa m:ss (3:30) o segundos (210).",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return new Cancion(titulo.trim(), artista.trim(), duracion);
    }

    private static void buscarEnYoutube(Cancion c) {
        try {
            String texto = URLEncoder.encode(c.getTitulo() + " " + c.getArtista(),
                    StandardCharsets.UTF_8);
            String url = "https://www.youtube.com/results?search_query=" + texto;
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                // Plan B para Ubuntu
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudo abrir el navegador.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) { }

        ListaDoble lista = new ListaDoble();
        Pila historial = new Pila();

        lista.agregar(new Cancion("Bohemian Rhapsody", "Queen", 355));
        lista.agregar(new Cancion("Hotel California", "Eagles", 391));
        lista.agregar(new Cancion("Imagine", "John Lennon", 187));

        String[] opciones = {
            "Anterior", "Reproducir", "Siguiente", "Añadir canción",
            "Eliminar canción actual", "Ver lista", "Ver historial",
            "Buscar en YouTube", "Salir"
        };
        String eleccion;

        do {
            Cancion c = lista.getActual();

            String mensaje;
            if (c == null) {
                mensaje = "<html><h2>Reproductor de Música</h2>La lista está vacía.<br>Añade una canción.</html>";
            } else {
                mensaje = "<html><div style='width:280px'>"
                        + "<h2>Reproductor de Música</h2>"
                        + "<b>Pista " + lista.posicionActual() + " de " + lista.getTamano() + "</b><br><br>"
                        + "<b>Título:</b> " + c.getTitulo() + "<br>"
                        + "<b>Artista:</b> " + c.getArtista() + "<br>"
                        + "<b>Duración:</b> " + c.getDuracionTexto() + "<br>"
                        + "<b>Reproducciones:</b> " + c.getReproducciones()
                        + "</div></html>";
            }

            Object sel = JOptionPane.showInputDialog(null, mensaje, "Reproductor",
                    JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[1]);
            eleccion = (sel == null) ? "Salir" : (String) sel;

            // Opciones que necesitan una canción seleccionada
            boolean necesitaCancion = eleccion.equals("Anterior") || eleccion.equals("Reproducir")
                    || eleccion.equals("Siguiente") || eleccion.equals("Eliminar canción actual")
                    || eleccion.equals("Buscar en YouTube");
            if (c == null && necesitaCancion) {
                JOptionPane.showMessageDialog(null, "No hay canciones. Añade una primero.",
                        "Lista vacía", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            switch (eleccion) {
                case "Anterior":
                    lista.anterior();
                    break;
                case "Reproducir":
                    c.incrementarReproducciones();
                    historial.push(c);
                    JOptionPane.showMessageDialog(null,
                            "<html><h3>▶ Sonando</h3>" + c.getTitulo() + " - " + c.getArtista() + "</html>",
                            "Reproduciendo", JOptionPane.INFORMATION_MESSAGE);
                    break;
                case "Siguiente":
                    lista.siguiente();
                    break;
                case "Añadir canción":
                    Cancion nueva = pedirCancion();
                    if (nueva != null) {
                        lista.agregar(nueva);
                        lista.irAUltima();
                    }
                    break;
                case "Eliminar canción actual":
                    int ok = JOptionPane.showConfirmDialog(null,
                            "¿Eliminar \"" + c.getTitulo() + "\"?", "Confirmar",
                            JOptionPane.YES_NO_OPTION);
                    if (ok == JOptionPane.YES_OPTION) lista.eliminarActual();
                    break;
                case "Ver lista":
                    JOptionPane.showMessageDialog(null, lista.textoLista(),
                            "Lista", JOptionPane.PLAIN_MESSAGE);
                    break;
                case "Ver historial":
                    JOptionPane.showMessageDialog(null, historial.textoHistorial(10),
                            "Historial", JOptionPane.PLAIN_MESSAGE);
                    break;
                case "Buscar en YouTube":
                    buscarEnYoutube(c);
                    break;
            }
        } while (!eleccion.equals("Salir"));
    }
}
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;

public class TablaHash {

    // Un nodo de la lista enlazada de cada casilla (así se resuelven las colisiones)
    private static class Entrada {
        String palabra;
        ArrayList<Cancion> canciones = new ArrayList<>();
        Entrada siguiente;
        Entrada(String palabra) { this.palabra = palabra; }
    }

    private final Entrada[] casillas;

    public TablaHash(int capacidad) {
        casillas = new Entrada[capacidad];
    }

    // Función hash: convierte una palabra en un número de casilla
    private int hash(String palabra) {
        int h = 7;
        for (int i = 0; i < palabra.length(); i++) {
            h = h * 31 + palabra.charAt(i);
        }
        return Math.abs(h % casillas.length);
    }

    // Minúsculas y sin tildes: "Canción" -> "cancion"
    public static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    public static ArrayList<String> palabras(String texto) {
        ArrayList<String> lista = new ArrayList<>();
        for (String p : normalizar(texto).split("[^a-z0-9]+")) {
            if (!p.isEmpty()) lista.add(p);
        }
        return lista;
    }

    // Indexa una canción por cada palabra de su título, artista y álbum
    public void agregar(Cancion c) {
        for (String p : palabras(c.getTitulo() + " " + c.getArtista() + " " + c.getAlbum())) {
            agregarPalabra(p, c);
        }
    }

    private void agregarPalabra(String palabra, Cancion c) {
        int i = hash(palabra);
        Entrada e = casillas[i];
        while (e != null && !e.palabra.equals(palabra)) {
            e = e.siguiente;
        }
        if (e == null) {                      // palabra nueva: va al inicio de la casilla
            e = new Entrada(palabra);
            e.siguiente = casillas[i];
            casillas[i] = e;
        }
        if (!e.canciones.contains(c)) e.canciones.add(c);
    }

    // Búsqueda directa de una palabra completa (O(1))
    public ArrayList<Cancion> obtener(String palabra) {
        int i = hash(palabra);
        Entrada e = casillas[i];
        while (e != null) {
            if (e.palabra.equals(palabra)) return e.canciones;
            e = e.siguiente;
        }
        return null;
    }

    // Busca canciones que contengan TODAS las palabras escritas (aunque estén incompletas).
    // Devuelve null si no se escribió nada (significa "no filtrar").
    public ArrayList<Cancion> buscar(String consulta) {
        ArrayList<String> ws = palabras(consulta);
        if (ws.isEmpty()) return null;

        HashSet<Cancion> resultado = null;
        for (String w : ws) {
            HashSet<Cancion> coincide = new HashSet<>();

            ArrayList<Cancion> exacta = obtener(w);          // palabra completa: directo
            if (exacta != null) coincide.addAll(exacta);

            for (Entrada inicio : casillas) {                // palabra incompleta: recorre
                for (Entrada e = inicio; e != null; e = e.siguiente) {
                    if (e.palabra.startsWith(w)) coincide.addAll(e.canciones);
                }
            }

            if (resultado == null) resultado = coincide;
            else resultado.retainAll(coincide);              // deben cumplirse todas
        }
        return new ArrayList<>(resultado);
    }
}
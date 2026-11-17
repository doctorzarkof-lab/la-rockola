import java.util.ArrayList;
import java.util.HashSet;

public class Filtro {

    public static ArrayList<Cancion> filtrar(ArrayList<Cancion> todas, String consulta) {
        TablaHash indice = new TablaHash(64);
        for (Cancion c : todas) indice.agregar(c);

        ArrayList<Cancion> encontradas = indice.buscar(consulta);
        if (encontradas == null) return todas;          // nada escrito: se muestra todo

        HashSet<Cancion> coinciden = new HashSet<>(encontradas);
        ArrayList<Cancion> resultado = new ArrayList<>();
        for (Cancion c : todas) {                       // respeta el orden original
            if (coinciden.contains(c)) resultado.add(c);
        }
        return resultado;
    }
}
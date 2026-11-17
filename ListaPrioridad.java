import java.util.ArrayList;

public class ListaPrioridad {

    // Ordena de mayor a menor número de reproducciones (inserción a mano).
    // Si hay empate, se respeta el orden de la lista de Inicio.
    public static ArrayList<Cancion> masEscuchadas(ArrayList<Cancion> origen) {
        ArrayList<Cancion> orden = new ArrayList<>();
        for (Cancion c : origen) {
            int pos = orden.size();
            // retrocede mientras la anterior tenga MENOS reproducciones
            while (pos > 0 && orden.get(pos - 1).getReproducciones() < c.getReproducciones()) {
                pos--;
            }
            orden.add(pos, c);
        }
        return orden;
    }
}
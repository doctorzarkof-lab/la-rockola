import java.util.ArrayList;

public class Pila {

    private static class NodoPila {
        Cancion dato;
        NodoPila abajo;
        NodoPila(Cancion dato, NodoPila abajo) {
            this.dato = dato;
            this.abajo = abajo;
        }
    }

    private NodoPila tope;
    private int tamano;

    public void push(Cancion c) {
        tope = new NodoPila(c, tope);
        tamano++;
    }

    public Cancion pop() {
        if (tope == null) return null;
        Cancion c = tope.dato;
        tope = tope.abajo;
        tamano--;
        return c;
    }

    public boolean estaVacia() { return tope == null; }
    public int getTamano() { return tamano; }

    public void vaciar() {
        tope = null;
        tamano = 0;
    }

    // Quita todas las apariciones de una canción (por ejemplo, al eliminarla)
    public void quitar(Cancion c) {
        while (tope != null && tope.dato == c) {
            tope = tope.abajo;
            tamano--;
        }
        NodoPila aux = tope;
        while (aux != null && aux.abajo != null) {
            if (aux.abajo.dato == c) {
                aux.abajo = aux.abajo.abajo;
                tamano--;
            } else {
                aux = aux.abajo;
            }
        }
    }

    // De la más reciente (tope) a la más antigua
    public ArrayList<Cancion> aArrayList() {
        ArrayList<Cancion> resultado = new ArrayList<>();
        NodoPila aux = tope;
        while (aux != null) {
            resultado.add(aux.dato);
            aux = aux.abajo;
        }
        return resultado;
    }

    // Muestra las últimas 'max' canciones, de la más reciente a la más antigua
    public String textoHistorial(int max) {
        if (tope == null) return "Todavía no has reproducido nada.";
        StringBuilder sb = new StringBuilder("<html><div style='width:300px'><h3>Historial</h3>");
        NodoPila aux = tope;
        int i = 1;
        while (aux != null && i <= max) {
            sb.append(i).append(". ").append(aux.dato.getTitulo())
              .append(" - ").append(aux.dato.getArtista()).append("<br>");
            aux = aux.abajo;
            i++;
        }
        sb.append("</div></html>");
        return sb.toString();
    }
}
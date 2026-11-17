public class ListaDoble {
    private Nodo cabeza;   // primera canción
    private Nodo actual;   // la que está seleccionada
    private int tamano;

    public boolean estaVacia() { return tamano == 0; }
    public int getTamano() { return tamano; }

    // Inserción al final. La lista es circular: la última apunta a la primera.
    public void agregar(Cancion c) {
        Nodo nuevo = new Nodo(c);
        if (cabeza == null) {
            cabeza = nuevo;
            nuevo.siguiente = nuevo;
            nuevo.anterior = nuevo;
            actual = nuevo;
        } else {
            Nodo ultimo = cabeza.anterior;
            ultimo.siguiente = nuevo;
            nuevo.anterior = ultimo;
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
        }
        tamano++;
    }

    public Cancion getActual() { return actual == null ? null : actual.dato; }

    public void siguiente() { if (actual != null) actual = actual.siguiente; }
    public void anterior()  { if (actual != null) actual = actual.anterior; }
    public void irAUltima() { if (cabeza != null) actual = cabeza.anterior; }

    // Mueve 'actual' hasta la canción indicada
    public boolean seleccionar(Cancion c) {
        if (cabeza == null) return false;
        Nodo aux = cabeza;
        do {
            if (aux.dato == c) { actual = aux; return true; }
            aux = aux.siguiente;
        } while (aux != cabeza);
        return false;
    }

    // Elimina la canción actual y pasa a la siguiente
    public Cancion eliminarActual() {
        if (actual == null) return null;
        Nodo borrar = actual;
        Cancion dato = borrar.dato;

        if (tamano == 1) {
            cabeza = null;
            actual = null;
        } else {
            borrar.anterior.siguiente = borrar.siguiente;
            borrar.siguiente.anterior = borrar.anterior;
            if (borrar == cabeza) cabeza = borrar.siguiente;
            actual = borrar.siguiente;
        }
        tamano--;
        return dato;
    }

    public int posicionActual() {
        int pos = 1;
        Nodo aux = cabeza;
        while (aux != actual) {
            aux = aux.siguiente;
            pos++;
        }
        return pos;
    }

    // Recorre toda la lista una vez (do-while por ser circular)
    public String textoLista() {
        if (cabeza == null) return "La lista está vacía";
        StringBuilder sb = new StringBuilder("<html><div style='width:320px'><h3>Lista de reproducción</h3>");
        Nodo aux = cabeza;
        int i = 1;
        do {
            if (aux == actual) sb.append("<b>▶ ");
            else sb.append("&nbsp;&nbsp;&nbsp;");
            sb.append(i).append(". ").append(aux.dato.getTitulo())
              .append(" - ").append(aux.dato.getArtista())
              .append(" [").append(aux.dato.getDuracionTexto()).append("]");
            if (aux == actual) sb.append("</b>");
            sb.append("<br>");
            aux = aux.siguiente;
            i++;
        } while (aux != cabeza);
        sb.append("</div></html>");
        return sb.toString();
    }

    // Devuelve todas las canciones en orden (recorre la lista una sola vez)
    public java.util.ArrayList<Cancion> aArrayList() {
        java.util.ArrayList<Cancion> resultado = new java.util.ArrayList<>();
        if (cabeza == null) return resultado;
        Nodo aux = cabeza;
        do {
            resultado.add(aux.dato);
            aux = aux.siguiente;
        } while (aux != cabeza);
        return resultado;
    }
}
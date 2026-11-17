import java.io.File;

public class Playlist {

    private String nombre;
    private String rutaFoto;                              // relativa; null si no tiene propia
    private final ListaDoble canciones = new ListaDoble(); // lista doble circular propia

    public Playlist(String nombre, String rutaFoto) {
        this.nombre = nombre;
        this.rutaFoto = rutaFoto;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public ListaDoble getCanciones() { return canciones; }

    public String getRutaFotoGuardada() { return rutaFoto; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }

    // Solo la foto que el usuario eligió para la playlist (o null)
    public String getRutaFotoPropia() {
        File f = Almacen.resolver(rutaFoto);
        return (f != null && f.exists()) ? f.getAbsolutePath() : null;
    }

    // Foto a mostrar: la propia; si no tiene, la de la primera canción que tenga foto
    public String getRutaFoto() {
        String propia = getRutaFotoPropia();
        if (propia != null) return propia;
        for (Cancion c : canciones.aArrayList()) {
            if (c.getRutaFoto() != null) return c.getRutaFoto();
        }
        return null;
    }

    public boolean contiene(Cancion c) {
        return canciones.aArrayList().contains(c);
    }

    public void agregar(Cancion c) {
        if (!contiene(c)) canciones.agregar(c);
    }

    // Quita la canción de la playlist (no la borra de la biblioteca)
    public void quitar(Cancion c) {
        if (canciones.seleccionar(c)) canciones.eliminarActual();
    }
}
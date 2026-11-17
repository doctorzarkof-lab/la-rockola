import java.io.File;

public class Cancion {
    private String titulo;
    private String artista;
    private String album;
    private int duracionSegundos;
    private int reproducciones;
    private boolean favorito;
    private String rutaFoto;    // se guarda RELATIVA (ej: portadas/foto.jpg)
    private String rutaAudio;   // se guarda RELATIVA (ej: musica/cancion.mp3)

    public Cancion(String titulo, String artista, String album, int duracionSegundos) {
        this.titulo = titulo;
        this.artista = artista;
        this.album = album;
        this.duracionSegundos = duracionSegundos;
        this.reproducciones = 0;
        this.favorito = false;
    }

    public Cancion(String titulo, String artista, int duracionSegundos) {
        this(titulo, artista, "Sencillo", duracionSegundos);
    }

    public String getTitulo() { return titulo; }
    public String getArtista() { return artista; }
    public String getAlbum() { return album; }
    public int getDuracionSegundos() { return duracionSegundos; }
    public int getReproducciones() { return reproducciones; }
    public boolean isFavorito() { return favorito; }

    // Rutas REALES (absolutas) para abrir los archivos
    public String getRutaFoto() { return absoluta(rutaFoto); }
    public String getRutaAudio() { return absoluta(rutaAudio); }

    // Rutas tal como se guardan en canciones.txt (relativas)
    public String getRutaFotoGuardada() { return rutaFoto; }
    public String getRutaAudioGuardada() { return rutaAudio; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setArtista(String artista) { this.artista = artista; }
    public void setAlbum(String album) { this.album = album; }
    public void setReproducciones(int reproducciones) { this.reproducciones = reproducciones; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }
    public void setRutaAudio(String rutaAudio) { this.rutaAudio = rutaAudio; }

    public void incrementarReproducciones() { reproducciones++; }

    public String getDuracionTexto() {
        return String.format("%02d:%02d", duracionSegundos / 60, duracionSegundos % 60);
    }

    private String absoluta(String ruta) {
        File f = Almacen.resolver(ruta);
        return f == null ? null : f.getAbsolutePath();
    }

    @Override
    public String toString() {
        return titulo + " - " + artista + " [" + getDuracionTexto() + "]"
                + " (reproducida " + reproducciones
                + (reproducciones == 1 ? " vez)" : " veces)");
    }
}
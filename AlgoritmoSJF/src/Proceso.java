public class Proceso {
    String nombre;
    int tiempoLlegada;
    int tiempoEjecucion;
    int tiempoComienzo;
    int tiempoFinalizacion;
    int tiempoRetorno;
    int tiempoEspera;
    double penalizacion;
    boolean completado;

    public Proceso(String nombre, int tiempoLlegada, int tiempoEjecucion) {
        this.nombre = nombre;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoEjecucion = tiempoEjecucion;
        this.completado = false;
    }
}
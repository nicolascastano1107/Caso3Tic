public class ControlFinClasificadores {
    private int activos;

    public ControlFinClasificadores(int n) {
        activos = n;
    }

    public synchronized boolean ultimoEnTerminar() {
        activos--;
        return activos == 0;
    }
}

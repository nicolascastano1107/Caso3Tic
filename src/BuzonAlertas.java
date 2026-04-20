import java.util.LinkedList;

public class BuzonAlertas {
    private LinkedList<Evento> cola;

    public BuzonAlertas() {
        cola = new LinkedList<>();
    }

    public synchronized void depositar(Evento e) {
        cola.addLast(e);
    }

    public Evento retirar() {
        while (true) {
            synchronized (this) {
                if (!cola.isEmpty()) {
                    return cola.removeFirst();
                }
            }
            Thread.yield();
        }
    }
}

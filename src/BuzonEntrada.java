import java.util.LinkedList;

public class BuzonEntrada {
    private LinkedList<Evento> cola;

    public BuzonEntrada() {
        cola = new LinkedList<>();
    }

    public synchronized void depositar(Evento e) {
        cola.addLast(e);
        notifyAll();
    }

    public synchronized Evento retirar() {
        while (cola.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) { }
        }
        return cola.removeFirst();
    }
}

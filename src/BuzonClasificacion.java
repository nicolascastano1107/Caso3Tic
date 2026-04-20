import java.util.LinkedList;

public class BuzonClasificacion {
    private LinkedList<Evento> cola;
    private int capacidad;

    public BuzonClasificacion(int pCapacidad) {
        capacidad = pCapacidad;
        cola = new LinkedList<Evento>();
    }

    public synchronized void depositar(Evento e) {
        while (cola.size() == capacidad) {
            try {
                wait();
            } catch (InterruptedException ex) { }
        }

        cola.addLast(e);
        notifyAll();
    }

    public void depositarSemiActivo(Evento e) {
        while (true) {
            synchronized (this) {
                if (cola.size() < capacidad) {
                    cola.addLast(e);
                    notifyAll();
                    return;
                }
            }
            Thread.yield();
        }
    }

    public synchronized Evento retirar() {
        while (cola.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) { }
        }

        Evento e = cola.removeFirst();
        notifyAll();
        return e;
    }
}
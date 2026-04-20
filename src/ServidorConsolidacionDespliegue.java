import java.util.concurrent.CyclicBarrier;

public class ServidorConsolidacionDespliegue extends Thread {
    private int idServidor;
    private BuzonConsolidacion buzon;
    private CyclicBarrier barrera;

    public ServidorConsolidacionDespliegue(int pIdServidor, BuzonConsolidacion pBuzon, CyclicBarrier pBarrera) {
        idServidor = pIdServidor;
        buzon = pBuzon;
        barrera = pBarrera;
    }

    public void procesarEvento(Evento e) {
        System.out.println("Servidor " + idServidor +
                           " procesando evento " + e.darId() +
                           " de tipo " + e.darTipo());

        try {
            sleep((long) (100 + Math.random() * 901));
        } catch (InterruptedException ex) { }
    }

    public void run() {
        try { barrera.await(); } catch (Exception ex) { }

        Evento e;
        boolean terminar = false;

        while (!terminar) {
            e = buzon.retirar();

            if (e.esFin()) {
                terminar = true;
                System.out.println("Servidor " + idServidor + " termina.");
            } else {
                procesarEvento(e);
            }
        }
    }
}

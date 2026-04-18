public class ServidorConsolidacionDespliegue extends Thread {
    private int idServidor;
    private BuzonConsolidacion buzon;

    public ServidorConsolidacionDespliegue(int pIdServidor, BuzonConsolidacion pBuzon) {
        idServidor = pIdServidor;
        buzon = pBuzon;
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

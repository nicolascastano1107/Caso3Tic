import java.util.concurrent.CyclicBarrier;

public class Clasificador extends Thread {
    private int idClasificador;
    private BuzonClasificacion buzonClasificacion;
    private BuzonConsolidacion[] buzonesConsolidacion;
    private ControlFinClasificadores control;
    private CyclicBarrier barrera;

    public Clasificador(int pId,
                        BuzonClasificacion pBuzonClasificacion,
                        BuzonConsolidacion[] pBuzonesConsolidacion,
                        ControlFinClasificadores pControl,
                        CyclicBarrier pBarrera) {
        idClasificador = pId;
        buzonClasificacion = pBuzonClasificacion;
        buzonesConsolidacion = pBuzonesConsolidacion;
        control = pControl;
        barrera = pBarrera;
    }

    public void run() {
        try { barrera.await(); } catch (Exception ex) { }

        Evento e;
        int tipo;
        boolean terminar = false;

        while (!terminar) {
            e = buzonClasificacion.retirar();

            if (e.esFin()) {
                terminar = true;
                System.out.println("Clasificador " + idClasificador + " termina.");

                if (control.ultimoEnTerminar()) {
                    for (int i = 0; i < buzonesConsolidacion.length; i++) {
                        buzonesConsolidacion[i].depositar(new Evento(true));
                    }
                }
            } else {
                tipo = e.darTipo();
                buzonesConsolidacion[tipo - 1].depositar(e);

                System.out.println("Clasificador " + idClasificador +
                                   " envió evento " + e.darId() +
                                   " al servidor " + tipo);
            }
        }
    }
}
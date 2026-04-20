import java.util.concurrent.CyclicBarrier;

public class Administrador extends Thread {
    private int nc;
    private BuzonAlertas buzonAlertas;
    private BuzonClasificacion buzonClasificacion;
    private CyclicBarrier barrera;

    public Administrador(int pNc, BuzonAlertas pBuzonAlertas,
                         BuzonClasificacion pBuzonClasificacion, CyclicBarrier pBarrera) {
        nc = pNc;
        buzonAlertas = pBuzonAlertas;
        buzonClasificacion = pBuzonClasificacion;
        barrera = pBarrera;
    }

    public void run() {
        try { barrera.await(); } catch (Exception ex) { }

        boolean terminar = false;
        while (!terminar) {
            Evento e = buzonAlertas.retirar();
            if (e.esFin()) {
                terminar = true;
                for (int i = 0; i < nc; i++) {
                    buzonClasificacion.depositar(new Evento(true));
                }
                System.out.println("Administrador termina, envió " + nc + " FINes a BuzonClasificacion.");
            } else {
                int random = (int) (Math.random() * 21);
                if (random % 4 == 0) {
                    buzonClasificacion.depositarSemiActivo(e);
                }
            }
        }
    }
}

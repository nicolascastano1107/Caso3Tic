import java.util.concurrent.CyclicBarrier;

public class Broker extends Thread {
    private int totalEsperado;
    private BuzonEntrada buzonEntrada;
    private BuzonAlertas buzonAlertas;
    private BuzonClasificacion buzonClasificacion;
    private CyclicBarrier barrera;

    public Broker(int pTotalEsperado, BuzonEntrada pBuzonEntrada,
                  BuzonAlertas pBuzonAlertas, BuzonClasificacion pBuzonClasificacion,
                  CyclicBarrier pBarrera) {
        totalEsperado = pTotalEsperado;
        buzonEntrada = pBuzonEntrada;
        buzonAlertas = pBuzonAlertas;
        buzonClasificacion = pBuzonClasificacion;
        barrera = pBarrera;
    }

    public void run() {
        try { barrera.await(); } catch (Exception ex) { }

        for (int i = 0; i < totalEsperado; i++) {
            Evento e = buzonEntrada.retirar();
            int random = (int) (Math.random() * 201);
            if (random % 8 == 0) {
                buzonAlertas.depositar(e);
            } else {
                buzonClasificacion.depositar(e);
            }
        }
        buzonAlertas.depositar(new Evento(true));
        System.out.println("Broker termina (" + totalEsperado + " eventos procesados).");
    }
}

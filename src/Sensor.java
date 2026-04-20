import java.util.concurrent.CyclicBarrier;

public class Sensor extends Thread {
    private int id;
    private int base;
    private int ns;
    private BuzonEntrada buzonEntrada;
    private CyclicBarrier barrera;

    public Sensor(int pId, int pBase, int pNs, BuzonEntrada pBuzonEntrada, CyclicBarrier pBarrera) {
        id = pId;
        base = pBase;
        ns = pNs;
        buzonEntrada = pBuzonEntrada;
        barrera = pBarrera;
    }

    public void run() {
        try { barrera.await(); } catch (Exception ex) { }

        int total = base * id;
        for (int i = 1; i <= total; i++) {
            int tipo = 1 + (int) (Math.random() * ns);
            buzonEntrada.depositar(new Evento("S" + id + "-" + i, tipo));
        }
        System.out.println("Sensor " + id + " termina (" + total + " eventos).");
    }
}

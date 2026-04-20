import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws Exception {
        int ni = 0, base = 0, nc = 0, ns = 0, tam1 = 0, tam2 = 0;

        BufferedReader br = new BufferedReader(new FileReader("config.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("ni="))   ni   = Integer.parseInt(line.substring(3));
            else if (line.startsWith("base=")) base = Integer.parseInt(line.substring(5));
            else if (line.startsWith("nc="))   nc   = Integer.parseInt(line.substring(3));
            else if (line.startsWith("ns="))   ns   = Integer.parseInt(line.substring(3));
            else if (line.startsWith("tam1=")) tam1 = Integer.parseInt(line.substring(5));
            else if (line.startsWith("tam2=")) tam2 = Integer.parseInt(line.substring(5));
        }
        br.close();

        int totalEsperado = base * ni * (ni + 1) / 2;
        int partes = ni + nc + ns + 2; // sensores + clasificadores + servidores + broker + administrador
        CyclicBarrier barrera = new CyclicBarrier(partes);

        System.out.println("Configuracion: ni=" + ni + " base=" + base + " nc=" + nc +
                           " ns=" + ns + " tam1=" + tam1 + " tam2=" + tam2);
        System.out.println("Total eventos esperados: " + totalEsperado + ", partes barrera: " + partes);

        BuzonEntrada buzonEntrada = new BuzonEntrada();
        BuzonAlertas buzonAlertas = new BuzonAlertas();
        BuzonClasificacion buzonClasificacion = new BuzonClasificacion(tam1);
        BuzonConsolidacion[] buzones = new BuzonConsolidacion[ns];
        ControlFinClasificadores control = new ControlFinClasificadores(nc);

        ServidorConsolidacionDespliegue[] servidores = new ServidorConsolidacionDespliegue[ns];
        for (int i = 0; i < ns; i++) {
            buzones[i] = new BuzonConsolidacion(tam2);
            servidores[i] = new ServidorConsolidacionDespliegue(i + 1, buzones[i], barrera);
        }

        Clasificador[] clasificadores = new Clasificador[nc];
        for (int i = 0; i < nc; i++) {
            clasificadores[i] = new Clasificador(i + 1, buzonClasificacion, buzones, control, barrera);
        }

        Administrador administrador = new Administrador(nc, buzonAlertas, buzonClasificacion, barrera);
        Broker broker = new Broker(totalEsperado, buzonEntrada, buzonAlertas, buzonClasificacion, barrera);

        Sensor[] sensores = new Sensor[ni];
        for (int i = 0; i < ni; i++) {
            sensores[i] = new Sensor(i + 1, base, ns, buzonEntrada, barrera);
        }

        // Orden de arranque: servidores → clasificadores → administrador → broker → sensores
        for (ServidorConsolidacionDespliegue s : servidores) s.start();
        for (Clasificador c : clasificadores) c.start();
        administrador.start();
        broker.start();
        for (Sensor s : sensores) s.start();

        for (ServidorConsolidacionDespliegue s : servidores) s.join();
        for (Clasificador c : clasificadores) c.join();
        administrador.join();
        broker.join();
        for (Sensor s : sensores) s.join();

        System.out.println("Simulacion completada.");
    }
}

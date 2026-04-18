public class PruebaTuParte {
    public static void main(String[] args) {
        int ns = 3;
        int nc = 2;

        BuzonClasificacion buzonClasificacion = new BuzonClasificacion(5);
        BuzonConsolidacion[] buzones = new BuzonConsolidacion[ns];
        ServidorConsolidacionDespliegue[] servidores = new ServidorConsolidacionDespliegue[ns];
        Clasificador[] clasificadores = new Clasificador[nc];
        ControlFinClasificadores control = new ControlFinClasificadores(nc);

        for (int i = 0; i < ns; i++) {
            buzones[i] = new BuzonConsolidacion(3);
            servidores[i] = new ServidorConsolidacionDespliegue(i + 1, buzones[i]);
            servidores[i].start();
        }

        for (int i = 0; i < nc; i++) {
            clasificadores[i] = new Clasificador(i + 1, buzonClasificacion, buzones, control);
            clasificadores[i].start();
        }

        buzonClasificacion.depositar(new Evento("E1", 1));
        buzonClasificacion.depositar(new Evento("E2", 2));
        buzonClasificacion.depositar(new Evento("E3", 3));
        buzonClasificacion.depositar(new Evento("E4", 1));
        buzonClasificacion.depositar(new Evento("E5", 2));

        for (int i = 0; i < nc; i++) {
            buzonClasificacion.depositar(new Evento(true));
        }
    }
}
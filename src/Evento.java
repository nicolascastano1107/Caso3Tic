public class Evento {
    private String id;
    private int tipo;
    private boolean fin;

    public Evento(String pId, int pTipo) {
        id = pId;
        tipo = pTipo;
        fin = false;
    }

    public Evento(boolean pFin) {
        fin = pFin;
        id = "FIN";
        tipo = -1;
    }

    public String darId() {
        return id;
    }

    public int darTipo() {
        return tipo;
    }

    public boolean esFin() {
        return fin;
    }
}
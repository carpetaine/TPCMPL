package edl;

/**
 * classe EltRef definissant le type des references externes
 */
class EltRef {
    public String nomProc; // nom de la procedure definie en REF
    public int nbParam; // nombre de parametres de cette procedure

    public EltRef(String nomProc, int nbParam) {
        this.nomProc = nomProc;
        this.nbParam = nbParam;
    }
}
package edl;

/**
 * classe EltDef definissant le type des points d'entree
 */

class EltDef {
    public String nomProc; // nom de la procedure definie en DEF
    public int adPo; // adresse de debut de code de cette procedure
    public int nbParam; // nombre de parametres de cette procedure

    public EltDef(String nomProc, int adPo, int nbParam) {
        this.nomProc = nomProc;
        this.adPo = adPo;
        this.nbParam = nbParam;
    }
}
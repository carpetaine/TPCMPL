package compilateur;

import analyseurs.UtilLex;

/**
 * Classe définissant la pile des reprises du code
 * (branchements en avant et chaînes de reprises)
 */
public class TPileRep {

	private final int MAXPILEREP = 50;
	private int ip; // sommet de pile
	private int[] T = new int[MAXPILEREP + 1];

	public TPileRep() {
		ip = 0;
	}

	public void empiler(int x) {
		if (ip == MAXPILEREP)
			UtilLex.messErr("débordement de la pile de gestion des reprises");
		ip = ip + 1;
		T[ip] = x;
	}

	public int depiler() {
		if (ip == 0)
			UtilLex.messErr("action dépiler sur chaîne de reprises vide");
		ip = ip - 1;
		return T[ip + 1];
	}

}

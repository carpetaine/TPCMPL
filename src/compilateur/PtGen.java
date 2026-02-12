package compilateur;

import analyseurs.UtilLex;
import edl.*;
import libIO.*;

/**
 * classe de mise en oeuvre du compilateur
 * =======================================
 * (verifications semantiques + production du code objet)
 * 
 * @author Girard, Masson, Perraudeau
 *
 */

public class PtGen {

	// Renseigner ici un nom pour le trinome, constitué UNIQUEMENT DE LETTRES
	public static String trinome = "A COMPLETER"; // TODO

	// taille max de la table des symboles
	private static final int MAXSYMB = 300;

	// TABLE DES SYMBOLES
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];
	private static int it; // indice de remplissage de tabSymb
	private static int bc; // bloc courant (=1 si le bloc courant est le programme principal)

	// codes MAPILE
	private static final int RESERVER = 1, EMPILER = 2, CONTENUG = 3, AFFECTERG = 4,
			OU = 5, ET = 6, NON = 7, INF = 8, INFEG = 9, SUP = 10, SUPEG = 11, EG = 12, DIFF = 13,
			ADD = 14, SOUS = 15, MUL = 16, DIV = 17,
			BSIFAUX = 18, BINCOND = 19,
			LIRENT = 20, LIREBOOL = 21, ECRENT = 22, ECRBOOL = 23,
			ARRET = 24,
			EMPILERADG = 25, EMPILERADL = 26, CONTENUL = 27, AFFECTERL = 28, APPEL = 29, RETOUR = 30;

	// codes des valeurs vrai/faux
	private static final int VRAI = 1, FAUX = 0;

	// types permis
	private static final int ENT = 1, BOOL = 2, NEUTRE = 3;

	// categories des identificateurs
	private static final int CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3,
			PARAMFIXE = 4, PARAMMOD = 5, PROC = 6, DEF = 7, REF = 8, PRIVEE = 9;

	// production du code objet en memoire
	private static ProgObjet po;

	// pile de reprise pour les branchements en avant et en arrière
	private static TPileRep pileRep;

	// compilation des littéraux (entier ou booléen)
	private static int vCour;

	// contrôle de type : type de l'expression compilee
	private static int tCour;

	// TABLE DES SYMBOLES
	// ----------------------
	/**
	 * utilitaire de recherche de l'ident courant (ayant pour code
	 * UtilLex.numIdCourant) dans tabSymb
	 * 
	 * @param borneInf recherche de l'indice it vers borneInf (=1 si recherche
	 *                 dans tout tabSymb)
	 * @return indice de l'ident courant (de code UtilLex.numIdCourant) dans
	 *         tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code UtilLex.numIdCourant de l'ident
	 * @param cat  categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type ENT, BOOL ou NEUTRE
	 * @param info valeur pour une constante, ad d'exécution pour une variable, etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 * utilitaire d'affichage de la table des symboles
	 */
	private static void afftabSymb() {
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}

	// VERIFICATION DE TYPE
	// ----------------------

	/**
	 * verification du type entier de l'expression en cours de compilation
	 * (arret de la compilation sinon)
	 */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}

	/**
	 * verification du type booleen de l'expression en cours de compilation
	 * (arret de la compilation sinon)
	 */
	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

	// COMPILATION SEPAREE
	// -------------------

	// Valeurs possible du vecteur de translation
	private static final int TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// descripteur associe a un programme objet
	private static Descripteur desc;

	/**
	 * modification du vecteur de translation associe au code produit
	 * + incrementation attribut nbTransExt du descripteur
	 * NB: effectue uniquement si c'est une reference externe ou si on compile un
	 * module
	 * 
	 * @param valeur TRANSDON, TRANSCODE ou REFEXT
	 */
	private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}

	// À COMPLÉTER SI BESOIN
	// ---------------------

	/**
	 * initialisations A COMPLETER SI BESOIN
	 * -------------------------------------
	 */
	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;

		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep();
		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();
		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();

		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();

		// initialisation du type de l'expression courante
		tCour = NEUTRE;

		// TODO si necessaire

	}

	/**
	 * code des points de generation A COMPLETER
	 * -----------------------------------------
	 * 
	 * @param numGen : numero du point de generation a executer
	 */
	public static void pt(int numGen) {

		switch (numGen) {
			case 0:
				initialisations();
				break;

			// TODO

			case 255:
				// En fin de compilation :
				// - création des fichiers contenant le code produit (exécutable et mnémonique)
				// - affichage de la table des symboles
				// TODO À compléter si besoin
				po.constObj();
				po.constGen();
				afftabSymb();
				break;

			default:
				System.out.println("Point de generation non prevu dans votre liste");
				break;

		}
	}
}

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
	public static String trinome = "RIO"; 

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

	// adresse d'éxecution à sauvegarder pour une affection
	private static int adresseAff, reservation, addrExec;

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

		reservation = 0;
		adresseAff = -1;
		addrExec = 0;
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

			//|===========================================|@declarations
			//|         D E C L A R A T I O N S			  |		
			//|===========================================|

			// CONSTANTE
			case 1:
				if (presentIdent(bc)>0) {
					UtilLex.messErr("Ré-affectation des constantes interdite");
					break;
				}
				// P R O C
				placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour);
				break;

			// VARGLOBALE
			case 2:
				if (presentIdent(bc)>0) {
					UtilLex.messErr("Ident { "+ UtilLex.chaineIdent(UtilLex.numIdCourant) +" } déjà reservé");
					break;
				}
				// P R O C
				placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, addrExec++);
				// Mises à jour var
				reservation += 1;
				break;

			case 100:
				po.produire(RESERVER);
				po.produire(reservation);
				reservation = 0;
				break;
			
			//|===========================================|@valeur
			//|					V A L E U R				  |
			//|===========================================|

			// M.À.J   T Y P E  après  V A L E U R
			case 3:
				tCour = ENT;
				break;

			case 4:
				tCour = BOOL;
				break;

			// M.À.J   T Y P E  après  I D E N T
			case 40:
				int iVerifType = presentIdent(1);
				if (iVerifType == 0) {UtilLex.messErr("Variable { "+ UtilLex.chaineIdent(UtilLex.numIdCourant) +" } non déclarée");break;}
				tCour = tabSymb[iVerifType].type;
				break;

			// M.À.J   N B E N T I E R
			case 5:
				vCour = UtilLex.valEnt;
				break;

			case 6:
				vCour = -UtilLex.valEnt;
				break;
			
			// M.À.J   B O O L
			case 7:
				vCour = VRAI;
				break;

			case 8:
				vCour = FAUX;
				break;

			//|===========================================|@primaire
			//|				P R I M A I R E				  |
			//|===========================================|

			// I D E N T
			case 9:
				int index = presentIdent(1);
				if (index == 0) { UtilLex.messErr("Variable { "+ UtilLex.chaineIdent(UtilLex.numIdCourant) +" } non déclarée"); break;}

				if (tabSymb[index].categorie == CONSTANTE) {
					po.produire(EMPILER);
					po.produire(tabSymb[index].info);
				} else {
					po.produire(CONTENUG);
					po.produire(tabSymb[index].info);
				}
				break;

			// V A L E U R
			case 10:
				po.produire(EMPILER);
				po.produire(vCour);
				break;

			//|===========================================|@exp
			//|			  E X P R E S S I O N			  |
			//|===========================================|

			// P R O D U C T I O N   C O D E   M A - P I L E
			case 11 :
				po.produire(DIV);
				break;

			case 12:
				po.produire(MUL);
				break;
			
			case 13:
				po.produire(SOUS);
				break;

			case 14:
				po.produire(ADD);
				break;

			case 15:
				po.produire(INFEG);
				break;

			case 16:
				po.produire(INF);
				break;
			
			case 17:
				po.produire(SUPEG);
				break;

			case 18:
				po.produire(SUP);
				break;

			case 19:
				po.produire(DIFF);
				break;
			
			case 20:
				po.produire(EG);
				break;

			case 21:
				po.produire(NON);
				break;

			case 22:
				po.produire(ET);
				break;

			case 23:
				po.produire(OU);
				break;

			// V É R I F I C A T I O N S   de   T Y P E
			case 41:
				verifEnt();
				break;

			case 42:
				verifBool();
				break;

			//|===========================================|@affouappel
			//|			  A F F O U A P P E L			  |
			//|===========================================|

			case 24:
				int iAff = presentIdent(1);
				if (iAff == 0) {UtilLex.messErr("Variable { "+ UtilLex.chaineIdent(UtilLex.numIdCourant) +" } non déclarée !"); break;}
				if (tabSymb[iAff].categorie == CONSTANTE) { UtilLex.messErr("Ré-affectation des constantes interdite !"); break;}
				adresseAff = tabSymb[iAff].info;
				break;

			case 25:
				po.produire(AFFECTERG);
				po.produire(adresseAff);
				break;

			//|===========================================|@lecture
			//|    L E C T U R E   &   E C R I T U R E	  |@ecriture
			//|===========================================|

			case 26:
				int iLec = presentIdent(1);
				if (iLec == 0) {UtilLex.messErr("Variable non déclarée, LECTURE impossible !"); break;}
				if (tabSymb[iLec].categorie == CONSTANTE) {UtilLex.messErr("Ré-affection par LECTURE illégale !"); break;}

				switch (tabSymb[iLec].type) {
					case ENT:
						po.produire(LIRENT);
						break;
					case BOOL:
						po.produire(LIREBOOL);
						break;
				}
				
				po.produire(AFFECTERG);
				po.produire(tabSymb[iLec].info);

				break;

			case 27:
				switch (tCour) {
					case ENT:
						po.produire(ECRENT);
						break;
					case BOOL:
						po.produire(ECRBOOL);
						break;
				}
				break;

			//|===========================================|@inssi
			//|			        I N S S I				  |
			//|===========================================|

			case 28:
				po.produire(BSIFAUX);
				po.produire(-1); // BRANCHEMENT À MODIFIER PLUS TARD
				pileRep.empiler(po.getIpo());
				break;

			case 29:
				po.produire(BINCOND);
				po.produire(-1); // BRANCHEMENT À MODIFIER PLUS TARD
				po.modifier(pileRep.depiler(), po.getIpo()+1);
				pileRep.empiler(po.getIpo());
				break;

			case 30:
				po.modifier(pileRep.depiler(), po.getIpo()+1);
				break;

			//|===========================================|@boucle
			//|			        B O U C L E				  |
			//|===========================================|

			case 31:
				pileRep.empiler(po.getIpo());
				break;

			case 32:
				po.produire(BSIFAUX); // BRANCHÉ APRÈS LE CORPS DE LA BOUCLE
				po.produire(-1); 
				pileRep.empiler(po.getIpo());
				break;

			case 33:
				po.produire(BINCOND); // BRANCHÉ L'EXPRESSION AU DESSUS DU BSIFAUX
				po.modifier(pileRep.depiler(), po.getIpo() +2 );
				po.produire(pileRep.depiler() +1 );
				break;

			//|===========================================|@inscond
			//|			       I N S C O N D			  |
			//|===========================================|

			case 34:
				pileRep.empiler(0);
				break;

			case 35:
				po.produire(BSIFAUX);
				po.produire(0);
				pileRep.empiler(po.getIpo());
				break;

			case 36:
				po.produire(BINCOND);
				po.modifier(pileRep.depiler(), po.getIpo()+2);
				po.produire(pileRep.depiler());
				pileRep.empiler(po.getIpo());
				break;

			case 37:
				int ipoCible = pileRep.depiler();
				int memoire = po.getElt(ipoCible);

				while (memoire != 0) {
					memoire = po.getElt(ipoCible);
					po.modifier(ipoCible, po.getIpo() + 1);
					ipoCible = memoire;
				}
				break;

			// VerifBOOL
			case 38:
				verifBool();
				break;

			// VerifENT
			case 39:
				verifEnt();
				break;

			
				
			case 255:
				// En fin de compilation :
				// - création des fichiers contenant le code produit (exécutable et mnémonique)
				// - affichage de la table des symboles
				// TODO À compléter si besoin
				po.produire(ARRET);
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

package compilateur;

import java.io.*;

import analyseurs.UtilLex;
import libIO.*;
import libMapile.*;

/**
 * Classe de definition du programme objet en memoire
 * et du vecteur de translation associe (pour la compilation separee)
 * 
 * @author Girard, Masson, Perraudeau
 *
 */

public class ProgObjet {

	// Le code objet produit est mémorisé dans le tableau po
	private static final int MAXOBJ = 1000;
	private int[] po = new int[MAXOBJ + 1];
	private int ipo; // indice de remplissage de po

	// Vecteur de translations à effectuer (compilation séparée)
	private int[] vTrans = new int[MAXOBJ + 1];

	// Constructeur
	public ProgObjet() {
		ipo = 0;
		initvTrans();
	}

	/**
	 * methode permettant d'ajouter un code MAPILE ou une valeur
	 * a la fin du code deja produit
	 * 
	 * @param codeOuArg code MAPILE ou bien argument d'un code MAPILE
	 */
	public void produire(int codeOuArg) {
		if (ipo == MAXOBJ)
			UtilLex.messErr("debordement : programme objet trop long");
		ipo = ipo + 1;
		po[ipo] = codeOuArg;
	}

	/**
	 * methode permettant de modifier un code MAPILE ou une valeur
	 * dans le code deja produit (a l'indice i)
	 * 
	 * @param i         indice du code ou argument a modifier
	 * @param codeOuArg code MAPILE ou argument a mettre a jour
	 */
	public void modifier(int i, int codeOuArg) {
		if (i > ipo)
			UtilLex.messErr("programme objet non defini a l'indice " + i);
		po[i] = codeOuArg;
	}

	/**
	 * methode d'acces a l'indice du dernier code produit
	 * 
	 * @return : indice du dernier code produit
	 */
	public int getIpo() {
		return ipo;
	}

	/**
	 * methode d'acces au code ou argument produit a l'indice i
	 * 
	 * @param i : indice recherche
	 * @return : code ou argument deja produit a l'indice i
	 */
	public int getElt(int i) {
		return po[i];
	}

	/**
	 * construction du fichier objet pour MAPILE
	 * -----------------------------------------
	 * NB: le nom du fichier est recupere automatiquement par UtilLex
	 * a partir du main (quand le nom du fichier a compiler est demande)
	 */
	public void constObj() {
		OutputStream f = Ecriture.ouvrir(UtilLex.nomSource + ".obj");
		if (f == null) {
			System.out.println("impossible de creer " + UtilLex.nomSource
					+ ".obj");
			System.exit(1);
		}
		for (int i = 1; i <= ipo; i++)
			if (vTrans[i] != -1)
				Ecriture.ecrireStringln(f, i + "   " + vTrans[i]);
		for (int i = 1; i <= ipo; i++)
			Ecriture.ecrireStringln(f, "" + po[i]);
		Ecriture.fermer(f);
	}

	/**
	 * construction du fichier objet sous forme mnemonique
	 * ---------------------------------------------------
	 */
	public void constGen() {
		Mnemo.creerFichier(ipo, po, UtilLex.nomSource + ".gen");
	}

	/**
	 * Gestion du vecteur de translation associe au programme objet
	 * ------------------------------------------------------------
	 */

	/**
	 * initialisation du vecteur de translation
	 */
	public void initvTrans() {
		for (int i = 1; i <= MAXOBJ; i++)
			vTrans[i] = -1;
	}

	/**
	 * ajout d'un doublet (ipo, x) au vecteur de translation
	 * 
	 * @param x : TRANSDON (1) ou TRANSCODE (2) ou REFEXT (3)
	 */
	public void vecteurTrans(int x) {
		vTrans[ipo] = x;
	}

}
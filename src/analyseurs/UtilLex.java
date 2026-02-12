package analyseurs;

/**
 * La class UtilLex complémente l'ANALYSEUR LEXICAL produit par ANTLR
 * 
 * - Nom du programme compile, sans suffixe : String UtilLex.nomSource
 * 
 * - Attributs lexicaux (selon items figurant dans la grammaire):
 * + int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)
 * + int UtilLex.numIdCourant = code du dernier identificateur lu (item ident)
 * 
 * - Methodes utiles
 * + void UtilLex.messErr(String m) affichage de m et arret compilation
 * + String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId
 * + void afftabSymb() affiche la table des symboles
 * 
 * @author Grazon
 */

public class UtilLex {

	public static String nomSource; // nom de l'unite compilee, sans suffixe
	public static int numIdCourant; // codage du dernier ident lu (attribut lexical de ident)
	public static int valEnt; // valeur du dernier nombre entier lu (attribut lexical de nbentier)
	private static int numLigne; // numero de la ligne courante

	/*
	 * messages d'erreur
	 * ----------------
	 */
	public static class CompilationError extends RuntimeException {
		public CompilationError(String messErr) {
			super(messErr);
		}
	}

	public static void arret() {
		System.out.println("erreur, arrêt de la compilation");
		throw new CompilationError("exception déclenchée volontairement par UtilLex.arret");
	}

	/**
	 * gestion des erreurs
	 * erreurs de nature semantique uniquement
	 * 
	 * @param message a afficher
	 *
	 */
	public static void messErr(String message) {
		System.out.println("erreur, ligne numero : " + numLigne);
		System.out.println(message);
		arret();
	}

	/*
	 * gestion des terminaux et de la table des identificateurs
	 * --------------------------------------------------------
	 */

	private static final int MAXID = 2000;
	private static int nbId; // indice de remplissage de la table identificateurs
	private static String[] identificateurs;

	public static void initialisation() {
		nbId = 0; // indice de remplissage de la table identificateurs a remettre a zero entre
					// deux compilations
		identificateurs = new String[MAXID];// table identificateurs a vider entre deux compilations
		numIdCourant = 0; // codage du dernier ident lu
		valEnt = 0; // valeur du dernier nombre entier lu
		numLigne = 1; // numero de la ligne courante
	}

	/**
	 * permet de chercher un identificateur dans la table des idents
	 * et de le placer s'il n'y est pas
	 * (idents predefinis geres eux par Antlr)
	 * 
	 * @param ident a chercher (et a placer si non trouve)
	 * @return numId (index) de l'ident
	 */
	private static int chercherId(String id) {
		/* message d'erreur si la table d'identificateurs est pleine */
		// la table identificateurs est numurotee de 0 a MAXID-1 et le dernier
		// range est nbId-1
		// nbId est donc le nombre d'identificateurs connus
		int indice = 0;
		while (indice < nbId && !id.equals(identificateurs[indice])) {
			indice++;
		}

		if (indice == nbId) { // nouvel identificateur
			if (nbId == MAXID) {
				messErr("Debordement de la table des identificateurs");
			}
			;
			identificateurs[nbId] = id;
			nbId = nbId + 1; // on a alors indice=nbId-1
		}
		// else identificateur deja connu, rien a faire, indice est l'endroit ou
		// on l'a trouve
		return indice;
	}

	/**
	 * obtention de la representation textuelle de l'identificateur de numId i
	 * 
	 * @param numId (index) d'un ident
	 * @return chaine correspondant à l'ident
	 */
	public static String chaineIdent(int i) {
		if (i >= nbId || i < 0)
			messErr("chaineIdent sur num ident errone");
		return identificateurs[i];
	}

	/**
	 * permet la mise a jour de l'attribut lexical numIdCourant
	 * 
	 * @param ident courant
	 */
	public static void traiterId(String id) {
		numIdCourant = chercherId(id.toLowerCase());
	}

	/**
	 * permet la mise a jour de numLigne dans le lexical, au moment ou on lit une
	 * fin de ligne
	 * 
	 * incrementeligne permet de compter les lignes en passant sur le caractere
	 * "entree" dans le lexical
	 * sert uniquement au traitement des identificateur non reserves et au
	 * traitement des erreurs
	 */
	public static void incrementeLigne() {
		numLigne++;
	}

}

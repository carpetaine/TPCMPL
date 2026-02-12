package compilateur;

import analyseurs.UtilLex;

/**
 * Classe de définition d'un élément d'une table des symboles
 */
public class EltTabSymb {

	// Dans la table, 4 champs par symbole
	public int code, categorie, type, info;

	public EltTabSymb(int code, int categorie, int type, int info) {
		this.code = code;
		this.categorie = categorie;
		this.type = type;
		this.info = info;
	}

	public String toString() {
		final String[] chcat = { "", "CONSTANTE      ", "VARGLOBALE     ",
				"VARLOCALE      ", "PARAMFIXE      ", "PARAMMOD       ",
				"PROC           ", "DEF            ", "REF            ",
				"PRIVEE         " };
		final String[] chtype = { "", "ENT     ", "BOOL    ", "NEUTRE  " };
		String ch = "";
		if (code == -1)
			ch += "-1";
		else
			ch += "@" + UtilLex.chaineIdent(code);
		while (ch.length() < 15)
			ch += ' ';
		return ch + chcat[categorie] + chtype[type] + info;
	}
}

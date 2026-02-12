package compilateur;

/**
 * Programme fourni aux étudiant·es QUI NE DOIVENT PAS LE MODIFIER
 * 
 * Ce programme contient le main qui demande le nom du programme source que l'on souhaite compiler,
 * et lance sa compilation à partir de l'axiome "unite" de la grammaire projet.g
 * 
 * On peut compiler plusieurs programmes sources de suite (arrêt par un retour-chariot)
 *  
 * @author Girard, Masson, Perraudeau
 */

import java.io.*;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import libIO.*;
import analyseurs.*;

public class Compilateur {

	private static void uneCompilation(String nomDuSource) {
		try {
			// Production d'un flot d'unités lexicales
			ANTLRFileStream input = new ANTLRFileStream(nomDuSource);
			projetLexer lexer = new projetLexer(input);
			CommonTokenStream token_stream = new CommonTokenStream(lexer);

			// Création analyseur syntaxique
			projetParser parser = new projetParser(token_stream);

			// Point de génération des initialisations
			PtGen.pt(0);

			// Exécution de la procédure de l'axiome "unite"
			parser.unite();
		} catch (IOException exc) {
			// Fichier source entré inexistant
			System.out.println("IO exception: " + exc);
		} catch (RecognitionException re) {
			// Erreur syntaxique détectée par le lexer ou le parser
			System.out.println("Recognition exception: " + re);
		} catch (UtilLex.CompilationError ce) {
			// Erreur de compilation, détectée par les points de génération
			System.out.println("Erreur de compilation: " + ce);
		}
	}

	public static void main(String[] args) {
		prelude();
		if (args.length == 0) {
			modeInteractif();
		}
		// Mode en ligne de commande
		for (String filename : args) {
			if (setGlobalFilename(filename)) {
				uneCompilation(filename);
			}
		}
		postlude();
	}

	private static boolean setGlobalFilename(String filename) {
		String name = new File(filename).getName();
		if (name.endsWith(".pro")) {
			// UtilLex.nomSource doit être défini sans l'extension
			UtilLex.nomSource = name.substring(0, name.lastIndexOf(".pro"));
			return true;
		} else {
			System.out.println("On attend un fichier d'extension .pro");
			return false;
		}
	}

	private static void prelude() {
		System.out.println("PROJET DE COMPILATION version : " + PtGen.trinome);
		System.out.println("--------------------------------" + "-".repeat(PtGen.trinome.length()));
		System.out.println();
	}

	private static void postlude() {
		System.out.println("\n \n Projet de " + PtGen.trinome + " terminé");
	}

	private static void modeInteractif() {
		prelude();
		do {
			// Lecture du nom de fichier en entrée, sans suffixe
			System.out.println();
			System.out.print("Donnez le nom du fichier que vous souhaitez compiler, sans suffixe :  (RC si terminé) ");
			UtilLex.nomSource = Lecture.lireString();
			System.out.println();

			// Déclenchement de la compilation du fichier
			if (!UtilLex.nomSource.equals("")) {
				uneCompilation(UtilLex.nomSource + ".pro");
			}
		} while (!UtilLex.nomSource.equals(""));
	}
}
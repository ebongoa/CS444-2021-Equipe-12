package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

/**
 * Génération de code pour un programme JCas à partir d'un arbre décoré.
 */

class Generation {
   
   /**
    * Méthode principale de génération de code.
    * Génère du code pour l'arbre décoré a.
    */
   static Prog coder(Arbre a) {
      Prog.ajouterGrosComment("Programme généré par JCasc");

      // -----------
      // A COMPLETER
      // -----------

      coder_LISTEDECL(a.getFils1());
      coder_LISTEINST(a.getFils2());

      // Fin du programme
      // L'instruction "HALT"
      Inst inst = Inst.creation0(Operation.HALT);
      // On ajoute l'instruction à la fin du programme
      Prog.ajouter(inst);

      // On retourne le programme assembleur généré
      return Prog.instance(); 
   }


   private static void coder_LISTEDECL(Arbre a) {
	   
   }


	private static void coder_LISTEINST(Arbre a) {
		switch (a.getNoeud()) {
			case Vide :
				break;
			case ListeInst :
				coder_LISTEINST(a.getFils1());
				coder_INST(a.getFils2());
				break;
		
			default:
				break;
		}
	   }


   private static void coder_INST(Arbre a) {
	   switch (a.getNoeud()) {
		   case Ecriture :
				coder_LISTE_EXP(a.getFils1());
				break;
	
			default:
				break;
		}
	}


	private static void coder_LISTE_EXP(Arbre a) {
		switch (a.getNoeud()) {
			case Vide :
				break;
			case ListeExp:
				coder_LISTE_EXP(a.getFils1());
				coder_EXP(a.getFils2());
				break;
			default:
				break;
		}
		
	}
	
	private static void coder_EXP(Arbre a) {
		switch (a.getNoeud()) {
			case Chaine:
				String val = new String(a.getChaine());
				Inst instWSTR = Inst.creation1(Operation.WSTR, Operande.creationOpChaine(val));
			    Prog.ajouter(instWSTR);
				break;
			default:
				break;
		}
	}
}




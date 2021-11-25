package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

/**
 * GÃ©nÃ©ration de code pour un programme JCas Ã  partir d'un arbre dÃ©corÃ©.
 */

class Generation {
   
   /**
    * MÃ©thode principale de gÃ©nÃ©ration de code.
    * GÃ©nÃ¨re du code pour l'arbre dÃ©corÃ© a.
    */
   static Prog coder(Arbre a) {
      Prog.ajouterGrosComment("Programme gÃ©nÃ©rÃ© par JCasc");

      // -----------
      // A COMPLETER
      // -----------
      
      
      // Fin du programme
      // L'instruction "HALT"
      Inst inst = Inst.creation0(Operation.HALT);
      // On ajoute l'instruction Ã  la fin du programme
      Prog.ajouter(inst);

      // On retourne le programme assembleur gÃ©nÃ©rÃ©
      return Prog.instance(); 
   }
   
   static Etiq Nouvelle_Etiq(String chaine) {
	   return Etiq.nouvelle(chaine);
   }

   static void coder_Cond(Arbre c, boolean saut, Etiq etiq) {
	   Noeud noeud = c.getNoeud();
	   
	   switch(noeud) {
	   case Ident :
		   if(c.getChaine().equals(String.valueOf(saut))) {
			   Inst inst = Inst.creation1(Operation.BRA, new OperandeEtiq(etiq));
			   Prog.ajouter(inst);
		   }
		   else if(c.getChaine().equals(String.valueOf(!saut))) {
			   null; // il n'y a rien à faire
		   }
		   else {
			   if(saut) {
				   Inst inst1 = Inst.creation2(Operation.LOAD, new OperandeChaine(c.getChaine()),Operande.R0);
				   Prog.ajouter(inst1);
				   Inst inst2 = Inst.creation2(Operation.CMP, new OperandeEntier(0), Operande.R0);
				   Prog.ajouter(inst2);
				   Inst inst3 = Inst.creation1(Operation.BNE, new OperandeEtiq(etiq));
				   Prog.ajouter(inst3);
			   }
			   else {
				   Inst inst1 = Inst.creation2(Operation.LOAD, new OperandeChaine(c.getChaine()),Operande.R0);
				   Prog.ajouter(inst1);
				   Inst inst2 = Inst.creation2(Operation.CMP, new OperandeEntier(0), Operande.R0);
				   Prog.ajouter(inst2);
				   Inst inst3 = Inst.creation1(Operation.BEQ, new OperandeEtiq(etiq));
				   Prog.ajouter(inst3);
			   }
		   }
		   break;
	   case Et :
		   if(saut) {
			   Etiq etiq_fin = Nouvelle_Etiq(etiq.toString()+"_fin");
			   Coder_Cond(c.getFils1(), false, etiq_fin);
			   Coder_Cond(c.getFils2(), true, etiq);
			   Prog.ajouter(etiq_fin);
		   }
		   else {
			   Coder_Cond(c.getFils1(), false, etiq);
			   Coder_Cond(c.getFils2(), false, etiq);
		   }
		   break;
	   case Ou :
		   if(saut) {
			   Coder_Cond(c.getFils1(), true, etiq);
			   Coder_Cond(c.getFils2(), true, etiq);
		   }
		   else {
			   Etiq etiq_fin = Nouvelle_Etiq(etiq.toString()+"_fin");
			   Coder_Cond(c.getFils1(), true, etiq_fin);
			   Coder_Cond(c.getFils2(), false, etiq);
			   Prog.ajouter(etiq_fin);
		   }
		   break;
	   case Non :
		   Coder_Cond(c.getFils1(), !saut, etiq);
		   break;
	   // Opérateurs de comparaison =, <, >, !=, ≤, et ≥
	   case Egal :
		   Coder_Expr(c.getFils1(), Registre.R1);
		   Coder_Expr(c.getFils2(), Registre.R2);
		   Inst inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   Inst inst2 = Inst.creation1(Operation.BEQ, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   Inst inst2 = Inst.creation1(Operation.BNE, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case Inf :
		   Coder_Expr(c.getFils1(), Registre.R1);
		   Coder_Expr(c.getFils2(), Registre.R2);
		   Inst inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   Inst inst2 = Inst.creation1(Operation.BLT, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   Inst inst2 = Inst.creation1(Operation.BGE, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case Sup :
		   Coder_Expr(c.getFils1(), Registre.R1);
		   Coder_Expr(c.getFils2(), Registre.R2);
		   Inst inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   Inst inst2 = Inst.creation1(Operation.BGT, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   Inst inst2 = Inst.creation1(Operation.BLE, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case NonEgal :
		   Coder_Expr(c.getFils1(), Registre.R1);
		   Coder_Expr(c.getFils2(), Registre.R2);
		   Inst inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   Inst inst2 = Inst.creation1(Operation.BNE, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   Inst inst2 = Inst.creation1(Operation.BEQ, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case InfEgal : 
		   Coder_Expr(c.getFils1(), Registre.R1);
		   Coder_Expr(c.getFils2(), Registre.R2);
		   Inst inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   Inst inst2 = Inst.creation1(Operation.BLE, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   Inst inst2 = Inst.creation1(Operation.BGT, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case SupEgal :
		   Coder_Expr(c.getFils1(), Registre.R1);
		   Coder_Expr(c.getFils2(), Registre.R2);
		   Inst inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   Inst inst2 = Inst.creation1(Operation.BGE, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   Inst inst2 = Inst.creation1(Operation.BLT, new OperandeEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case default :
		   break;
	   }

   }
   
   static void Coder_Inst(Arbre a) {
	   Noeud noeud = a.getNoeud();
	   
	   switch(noeud) {
	   case Si :
		   Etiq etiq_Fin = Nouvelle_Etiq("Si_Fin");
		   if(a.getFils3().getNoeud().equals(Noeud.Vide)) {
			   Coder_Cond(a.getFils1(), false, etiq_Fin);
			   Coder_Inst(a.getFils2());
			   Prog.ajouter(etiq_Fin);
		   }
		   else {
			   Etiq etiq_Sinon = Nouvelle_Etiq("Sinon");
			   Coder_Cond(a.getFils1(), false, etiq_Sinon);
			   Coder_Inst(a.getFils2());
			   Inst inst = Inst.creation1(Operation.BRA, new OperandeEtiq(etiq_Fin));
			   Prog.ajouter(inst);
			   Prog.ajouter(etiq_Sinon);
			   Coder_Inst(a.getFils3());
			   Prog.ajouter(etiq_Fin);
		   }
		   break;
	   case Pour :
		   /* Etiq etiq_Cond = Nouvelle_Etiq("Cond");
		   Etiq etiq_Debut = Nouvelle_Etiq("Debut");
		   Inst inst = Inst.creation1(Operation.BRA, new OperandeEtiq(etiq_Cond));
		   Prog.ajouter(inst);
		   Prog.ajouter(etiq_Debut);
		   Coder_Inst(a.getFils2());
		   Prog.ajouter(etiq_Cond);
		   Coder_Cond(a.getFils1(), true, new OperandeEtiq(etiq_Debut)); */
		   break;
	   case TantQue :
		   Etiq etiq_Cond = Nouvelle_Etiq("Cond");
		   Etiq etiq_Debut = Nouvelle_Etiq("Debut");
		   Inst inst = Inst.creation1(Operation.BRA, new OperandeEtiq(etiq_Cond));
		   Prog.ajouter(inst);
		   Prog.ajouter(etiq_Debut);
		   Coder_Inst(a.getFils2());
		   Prog.ajouter(etiq_Cond);
		   Coder_Cond(a.getFils1(), true, new OperandeEtiq(etiq_Debut));
		   break;
	   case default :
		   break;
	   }
	   
   }
   
}




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

   
   static void coder_Expr(Arbre a, Operande r)
   {
	   //@TODO
   }
   
   
   static void coder_Expr(Arbre a, Registre r)
   {
	   //@Placeholder
   }
   
   static void coder_Cond(Arbre c, boolean saut, Etiq etiq) {
	   Noeud noeud = c.getNoeud();
	   Inst inst,inst1,inst2 , inst3 ;
	   Etiq etiq_fin ;
	   switch(noeud) {
	   case Ident :
		   if(c.getChaine().equals(String.valueOf(saut))) {
			   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst);
		   }
		   else if(c.getChaine().equals(String.valueOf(!saut))) {
			   return; // il n'y a rien à faire
		   }
		   else {
			   if(saut) {
				   inst1 = Inst.creation2(Operation.LOAD, Operande.creationOpChaine(c.getChaine()),Operande.R0);
				   Prog.ajouter(inst1);
				   inst2 = Inst.creation2(Operation.CMP, Operande.creationOpEntier(0), Operande.R0);
				   Prog.ajouter(inst2);
				   inst3 = Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiq));
				   Prog.ajouter(inst3);
			   }
			   else {
				   inst1 = Inst.creation2(Operation.LOAD, Operande.creationOpChaine(c.getChaine()),Operande.R0);
				   Prog.ajouter(inst1);
				   inst2 = Inst.creation2(Operation.CMP, Operande.creationOpEntier(0), Operande.R0);
				   Prog.ajouter(inst2);
				   inst3 = Inst.creation1(Operation.BEQ, Operande.creationOpEtiq(etiq));
				   Prog.ajouter(inst3);
			   }
		   }
		   break;
	   case Et :
		   if(saut) {
			   etiq_fin = Nouvelle_Etiq(etiq.toString()+"_fin");
			   coder_Cond(c.getFils1(), false, etiq_fin);
			   coder_Cond(c.getFils2(), true, etiq);
			   Prog.ajouter(etiq_fin);
		   }
		   else {
			   coder_Cond(c.getFils1(), false, etiq);
			   coder_Cond(c.getFils2(), false, etiq);
		   }
		   break;
	   case Ou :
		   if(saut) {
			   coder_Cond(c.getFils1(), true, etiq);
			   coder_Cond(c.getFils2(), true, etiq);
		   }
		   else {
			   etiq_fin = Nouvelle_Etiq(etiq.toString()+"_fin");
			   coder_Cond(c.getFils1(), true, etiq_fin);
			   coder_Cond(c.getFils2(), false, etiq);
			   Prog.ajouter(etiq_fin);
		   }
		   break;
	   case Non :
		   coder_Cond(c.getFils1(), !saut, etiq);
		   break;
	   // Opérateurs de comparaison =, <, >, !=, ≤, et ≥
	   case Egal :
		   coder_Expr(c.getFils1(), Registre.R1);
		   coder_Expr(c.getFils2(), Registre.R2);
		   inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   inst2 = Inst.creation1(Operation.BEQ, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   inst2 = Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case Inf :
		   coder_Expr(c.getFils1(), Registre.R1);
		   coder_Expr(c.getFils2(), Registre.R2);
		   inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   inst2 = Inst.creation1(Operation.BLT, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   inst2 = Inst.creation1(Operation.BGE, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case Sup :
		   coder_Expr(c.getFils1(), Registre.R1);
		   coder_Expr(c.getFils2(), Registre.R2);
		   inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   inst2 = Inst.creation1(Operation.BGT, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   inst2 = Inst.creation1(Operation.BLE, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case NonEgal :
		   coder_Expr(c.getFils1(), Registre.R1);
		   coder_Expr(c.getFils2(), Registre.R2);
		   inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   inst2 = Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   inst2 = Inst.creation1(Operation.BEQ, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case InfEgal : 
		   coder_Expr(c.getFils1(), Registre.R1);
		   coder_Expr(c.getFils2(), Registre.R2);
		   inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   inst2 = Inst.creation1(Operation.BLE, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   Inst inst2 = Inst.creation1(Operation.BGT, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case SupEgal :
		   coder_Expr(c.getFils1(), Registre.R1);
		   coder_Expr(c.getFils2(), Registre.R2);
		   inst1 = Inst.creation2(Operation.CMP, Operande.R2, Operande.R1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   inst2 = Inst.creation1(Operation.BGE, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   inst2 = Inst.creation1(Operation.BLT, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   default:
		   break;
	   }

   }
   
   static void Coder_Inst(Arbre a) {
	   Noeud noeud = a.getNoeud();
	   Inst inst,inst1 ;
	   Etiq etiq_Debut,etiq_Fin,etiq_Cond,etiq_Sinon;
	   switch(noeud) {
	   case Si :
		   etiq_Fin = Nouvelle_Etiq("Si_Fin");
		   if(a.getFils3().getNoeud().equals(Noeud.Vide)) {
			   coder_Cond(a.getFils1(), false, etiq_Fin);
			   Coder_Inst(a.getFils2());
			   Prog.ajouter(etiq_Fin);
		   }
		   else {
			   etiq_Sinon = Nouvelle_Etiq("Sinon");
			   coder_Cond(a.getFils1(), false, etiq_Sinon);
			   Coder_Inst(a.getFils2());
			   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq_Fin));
			   Prog.ajouter(inst);
			   Prog.ajouter(etiq_Sinon);
			   Coder_Inst(a.getFils3());
			   Prog.ajouter(etiq_Fin);
		   }
		   break;
	   case Pour :
		   etiq_Cond = Nouvelle_Etiq("Cond");
		   etiq_Debut = Nouvelle_Etiq("Debut");
		   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq_Cond));
		   Prog.ajouter(inst);
		   Prog.ajouter(etiq_Debut);
		   Coder_Inst(a.getFils2());
		   Prog.ajouter(etiq_Cond);
		   coder_Cond(a.getFils1(), true, Operande.creationOpEtiq(etiq_Debut));
		   break;
	   case TantQue :
		   etiq_Cond = Nouvelle_Etiq("Cond");
		   etiq_Debut = Nouvelle_Etiq("Debut");
		   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq_Cond));
		   Prog.ajouter(inst);
		   Prog.ajouter(etiq_Debut);
		   Coder_Inst(a.getFils2());
		   Prog.ajouter(etiq_Cond);
		   coder_Cond(a.getFils1(), true, Operande.creationOpEtiq(etiq_Debut));
		   break;
	   default :
		   break;
	   }
	   
   }
   
}




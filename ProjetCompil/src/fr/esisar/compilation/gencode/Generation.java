package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

/**
 * GÃ©nÃ©ration de code pour un programme JCas Ã  partir d'un arbre dÃ©corÃ©.
 */

class Generation {
	static boolean used[] = new boolean[16];
   
   /**
    * MÃ©thode principale de gÃ©nÃ©ration de code.
    * GÃ©nÃ¨re du code pour l'arbre dÃ©corÃ© a.
 * @throws Exception 
    */
   static Prog coder(Arbre a) throws Exception {
      Prog.ajouterGrosComment("Programme gÃ©nÃ©rÃ© par JCasc");

      // -----------
      // A COMPLETER
      // -----------
      coder_Liste_Decl(a.getFils1());
      coder_Liste_Inst(a.getFils2());

      
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

   static void coder_Liste_Decl(Arbre a) throws Exception
   {
	   Noeud noeud = a.getNoeud();
	   
	   if(noeud != Noeud.Vide) {
		   // (List_Decl Decl) | Decl
		   Arbre f1 = a.getFils1();
		   Arbre f2 = a.getFils2();
			
		   noeud = f1.getNoeud();	
			// List_Decl | vide
			switch(noeud) {
				
				case ListeDecl:
					coder_Liste_Decl(f1);
					break;
					
				case Vide :
					break;
			
			default:
				throw new Exception("Décor incohérent ligne : " + f1.getNumLigne());
				
			}
			noeud = f2.getNoeud();
			//Decl , rien d'autre (même pas vide)
			if (noeud.equals( Noeud.Decl))
			{
				//Placer en mémoire
			}
			else
			{
				throw new Exception("Décor incohérent ligne : " + f1.getNumLigne());
			}
		}
	   
   }
   
   static void coder_Expr(Arbre a, Operande r)
   {
	    Noeud noeud = a.getNoeud();
  
		switch (noeud) {
		case Chaine:
			String val = new String(a.getChaine());
			Inst instWSTR = Inst.creation1(Operation.WSTR, Operande.creationOpChaine(val));
		    Prog.ajouter(instWSTR);
			break;
		default:
			break;
	}
   }
   
   static void coder_Expr(Arbre a, Registre r)
   {
	   //@Placeholder
   }
     
   static void coder_Liste_Expr(Arbre a) {
		Noeud noeud = a.getNoeud();
	
		switch (noeud) {
			case Vide :
				break;
			case ListeExp:
				coder_Liste_Expr(a.getFils1());
				coder_Expr(a.getFils2(),(Operande)null);
				break;
			default:
				break;
		}
		
	}

   
   static void coder_Cond(Arbre c, boolean saut, Etiq etiq) throws Exception {
	   Noeud noeud = c.getNoeud();
	   Inst inst ,inst1,inst2 , inst3 ;
	   Etiq etiq_fin ;
	   Operande reg1 = allouer_Reg() ,reg2 = allouer_Reg();
	   switch(noeud) {
	   case Ident :
		   if(c.getChaine().equals(String.valueOf(saut))) {
			   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst);
		   }
		   else if (!(c.getChaine().equals(String.valueOf(!saut)))){
			   if(saut) {
				   inst1 = Inst.creation2(Operation.LOAD, Operande.creationOpChaine(c.getChaine()),allouer_Reg());
				   Prog.ajouter(inst1);
				   inst2 = Inst.creation2(Operation.CMP, Operande.creationOpEntier(0), allouer_Reg());
				   Prog.ajouter(inst2);
				   inst3 = Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiq));
				   Prog.ajouter(inst3);
			   }
			   else {
				   inst1 = Inst.creation2(Operation.LOAD, Operande.creationOpChaine(c.getChaine()),allouer_Reg());
				   Prog.ajouter(inst1);
				   inst2 = Inst.creation2(Operation.CMP, Operande.creationOpEntier(0), allouer_Reg());
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
		   coder_Expr(c.getFils1(), reg1);
		   coder_Expr(c.getFils2(), reg2);
		   inst1 = Inst.creation2(Operation.CMP, reg2, reg1);
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
		   coder_Expr(c.getFils1(), reg1);
		   coder_Expr(c.getFils2(), reg2);
		   inst1 = Inst.creation2(Operation.CMP, reg2, reg1);
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
		   coder_Expr(c.getFils1(), reg1);
		   coder_Expr(c.getFils2(), reg2);
		   inst1 = Inst.creation2(Operation.CMP, reg2, reg1);
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
		   coder_Expr(c.getFils1(), reg1);
		   coder_Expr(c.getFils2(), reg2);
		   inst1 = Inst.creation2(Operation.CMP, reg2, reg1);
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
		   coder_Expr(c.getFils1(), reg1);
		   coder_Expr(c.getFils2(), reg2);
		   inst1 = Inst.creation2(Operation.CMP, reg2, reg1);
		   Prog.ajouter(inst1);
		   if(saut) {
			   inst2 = Inst.creation1(Operation.BLE, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   else {
			   inst2 = Inst.creation1(Operation.BGT, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst2);
		   }
		   break;
	   case SupEgal :
		   coder_Expr(c.getFils1(), reg1);
		   coder_Expr(c.getFils2(), reg2);
		   inst1 = Inst.creation2(Operation.CMP, reg2, reg1);
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
	   free_Reg(reg1);
	   free_Reg(reg2);
   }
   
   
   static void coder_Inst(Arbre a) throws Exception {
	   Noeud noeud = a.getNoeud();
	   Inst inst,inst1 ;
	   Etiq etiq_Debut,etiq_Fin,etiq_Cond,etiq_Sinon;
	   switch(noeud) {
	   case Si :
		   etiq_Fin = Nouvelle_Etiq("Si_Fin");
		   if(a.getFils3().getNoeud().equals(Noeud.Vide)) {
			   coder_Cond(a.getFils1(), false, etiq_Fin);
			   coder_Inst(a.getFils2());
			   Prog.ajouter(etiq_Fin);
		   }
		   else {
			   etiq_Sinon = Nouvelle_Etiq("Sinon");
			   coder_Cond(a.getFils1(), false, etiq_Sinon);
			   coder_Inst(a.getFils2());
			   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq_Fin));
			   Prog.ajouter(inst);
			   Prog.ajouter(etiq_Sinon);
			   coder_Inst(a.getFils3());
			   Prog.ajouter(etiq_Fin);
		   }
		   break;
	   case Pour :
		   etiq_Cond = Nouvelle_Etiq("Cond");
		   etiq_Debut = Nouvelle_Etiq("Debut");
		   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq_Cond));
		   Prog.ajouter(inst);
		   Prog.ajouter(etiq_Debut);
		   coder_Inst(a.getFils2());
		   Prog.ajouter(etiq_Cond);
		   coder_Cond(a.getFils1(), true, etiq_Debut);
		   break;
	   case TantQue :
		   etiq_Cond = Nouvelle_Etiq("Cond");
		   etiq_Debut = Nouvelle_Etiq("Debut");
		   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq_Cond));
		   Prog.ajouter(inst);
		   Prog.ajouter(etiq_Debut);
		   coder_Inst(a.getFils2());
		   Prog.ajouter(etiq_Cond);
		   coder_Cond(a.getFils1(), true, etiq_Debut);
		   break;
		   
	   case Ecriture :
			coder_Liste_Expr(a.getFils1());
			break;
			
	   default :
		   break;
	   }
	   
   }
   
   static void coder_Liste_Inst(Arbre a) throws Exception {
		Noeud noeud = a.getNoeud();
		switch (noeud) {
			case Vide :
				break;
			case ListeInst :
				coder_Liste_Inst(a.getFils1());
				coder_Inst(a.getFils2());
				break;
		
			default:
				break;
		}
	}


   
   
   
   static private Operande allouer_Reg() throws Exception
   {
	   Operande to_return = null;
	   int ri = 0;
	   while (to_return == null && ri < 16)
	   {
		   if (!used[ri])
		   {
			   used[ri] = true;
			   to_return = Operande.opDirect(Registre.valueOf("R" + ri));
	   		}
	   	 
		   else
			   ri++;
	   }
	   if (to_return == null) throw new Exception("Out of register!");
	   else return to_return;
   }

   static private void free_Reg(Operande ri) throws Exception
   {
	   if (!(ri.getNature().equals(NatureOperande.OpDirect))) throw new Exception("free_Reg called on a non-register operande!");
	   
	   int i = -1;
	   
	   switch(ri.getRegistre())
	   {   	   
	   	   case R0:
	   			i = 0;
	   			break;
	   	   case R1:
	   			i = 1;
	   			break;	
	   	   case R2:
	   			i = 2;
	   			break;
	   	   case R3:
	   			i = 3;
	   			break;	   			   			
	   	   case R4:
	   			i = 4;	   			
	   			break;
	   	   case R5:
	   			i = 5;
	   			break;	
	   	   case R6:
	   			i = 6;
	   			break;
	   	   case R7:
	   			i = 7;
	   			break;			   
	   	   case R8:
	   			i = 8;
	   			break;
	   	   case R9:
	   			i = 9;
	   			break;	
	   	   case R10:
	   			i = 10;
	   			break;
	   	   case R11:
	   			i = 11;
	   			break;	   			   			
	   	   case R12:
	   			i = 12;
	   			break;
	   	   case R13:
	   			i = 13;
	   			break;	
	   	   case R14:
	   			i = 14;
	   			break;
	   	   case R15:
	   			i = 15;
	   			break;
	   		default:
	   			//LB ou RB
	   			break;
	   }
	   if (i != -1) used[i] = false;
   }

}




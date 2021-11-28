package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

/**
 * GÃ©nÃ©ration de code pour un programme JCas Ã  partir d'un arbre dÃ©corÃ©.
 */

class Generation {
	static private boolean used[] = new boolean[16];
	static private int pointeur_pile = 0;
	static private Etiq end = Etiq.lEtiq("end");
	static private Etiq hlt = Etiq.lEtiq("hlt");
   
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
      
      // Etiquettes de structure
     
      // program :
      Prog.ajouterGrosComment("program                    ");
      coder_Liste_Decl(a.getFils1());
      if (pointeur_pile > 0 ) 
      {
    	  verifier_Pile_OV(pointeur_pile);
    	  Prog.ajouter(Inst.creation1(Operation.ADDSP,Operande.creationOpEntier(pointeur_pile)));
      }
      
      
      // begin :
      Prog.ajouterGrosComment("begin                     ");
      coder_Liste_Inst(a.getFils2());
     
      // Fin du programme
      Prog.ajouterGrosComment("end                       ");
      
      Prog.ajouter(end);    
      if (pointeur_pile > 0 ) Prog.ajouter(Inst.creation1(Operation.SUBSP,Operande.creationOpEntier(pointeur_pile)));
   
      // L'instruction "HALT"
      Prog.ajouter(hlt);
      Inst inst = Inst.creation0(Operation.HALT);
      // On ajoute l'instruction Ã  la fin du programme
      Prog.ajouter(inst);

      // En cas d'erreur 
      Prog.ajouterComment("Erreurs a l'execution");      
      coder_Erreur(Prog.L_Etiq_Debordement_Arith,"Erreur a l'execution : debordement arithmetique");
      coder_Erreur(Prog.L_Etiq_Pile_Pleine,"Erreur a l'execution : debordement de la pile");
      coder_Erreur(Prog.L_Etiq_Debordement_Intervalle,"Erreur a l'execution : debordement d'intervalle");
      coder_Erreur(Prog.L_Etiq_Debordement_Indice,"Erreur a l'execution : debordement d'indice dans un tableau");
      
      // On retourne le programme assembleur gÃ©nÃ©rÃ©
      return Prog.instance(); 
   }
   
   static Etiq Nouvelle_Etiq(String chaine) {
	   return Etiq.nouvelle(chaine);
   }

   // ############################ Erreurs ################################
   
   private static void coder_Erreur(Etiq etiq , String comm)
   {	      
	      Prog.ajouter(etiq);
	      Prog.ajouter(Inst.creation1(Operation.WSTR,Operande.creationOpChaine(comm)));
	      Prog.ajouter(Inst.creation0(Operation.WNL));
	      Prog.ajouter(Inst.creation1(Operation.BRA, Operande.creationOpEtiq(hlt)));
   }
   
   // ############################ Declarations ################################
   
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
				allouer_Pile(f2.getFils1());
			}
			else
			{
				throw new Exception("Décor incohérent ligne : " + f1.getNumLigne());
			}
		}
	   
   }
   

   // ############################ Expressions ################################
   
   static void coder_Expr(Arbre a, Registre r)
   {
	   //@Placeholder
   }
     
   
   static void coder_Expr(Arbre a, Operande r)
   {
	    NatureType type = a.getDecor().getType().getNature();
  
		switch (type) 
		{
		default:
			break;
		}
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

   // ############################ Arithmetique ################################
   // ############################ Booleens ################################
   
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
   
   
   // ############################ Instructions ################################
   
   static void coder_Inst(Arbre a) throws Exception {
	   Noeud noeud = a.getNoeud();
	   Inst inst,inst1 ;
	   Etiq etiq_Debut,etiq_Fin,etiq_Cond,etiq_Sinon;

	   
	   switch(noeud) {
		case Affect:
			//coder_Affect(a);
			break;
	   
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
		   
		case Lecture:
			coder_IO(a,true);
			break;
		   
	   case Ecriture :	
		    coder_IO(a,false);
			break;
			
	   case Ligne:
		    // WNL             : ecriture newline
			Prog.ajouter(Inst.creation0(Operation.WNL));
			break;
			
	   case Nop:
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

   // True/False -> Lecture/Ecriture
   static private void coder_IO(Arbre a,boolean io) throws Exception
   {	 
	if (io)
	{
		//coder_Lecture
	}
	else 
	{ 
		switch (a.getNoeud()) 
		{
		case Ecriture:
			coder_IO(a.getFils1(),false);
			break;
	
		// (Liste_Exp , Exp) | (Vide,Exp)
		case ListeExp:
			coder_IO(a.getFils1(),false);

			
			Arbre f2 = a.getFils2();
			// String | Interval | Real
			switch (f2.getDecor().getType().getNature()) 
			{
			
			    // WSTR "..."      : ecriture de la chaine (meme notation qu'en langage Cas)
				case String:
					Prog.ajouter(Inst.creation1(Operation.WSTR, Operande.creationOpChaine(f2.getChaine())));
					break;

				// WINT            : ecriture de l'entier V[R1]
				case Interval:
					// Mise dans R1 (f2)
					Prog.ajouter(Inst.creation0(Operation.WINT));
					break;

				// WFLOAT          : ecriture du flottant V[R1]
				case Real:
					// Mise dans R1 (f2)
					Prog.ajouter(Inst.creation0(Operation.WFLOAT));
					break;

				default:
					throw new Exception("Decor incoherent ligne : " + f2.getNumLigne());
			}
			break;
			
		case Vide:
			break;
		
		default:
			throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
		}
	}
   }
   
   
   // ############################ Memoire ################################
    
   
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
   
   static private void allouer_Pile(Arbre lident) throws Exception
   {
	   Defn def = lident.getFils2().getDecor().getDefn();
	   NatureType nature = def.getType().getNature();

	   switch (nature)
	   {
	   		case Interval:
	   		case Boolean:
	   		case Real:
	   			def.setOperande(Operande.creationOpIndirect(pointeur_pile, Registre.GB));
	   			pointeur_pile++;
	   			break;
	   			
	   		case Array:
	   			Type tab = def.getType().getIndice();
	   			int inf = tab.getBorneInf();
	   			int sup = tab.getBorneSup();
	   			
	   			if (inf < sup)
	   			{
	   				def.setOperande(Operande.creationOpIndirect(pointeur_pile, Registre.GB));
	   				pointeur_pile += (1+sup-inf);
	   			}
	   			else
	   			{
	   				System.out.println("/!\\ Tableau de taille nulle ligne : " + lident.getNumLigne());
	   			}
	   			break;
	   		
	   		default:
	   			throw new Exception("Decor incoherent ligne : " +lident.getNumLigne());
	   }
	   
	   // A priori code Liste_Decl appel Decl par Decl
	   /*
	   if( lident.getFils1().getNoeud() == Noeud.ListeIdent) 
	   {
		   allouer_Pile(lident.getFils1());
	   }
	   */											
	}   
   
   // A placer après chaque ajout en pile
   static private void verifier_Pile_OV(int offset) 
   {
	Prog.ajouter(Inst.creation1(Operation.TSTO, Operande.creationOpEntier(offset)));
	Prog.ajouter(Inst.creation1(Operation.BOV, Operande.creationOpEtiq(Prog.L_Etiq_Pile_Pleine)));
   }
   
   // A placer après chaque operateur arithmetique risquant de faire un overflow
   static private void verifier_Arith_OV(Arbre a)
	{
	    // L'overflow sera causé par l'appel à une instruction arithmetique , on met juste un BOV après chacun
		Prog.ajouter(Inst.creation1(Operation.BOV, Operande.creationOpEtiq(Prog.L_Etiq_Debordement_Arith)));
	}
   
   static private void verifier_Interval_OV(Operande operande, Type bornes)
	{
		Operande etiq = Operande.creationOpEtiq(Prog.L_Etiq_Debordement_Intervalle);
		int inf = bornes.getBorneInf(),sup = bornes.getBorneSup();	
		
		if (inf != -java.lang.Integer.MAX_VALUE)
		{
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.creationOpEntier(inf), operande));
			Prog.ajouter(Inst.creation1(Operation.BLT, etiq));
		}

		if (sup != java.lang.Integer.MAX_VALUE)
		{
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.creationOpEntier(sup), operande));
			Prog.ajouter(Inst.creation1(Operation.BLT, etiq));
		}
	}
	
   static private void verifier_Tableau_OV(Operande operande, Type bornes)
	{
		Operande etiq = Operande.creationOpEtiq(Prog.L_Etiq_Debordement_Indice);
		int inf = bornes.getBorneInf(),sup = bornes.getBorneSup();	
		
		if (inf != -java.lang.Integer.MAX_VALUE)
		{
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.creationOpEntier(inf), operande));
			Prog.ajouter(Inst.creation1(Operation.BLT, etiq));
		}

		if (sup != java.lang.Integer.MAX_VALUE)
		{
			Prog.ajouter(Inst.creation2(Operation.CMP, Operande.creationOpEntier(sup), operande));
			Prog.ajouter(Inst.creation1(Operation.BLT, etiq));
		}
		
	}
   
}




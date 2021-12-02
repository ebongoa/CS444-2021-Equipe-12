package fr.esisar.compilation.gencode;

import java.util.Arrays;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

/**
 * GÃ©nÃ©ration de code pour un programme JCas Ã  partir d'un arbre dÃ©corÃ©.
 */

class Generation {
	// les états 
	static private boolean used[] = new boolean[16];
	static private int last_saved = -1;
	
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
    	  verifier_Pile_OV();
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
   
    
   // Une expression est similaire à une instruction , cependant une expression retourne un type 
   static void coder_Expr(Arbre a, Operande r) throws Exception
   {
	    NatureType nature = a.getDecor().getType().getNature();
		int[] regs = allouer(1);
		Operande rd = int_to_Op(regs[0]);
		

		switch(nature) 
		{
			case String:
				break;
			case Boolean:	
				coder_Expr_Bool(a,rd);
				break;
			case Real:
			case Interval:
				evaluer_Expr_Arith(a,rd);
				//verifier_Arith_OV(); // normalement deja appele par evaluer_Expr_Arith
				break;
			case Array:
				Arbre f1 = a.getFils1();
				switch (f1.getNoeud())
				{
					case Ident:
				   		Operande addr = operande_Ident(f1);
				   		Prog.ajouter(Inst.creation2(Operation.LEA, addr, rd));
				   		break;
				    case Index:
				   																						// A faire
				   		break;
				   	default:
				   		throw new Exception("Décor incohérent ligne : "+a.getNumLigne());
				}
				break;
			default:
				throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
		}
		desallouer(regs);
		/*                                                              Un bout qu'a commence alberte 
		Noeud noeud = a.getNoeud();
		
		// a est une feuille de l'arbre, arit� 0
		switch(noeud) {
		case Chaine:
			Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpChaine(a.getChaine()), r));
			break;
		case Reel:
			Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpReel(a.getReel()), r));
			break;
		case Entier:
			Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpEntier(a.getEntier()), r));
			break;
		case Ident:
			Prog.ajouter(Inst.creation2(Operation.LOAD, operande_Ident(a), r));	
			break;
		default:
			break;
		}
		
		int[] regs = allouer(1);
		Operande rd = int_to_Op(regs[0]);
		evaluer_Expr_Arith(a,rd);

		desallouer(regs);
		verifier_Arith_OV();*/
   }
   
   static void coder_Liste_Expr(Arbre a) throws Exception {
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
	   Operande op_Etiq = Operande.creationOpEtiq(etiq);
	   Operation option; //utiliser pour les cas de comparaison
	   //Allocation des registres
	   int[] regs = allouer(2);
	   Operande reg1 = int_to_Op(regs[0]) ,reg2 = int_to_Op(regs[1]);
	   
	   switch(noeud) {
	   case Ident :
		   if(c.getChaine().equals(String.valueOf(saut))) {
			   inst = Inst.creation1(Operation.BRA, Operande.creationOpEtiq(etiq));
			   Prog.ajouter(inst);
		   }
		   else if (!(c.getChaine().equals(String.valueOf(!saut)))){
			   if(saut) {
				   inst1 = Inst.creation2(Operation.LOAD, Operande.creationOpChaine(c.getChaine()),reg1);
				   Prog.ajouter(inst1);
				   inst2 = Inst.creation2(Operation.CMP, Operande.creationOpEntier(0), reg2);
				   Prog.ajouter(inst2);
				   inst3 = Inst.creation1(Operation.BNE, Operande.creationOpEtiq(etiq));
				   Prog.ajouter(inst3);
			   }
			   else {
				   inst1 = Inst.creation2(Operation.LOAD, Operande.creationOpChaine(c.getChaine()),reg2);
				   Prog.ajouter(inst1);
				   inst2 = Inst.creation2(Operation.CMP, Operande.creationOpEntier(0), reg2);
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
	   case Inf :
	   case Sup :
	   case NonEgal :
	   case InfEgal : 
	   case SupEgal :
		   option = coder_Operation_Comparaison(c);
		   if(saut) 
		   {
			   Prog.ajouter(Inst.creation1(option, op_Etiq));
		   } 
		   else 
		   {
			   Prog.ajouter(Inst.creation1(op_Comparaison(option), op_Etiq));
		   }
		   break;
	   default:
		   break;
	   }
	   //Restauration des registres
	   desallouer(regs);
   }
   
   static private void coder_Affect(Arbre a) throws Exception
   {
	   // a = b + c
	   // il faut d'abord evaluer b+c (dans un registre de travail)
	   // puis l'injecter dans a grace a son addresse (OpIndirect)
	   
	   int[] regs = allouer(2);
	   /**************************************************************************
	    * PLACE 		-> IDENT | IDENT est un identificateur de variable  
						|  Noeud.Index(PLACE, EXP)     
	    **************************************************************************/
	   
	   Arbre f1 = a.getFils1();
	   Operande ident = int_to_Op(regs[0]);
	   switch (f1.getNoeud())
	   {
	   		case Ident:
	   			Operande addr = operande_Ident(f1);
	   			Prog.ajouter(Inst.creation2(Operation.LEA, addr, ident));
	   			break;
	   		case Index:
	   																						// A faire
	   			break;
	   		default:
	   			throw new Exception("Décor incohérent ligne : "+a.getNumLigne());
	   }
	   
	   // b + c
	   Operande expr = int_to_Op(regs[1]);
	   coder_Expr(a.getFils2(), expr);
	   
	   Type type = f1.getDecor().getType();
	   NatureType nature = type.getNature();	   
	   switch(nature)
	   {	   		
	   		case Real:
	   		case Boolean:
	   		case Interval:
	   			if (nature.equals(NatureType.Interval)) verifier_Interval_OV(expr,type); 
	   			Prog.ajouter(Inst.creation2(Operation.STORE, expr, ident));
	   			break;
	   		case Array:   			
	   			int taille = 1;
   				Type indices;
   				int delta;

   				//Pour gérer les tableaux de tableaux
	   			while(type.getNature() == NatureType.Array) 
	   			{
	   				indices = type.getIndice();
	   				delta = indices.getBorneSup()-indices.getBorneInf();
	   				
	   				// A[i][j] 
	   				if( delta > 0 )
	   				{
	   					taille *= delta+1;	
	   				} 
	   				else //C'est un typle tableau avec aucun éléments
	   				{
	   					taille = 0;
	   				}
	   					
	   				//Si c'est un tableau de tableau on va reboucler
	   				type = type.getElement();
	   			}
	   			//																									//A faire  On recopie le tableau
	   			break;   
	   		default:
	   			throw new Exception("NatureType incohérent ligne : "+a.getNumLigne());
	   }
	   desallouer(regs);
   }
   
   // on recopie un tableau d'objets dans un autre
   static private Object[] recopier_tableau(Object[] tableau)
   {
	   		return Arrays.copyOf(tableau, tableau.length);																										//A faire  On recopie le tableau
   }
   
   // ############################ Bool ################################
	static void coder_Expr_Bool(Arbre a, Operande rdest) throws Exception 
	{
		/* si vrai on injecte l'equivalent de true (1) sinon l'equivalent de false (0) dans rdest
		 * traitement
		 * comparaison
		 * jump conditionnel
		 * injecte true
		 * jump
		 * label : faux
		 * injecte false
		 * label : fin
		 */
		
		Etiq faux = Etiq.lEtiq("cas_faux");
		Etiq fin = Etiq.lEtiq("fin");
		
		// traitement + comparaison + jump conditionnel
		coder_Cond(a, false, faux);	
		// injecte true
		Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpEntier(1), rdest));
		// jump
		Prog.ajouter(Inst.creation1(Operation.BRA, Operande.creationOpEtiq(fin)));
		// label : faux
		Prog.ajouter(faux);
		//  injecte false
		Prog.ajouter(Inst.creation2(Operation.LOAD, Operande.creationOpEntier(0), rdest));
		// label : fin
		Prog.ajouter(fin);
	}
   
   
   // Opérateurs de comparaison =, <, >, !=, ≤, et ≥
   // Prends un noeud en entree et retourne l'Operation correspondante. si revert = true alors retourne l'Operation contraire
   static private Operation op_Comparaison(Noeud n,boolean revert) throws Exception 
   {
	   Operation to_return;
		   switch(n) 
		   {			
	   			case Egal:
	   				to_return = Operation.BEQ;
	   			case Inf:
	   				to_return = Operation.BLT;
	   			case Sup:
	   				to_return = Operation.BGT;
	   			case NonEgal:
	   				to_return = Operation.BNE;
	   			case InfEgal:
	   				to_return = Operation.BLE;
	   			case SupEgal:
	   				to_return = Operation.BGE;

	   			default:
	   				to_return = null;
		  }
	  return (revert) ? op_Comparaison(to_return) : to_return;
  }
   
   //Un alias pour revert = false
   static private Operation op_Comparaison(Noeud n) throws Exception
   { return op_Comparaison(n,false);}
 
   //Un alias pour revert = true , prend une Operation en entree
   static private Operation op_Comparaison(Operation op) throws Exception
   {
	   switch (op)
	   {
	   		case BEQ:
	   			return Operation.BNE;
	   		case BLT:
	   			return Operation.BGE;
	   		case BGT:
	   			return Operation.BLE;
	   		case BNE:
	   			return Operation.BEQ;
	   		case BLE:
	   			return Operation.BGT;
	   		case BGE:
	   			return Operation.BLT;
			default:
   				return null;
		   
	   }
   }
   
   //Evalue les expressions fils , ajouter un CMP et renvoie quel branchement utiliser
   static private Operation coder_Operation_Comparaison(Arbre a) throws Exception
   {
	   Operation to_return = op_Comparaison(a.getNoeud());
	   if (to_return == null)
		   throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
	   else
	   {
		   int regs[] = allouer(2); 
		   // Allocation des registres
		   Operande reg1 = int_to_Op(regs[0]), reg2 = int_to_Op(regs[1]); 
	   
		   // Evaluation des expressions
		   coder_Expr(a.getFils1(), reg1);
		   /// Verification ici ou dans coder_Expr ?
		   coder_Expr(a.getFils2(), reg2);
		   /// Verification ici ou dans coder_Expr ?
	   
		   // Comparaison (i.e update des flags)
		   Prog.ajouter(Inst.creation2(Operation.CMP, reg2, reg1));
	   
		   // Liberation des registres
		   desallouer(regs);
   
		   return to_return;
	   }
   }
   
   // ############################ Arith ################################
  
   /*
   
   MoinsUnaire(1),
   Conversion(1, 1),
   PlusUnaire(1),
   
   Moins(2, 1),
   Plus(2, 1),
   DivReel(2, 1),
   Reste(2, 1),
   Mult(2, 1),
   Quotient(2, 1),
   */
   //On suppose que l'appelant à déjà alloué rdest
   static private void evaluer_Expr_Arith(Arbre a, Operande rdest) throws Exception
   {
	   //en fonction de l'arite il nous faudra allouer (ou non) un nouveau registre
	   Operande opande;
	   Operation option;
	   int[] regs = null;
	   switch (a.getArite())
	   {
	   
	    case 0: 
	    	option = Operation.LOAD;
	    	if (a.getNoeud().equals(Noeud.Ident))
	    	{
	    		opande = operande_Ident(a);
	    	}
	    	else 
	    	{
	    		opande = operande_Arithmetique(a);
	    	}
			//factorise , apres le switch
			//Prog.ajouter(Inst.creation2(option, opande, rdest));			
	    	break;
	   	case 1:
	   		//On evalue dans le sens naturel , gauche -> droite
	   		evaluer_Expr_Arith(a.getFils1(), rdest);
			//rdest a été modifié
	   		opande = rdest;
	   		
			switch (a.getNoeud()) 
			{
				case MoinsUnaire:
					option = Operation.OPP; // -rdest
					break;
				case Conversion:			
					option = Operation.FLOAT;// cast
					break;
				case PlusUnaire:
					option = Operation.LOAD;// +rdest
					break;
				default:
					throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
			}
			//factorise , apres le switch
			//Prog.ajouter(Inst.creation2(option, opande, rdest));
			break;
		
	   	case 2:
	   			regs = allouer(1);
				//On défini l'operation
				option = operation_Arithmetique(a);
				//
				opande = int_to_Op(regs[0]);

				//On evalue a gauche ...
				evaluer_Expr_Arith(a.getFils1(), rdest);
				// ... puis a droite
				evaluer_Expr_Arith(a.getFils2(), opande);
				
				// Et on ajoute !
				//factorise , apres le switch
				//Prog.ajouter(Inst.creation2(option, opande, rdest));

				verifier_Arith_OV();		
			break;
	   	default:
	   		throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
	 }
	   Prog.ajouter(Inst.creation2(option, opande, rdest));
	   if(regs != null)
			//On libere/restaure le registre
			desallouer(regs);
	  
   }
   
   
	static private Operation operation_Arithmetique(Arbre a) throws Exception 
	{
		// + | - | * | div | / | % 
		switch (a.getNoeud()) {
			case Plus:
				return Operation.ADD;
			case Moins:
				return Operation.SUB;
			case Mult:
				return Operation.MUL;
			case DivReel:
			case Quotient:
				return Operation.DIV;
			case Reste:
				return Operation.MOD;
			default:
				throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
		}
	}
	
	static Operande operande_Arithmetique(Arbre a) throws Exception 
	{	
		// Interval | Real
		switch (a.getDecor().getType().getNature()) 
		{
			case Interval:
				return Operande.creationOpEntier(a.getEntier());
			case Real:
				return Operande.creationOpReel(a.getReel());
			default:
				throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
		}
	}
   
   
   // ############################ Instructions ################################
   
   static void coder_Inst(Arbre a) throws Exception {
	   Noeud noeud = a.getNoeud();
	   Inst inst,inst1 ;
	   Etiq etiq_Debut,etiq_Fin,etiq_Cond,etiq_Sinon;

	   
	   switch(noeud) {
		case Affect:
			coder_Affect(a);
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
				coder_Inst(a.getFils2()); 										// Faut ptet le sortir en fait
				break;
		
			default:
	   			throw new Exception("Decor incoherent ligne : " +a.getNumLigne());
		}
	}

   // True/False -> Lecture/Ecriture
   static private void coder_IO(Arbre a,boolean io) throws Exception
   {	 
	if (io)
	{
		Arbre f1 = a.getFils1();
	
		switch(f1.getDecor().getType().getNature()) 
		{
		
			// RINT            : R1 <- entier lu         CC : CP, OV vrai ssi débordement ou erreur de syntaxe
			case Interval:
				// On lit
				Prog.ajouter(Inst.creation0(Operation.RINT));
				// On vérifie
				verifier_Interval_OV(Operande.R1,f1.getDecor().getType());
				// On stocke
				// Mise dans R1 (f1)
				break;
		
			// RFLOAT          : R1 <- flottant lu       CC : CP, OV (cf RINT)
			case Real:
				Prog.ajouter(Inst.creation0(Operation.RFLOAT));
				// Mise dans R1 (f1)
				break;
			
		default:
			throw new Exception("Decor incoherent ligne : " + a.getNumLigne());
		}
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

			boolean to_restore;
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
					to_restore = used[1];
					//On libère R1 de force
					if (to_restore)
					{
						push(1);
					}
					evaluer_Expr_Arith(f2,Operande.R1);
					Prog.ajouter(Inst.creation0(Operation.WINT));
					if (to_restore)
					{
						pop(1);					
					}
					break;

				// WFLOAT          : ecriture du flottant V[R1]
				case Real:
					to_restore = used[1];
					//On libère R1 de force
					if (to_restore)
					{
						push(1);
					}
					evaluer_Expr_Arith(f2,Operande.R1);
					Prog.ajouter(Inst.creation0(Operation.WFLOAT));
					if (to_restore)
					{
						pop(1);					
					}
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

   // >>>     Des/Allocation
   // Top level : demande l'allocation de nb var. Retourne un tableau d'indice (au modulo pres) des registres allouees
   // R1 ne sera jamais allouee pour ne pas causer de conflit avec les instruction d'IO
   static private int[] allouer(int nb) throws Exception
   {
	   Prog.ajouterComment("Allocation : "+nb);
	   int to_return[] = new int[nb] , tmp;
	   for (int i = 0 ; i < nb ; i++)
	   {
		   try
		   {
			   tmp = allouer_Reg();
		   }
		   //Il n'y a plus de registres disponibles , on doit en save
		   /// Pour le moment on va encoder cette information simplement en ajoutant un offset -> int_to_Reg pour éviter les ennuis !
		   catch (Exception e)
		   {
			   last_saved++;
               //On n'alloue jamais R1
			   if (last_saved%16 == 1 ) last_saved++;

			   tmp = last_saved;
			   push(Registre.valueOf("R" + tmp%16));
		   }	   
		   
		   to_return[i] = tmp;
	   }
	   
	   return to_return;   
   }
   
   // Top level : fonction contraire de allouer. Utilise l'indice pour determiner si il faut restaurer ou non le registre.
   static private void desallouer(int[] ris) throws Exception
   {
	   Prog.ajouterComment("Desallocation : "+ris.length);
	   int tmp;
	   for (int i = ris.length-1 ; i >= 0 ; i--)
	   {
		   tmp = ris[i];
		   // Ce registre a ete sauver il faut donc le restaurer
		   if (tmp > 15)
		   {
			   pop(tmp%16);
			   //Doit tout le temps etre vrai , le cas faux marquerait une desynchronisation ou une desallocation dans le mauvais ordre
			   if (last_saved == tmp) 
			   {
				   last_saved--;
				   //On n'alloue jamais R1
				   if (last_saved%16 == 1 ) last_saved--;
			   }
		   }
		   else
		   {
			   free_Reg(tmp);
		   }
	   }
   }
   

   // NE PAS UTILISER
   static private int allouer_Reg() throws Exception
   {
	   int ri = 15;
	   while (ri >= 0)
	   {
		   if (!used[ri] && ri != 1)
		   {
			   used[ri] = true;
			   return ri;
	   		}
	   	 
		   else
			   ri--;
	   }
	   throw new Exception("Out of register!");
   }

   // NE PAS UTILISER
   static private void free_Reg(Operande ri) throws Exception
   {
	   if (!(ri.getNature().equals(NatureOperande.OpDirect))) throw new Exception("free_Reg called on a non-register operande!");
	   
	   int i = reg_to_Int(ri.getRegistre());
	   
	   if (i != -1) used[i] = false;
   }
   
   // NE PAS UTILISER
   static private void free_Reg(int ri)
   {
	   if (ri != -1) used[(ri%16)] = false;
   }
   
   // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

   
   // >>>    Conversion
   static private Registre int_to_Reg(int ri)
   {
	   int ri_mod = ri%16;
	   return Registre.valueOf("R"+ri_mod);
   }
   
   // Prends l'indice d'un registre (au modulo pres) et retourne l'opDirect associe.
   // int_to_Reg se charge du modulo
   static private Operande int_to_Op(int ri)
   {
	   return Operande.opDirect(int_to_Reg(ri));
   }
   
   static private int reg_to_Int(Registre reg) throws Exception
   {
	   int to_return;
	   switch(reg)
	   {   	   
	   	   case R0:
	   		    to_return = 0;
	   			break;
	   	   case R1:
	   		    to_return = 1;
	   			break;	
	   	   case R2:
	   		    to_return = 2;
	   			break;
	   	   case R3:
	   		    to_return = 3;
	   			break;	   			   			
	   	   case R4:
	   		    to_return = 4;	   			
	   			break;
	   	   case R5:
	   		   	to_return = 5;
	   			break;	
	   	   case R6:
	   		   	to_return = 6;
	   			break;
	   	   case R7:
	   		   	to_return = 7;
	   			break;			   
	   	   case R8:
	   		   	to_return = 8;
	   			break;
	   	   case R9:
	   		   	to_return = 9;
	   			break;	
	   	   case R10:
	   		   	to_return = 10;
	   			break;
	   	   case R11:
	   		   	to_return = 11;
	   			break;	   			   			
	   	   case R12:
	   		   	to_return = 12;
	   			break;
	   	   case R13:
	   		   	to_return = 13;
	   			break;	
	   	   case R14:
	   		   	to_return = 14;
	   			break;
	   	   case R15:
	   		   	to_return = 15;
	   			break;
	   		default:
	   			//LB ou GB
	   			throw new Exception("Pas d'équivalent numérique à LB/RB");
	   }
	   return to_return;
   }
   
   
   // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
   
   
   
   // >>>    Pile
   static private void push(int ri) throws Exception
   {
	   if (used[ri])
	   {
		   Prog.ajouter(Inst.creation1(Operation.PUSH, Operande.opDirect(Registre.valueOf("R"+ri))));
		   
		   pointeur_pile++;
		   
		   verifier_Pile_OV();
		   
		   free_Reg(ri);
	   }
   }
   
   // Un alias pour utiliser directemet l'indice
   static private void push(Registre ri) throws Exception
   {
	   int i = reg_to_Int(ri);
	   push(i);
   }
   
   static private void pop(int ri) throws Exception
   {
	   if (pointeur_pile > 0)
	   {
		   Prog.ajouter(Inst.creation1(Operation.POP, Operande.opDirect(Registre.valueOf("R"+ri))));
		   
		   pointeur_pile--;
		   
		   used[ri] = true;
	   }
   }
   
   // Un alias pour utiliser directemet l'indice
   static private void pop(Registre ri) throws Exception
   {
	   int i = reg_to_Int(ri);
	   pop(i);
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
	   			Type tab = def.getType();
	   			Type indices;
	   			int inf;
	   			int sup;
	   			
	   			//Pour gérer les tableaux de tableaux ..
	   			while(tab.getNature() == NatureType.Array) {
	   				indices = tab.getIndice();
	   				inf = indices.getBorneInf();
	   				sup = indices.getBorneSup();
	   				
		   			if (inf < sup)
		   			{
		   				def.setOperande(Operande.creationOpIndirect(pointeur_pile, Registre.GB));
		   				pointeur_pile += (1+sup-inf);
		   			}
		   			else
		   			{
		   				System.out.println("/!\\ Tableau de taille nulle ligne : " + lident.getNumLigne());
		   			}
		   			
		   			//Soit on a un tableau de tableau et donc on a un Array sinon autre chose.
		   			tab = tab.getElement();
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
  
   // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
   
   
   // ############################ Debordements ################################
   
   // A placer après chaque ajout en pile
   static private void verifier_Pile_OV() 
   {
	Prog.ajouter(Inst.creation1(Operation.TSTO, Operande.creationOpEntier(pointeur_pile)));
	Prog.ajouter(Inst.creation1(Operation.BOV, Operande.creationOpEtiq(Prog.L_Etiq_Pile_Pleine)));
   }
   
   // A placer après chaque operateur arithmetique risquant de faire un overflow
   static private void verifier_Arith_OV()
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
   




   private static Operande operande_Ident (Arbre a) throws Exception
   {
	   Defn def = a.getDecor().getDefn();
	   Operande to_return;
	   switch (a.getChaine()) 
	   {
			case "true":
				to_return = def.getNature().equals(NatureDefn.ConstBoolean) ? Operande.creationOpEntier(1) : null;
				break;
			case "false":
				to_return = def.getNature().equals(NatureDefn.ConstBoolean) ? Operande.creationOpEntier(0) : null;
				break;
			case "max_int":
				to_return = def.getNature().equals(NatureDefn.ConstInteger) ? Operande.creationOpEntier(def.getValeurInteger()) : null;	
				break;
			default:
				to_return = def.getOperande();
	   }
	   if (to_return == null) throw new Exception("NatureDefn incohérent ligne : "+a.getNumLigne());
	   else return to_return;
}

}

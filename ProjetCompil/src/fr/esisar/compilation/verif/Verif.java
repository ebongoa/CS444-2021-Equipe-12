package fr.esisar.compilation.verif;

import fr.esisar.compilation.global.src.*;
 
/**
 * Cette classe permet de réaliser la vérification et la décoration 
 * de l'arbre abstrait d'un programme.
 */
public class Verif {

   private Environ env; // L'environnement des identificateurs

   /**
    * Constructeur.
    */
   public Verif() {
      env = new Environ();
   }

   /**
    * Vérifie les contraintes contextuelles du programme correspondant à 
    * l'arbre abstrait a, qui est décoré et enrichi. 
    * Les contraintes contextuelles sont décrites 
    * dans Context.txt.
    * En cas d'erreur contextuelle, un message d'erreur est affiché et 
    * l'exception ErreurVerif est levée.
    */
   public void verifierDecorer(Arbre a) throws ErreurVerif {
      verifier_PROGRAMME(a);
   }

   /**
    * Initialisation de l'environnement avec les identificateurs prédéfinis.
    *
    * Les identificateurs suivants sont prédéfinis dans le langage JCas :

  		integer, boolean, real (identificateurs de type)
   		true, false, max_int   (identificateurs de constante)
    */
   private void initialiserEnv() {
      Defn def;
      // Integer
      def = Defn.creationType(Type.Integer);
      def.setGenre(Genre.PredefInteger);
      env.enrichir("integer", def);
      
      // Boolean
      def = Defn.creationType(Type.Boolean);
      def.setGenre(Genre.PredefBoolean);
      env.enrichir("boolean", def);
      
      // Real
      def = Defn.creationType(Type.Real);
      def.setGenre(Genre.PredefReal);
      env.enrichir("real", def);
      
      // True
      def = Defn.creationConstBoolean(true);
      def.setGenre(Genre.PredefTrue);
      env.enrichir("true", def);
      
      // False
      def = Defn.creationConstBoolean(false);
      def.setGenre(Genre.PredefFalse);
      env.enrichir("false", def);
      
      // Max_int
      def = Defn.creationConstInteger(java.lang.Integer.MAX_VALUE);
      def.setGenre(Genre.PredefMaxInt);
      env.enrichir("max_int", def);

      
   }

   /**************************************************************************
    * PROGRAMME
    * PROGRAMME 	-> Noeud.Programme(LISTE_DECL, LISTE_INST) 
    **************************************************************************/
   private void verifier_PROGRAMME(Arbre a) throws ErreurVerif 
   {
      initialiserEnv();
      verifier_LISTE_DECL(a.getFils1());
      verifier_LISTE_INST(a.getFils2());
   }

   /**************************************************************************
    * LISTE_DECL
    * LISTE_DECL 	-> Noeud.ListeDecl(LISTE_DECL, DECL) 
					|  Noeud.Vide 
    **************************************************************************/
   private void verifier_LISTE_DECL(Arbre a) throws ErreurVerif 
   {
	        switch(a.getNoeud()) {
	            case Vide:
	                break;
	            case ListeDecl:
	                verifier_LISTE_DECL(a.getFils1());
	                verifier_DECL(a.getFils2());
	                break;
	            default:
	                throw new ErreurInterneVerif("Appel incorrect a verifier_LISTE_DECL ligne " + a.getNumLigne());
	        }
   }
   
   /**************************************************************************
    * DECL	 	-> Noeud.Decl(LISTE_IDENT, TYPE)
    **************************************************************************/
	private void verifier_DECL(Arbre a) throws ErreurVerif 
	{
        if(a.getNoeud().equals(Noeud.Decl)) 
        {
            Type type = verifier_TYPE(a.getFils2());
            verifier_LISTE_IDENT(a.getFils1(), type);
        }
        else throw new ErreurInterneVerif("Appel incorrect a verifier_DECL ligne " + a.getNumLigne());


	}
   
	/**************************************************************************
    * LISTE_IDENT 	-> Noeud.ListeIdent(LISTE_IDENT, IDENT) 
	*				|  Noeud.Vide
    **************************************************************************/
   private void verifier_LISTE_IDENT(Arbre a, Type t) throws ErreurVerif 
   {
       switch(a.getNoeud()) {
           case ListeIdent:
               verifier_LISTE_IDENT(a.getFils1(), t);
               verifier_IDENT(a.getFils2(), t);
               break;
           case Vide:
               break;
           default:
        	   throw new ErreurInterneVerif("Appel incorrect a verifier_LISTE_IDENT ligne " + a.getNumLigne());
       }
   }
   
   /**************************************************************************
   *IDENT 		-> Noeud.Ident                   -- attribut de type Chaine
   **************************************************************************/
   private Decor verifier_IDENT(Arbre a) throws ErreurVerif
   { 
	   if(a.getNoeud().equals(Noeud.Ident)) 
	   {
		   Defn def = env.chercher(a.getChaine()); 
       
		   if(def == null) 
		   {
			   ErreurContext.ErreurIdent.leverErreurContext(a.getChaine(), a.getNumLigne());
			   //Ident non défini
		   }       
		   return new Decor(def, def.getType());
	   }
       else throw new ErreurInterneVerif("Appel incorrect a verifier_IDENT ligne " + a.getNumLigne());
   }
   
   private void verifier_IDENT(Arbre a, Type t1) throws ErreurVerif 
   {
	   String chaine = a.getChaine();
	   if(!a.getNoeud().equals(Noeud.Ident)) throw new ErreurInterneVerif("Appel incorrect a verifier_IDENT ligne " + a.getNumLigne());
       else if(env.chercher(chaine) != null) 
       {
       	switch(env.chercher(chaine).getNature()) 
       	{
       		case ConstBoolean:
			case ConstInteger:
			case Type:
			case Var:
				ErreurContext.ErreurDejaDefini.leverErreurContext(a.getChaine(), a.getNumLigne());
				//l'identifiant existe déjà dans l'environnement
			default:
				throw new ErreurInterneVerif("Appel incorrect a verifier_IDENT ligne " + a.getNumLigne());
       	}
       } 
       else 
       {
    	   Defn def = Defn.creationVar(t1);
           a.setDecor(new Decor(def));
           env.enrichir(a.getChaine(), def);
       }
   }
   
   /**************************************************************************
    * TYPE		-> IDENT
    *			|  TYPE_INTERVALLE
	*			|  Noeud.Tableau(TYPE_INTERVALLE, TYPE) 
    **************************************************************************/
   	private Type verifier_TYPE(Arbre a) throws ErreurVerif 
   	{
       switch(a.getNoeud()) 
       {
           case Ident:
               Defn def = env.chercher(a.getChaine());
               if(def == null || !def.getNature().equals(NatureDefn.Type)) //Ne correspond a rien du tout
               {
               	ErreurContext.ErreurTypeInconnu.leverErreurContext(a.getChaine(), a.getNumLigne());
               	//Cet ident n'existe pas ou ne designe pas un type
               } 
               else //L'ident existe et est un type
               {
               	   a.setDecor(new Decor(def));
                   return def.getType();
               }
               
           case Intervalle:
               return verifier_TYPE_INTERVALLE(a);
           
           case Tableau:
        	   Type t1 = verifier_TYPE_INTERVALLE(a.getFils1());
            
        	   if(t1.getNature().equals(NatureType.Interval)) 
        		   return Type.creationArray(t1, verifier_TYPE(a.getFils2()));
        	   else
        		   ErreurContext.ErreurIndexInterval.leverErreurContext(t1.getNature().toString(), a.getNumLigne());
        	   	   //Un indice est de type Interval
           default:
				throw new ErreurInterneVerif("Appel incorrect a verifier_TYPE ligne " + a.getNumLigne());
       }
   }
   
   /**************************************************************************
    * TYPE_INTERVALLE -> Noeud.Intervalle(EXP_CONST, EXP_CONST)
    **************************************************************************/
    private Type verifier_TYPE_INTERVALLE(Arbre a) throws ErreurVerif 
    {
        if(a.getNoeud().equals(Noeud.Intervalle)) 
        {  
        	Arbre f1 = a.getFils1();
        	Arbre f2 = a.getFils2();

        	int borne_inf = verifier_EXP_CONST(f1);
        	int borne_sup = verifier_EXP_CONST(f2);

        	Type t1 = f1.getDecor().getType();
        	Type t2 = f2.getDecor().getType();
        	
        	
        	//Verifier que les noeuds sont de type interval
        	if(!(t1.equals(Type.Integer))) 
        	{
        		ErreurContext.ErreurIntervalInteger.leverErreurContext(t1.toString(), a.getNumLigne());
        		//La borne n'est pas un entier
        	}
        	else if(!(t2.equals(Type.Integer))) 
        	{
        		ErreurContext.ErreurIntervalInteger.leverErreurContext(t2.toString(), a.getNumLigne());
        		//La borne n'est pas un entier
        	}
        
        	return Type.creationInterval(borne_inf, borne_sup);
        }
        else throw new ErreurInterneVerif("Appel incorrect a verifier_TYPE_INTERVALLE ligne " + a.getNumLigne());
    }
   
   /**************************************************************************
    * EXP_CONST 	-> IDENT
                	|  Noeud.Entier                  -- attribut de type Entier
					|  Noeud.PlusUnaire(EXP_CONST)
					|  Noeud.MoinsUnaire(EXP_CONST)
	N'est appelé que pour verifier un interval i.e on attend une ConstInteger
    **************************************************************************/
    private int verifier_EXP_CONST(Arbre a) throws ErreurVerif 
	{
    	int value;
    	Arbre f1;

        switch(a.getNoeud())
        {
            case Ident:
            	Defn def = env.chercher(a.getChaine());
                if(def == null) 
                {
                	ErreurContext.ErreurIdent.leverErreurContext(a.getChaine(), a.getNumLigne());
                 	//Cet ident n'existe pas
                } 
                else if(!(def.getNature().equals(NatureDefn.ConstInteger))) 
                {
                	ErreurContext.ErreurConstInteger.leverErreurContext(def.getNature().toString(), a.getNumLigne());
                	//On ne fait pas d'intervale avec une constante booleene
                }
                
                a.setDecor(new Decor(def, def.getType()));
                return def.getValeurInteger();
        
            case Entier:
            	a.setDecor(new Decor(Type.Integer));
            	return a.getEntier();
            
            case PlusUnaire:
            	f1 = a.getFils1();
            	value = verifier_EXP_CONST(f1);
            	a.setDecor(f1.getDecor());
            	return value;
            
            case MoinsUnaire:
            	f1 = a.getFils1();
            	value = verifier_EXP_CONST(f1);
            	a.setDecor(f1.getDecor());
            	return -value;
            
            default:
                throw new ErreurInterneVerif("Appel incorrect a verifier_EXP_CONST ligne " + a.getNumLigne());
        }
    }
   
   /**************************************************************************
    * LISTE_INST 	-> Noeud.Vide 
					|  Noeud.ListeInst(LISTE_INST, INST) 
    **************************************************************************/
    private void verifier_LISTE_INST(Arbre a) throws ErreurVerif
    {
        
        switch(a.getNoeud())
        {
            case Vide:
                break;
                
            case ListeInst:
                verifier_LISTE_INST(a.getFils1());
                verifier_INST(a.getFils2());
                break;
            default:
            	 throw new ErreurInterneVerif("Appel incorrect a verifier_LISTE_INST ligne " + a.getNumLigne());
        }
           
    }

   /**************************************************************************
    * INST		-> Noeud.Nop
				|  Noeud.Affect(PLACE, EXP) 
				|  Noeud.Pour(PAS, LISTE_INST) 
				|  Noeud.TantQue(EXP, LISTE_INST) 
				|  Noeud.Si(EXP, LISTE_INST, LISTE_INST) 
				|  Noeud.Lecture(PLACE)
				|  Noeud.Ecriture(LISTE_EXP) 
                |  Noeud.Ligne
    **************************************************************************/
    private void verifier_INST(Arbre a) throws ErreurVerif
    {
    	Type t1;
    	Type t2;
    	
    	switch(a.getNoeud())
    	{
    		case Nop:
    			break;
    		
    		case Affect:
   			
                verifier_PLACE(a.getFils1());
                verifier_EXP(a.getFils2());
                
                t1 = a.getFils1().getDecor().getType();
                t2 = a.getFils2().getDecor().getType();
                   
                //. <place> et <expression> de type Array, les types des indices étant identiques (plus précisement, de type Type.Interval, avec les mêmes 
                // bornes), et les types des éléments compatibles pour l'affectation.
                if (t1.getNature().equals(NatureType.Array))
                {
                	//On test les bornes
                	if (t1.getIndice().getBorneInf() == t2.getIndice().getBorneInf())
                	{
                		if (t1.getIndice().getBorneSup() == t2.getIndice().getBorneSup())
                		{
               			
                			//De même Type
                			if (! (t1.equals(t2)))
                			{   
                				System.out.println(t1.toString()+";"+t2.toString());
                				ErreurContext.ErreurAffect.leverErreurContext("Les indices devraient être de même type.Obtenu : "+ t1.getElement().toString() +" et "+t2.getElement().toString(), a.getNumLigne());
                			}             			
                		}
                		else
                		{
                    		ErreurContext.ErreurAffect.leverErreurContext("Bornes sup différentes : "+ t1.getIndice().getBorneSup() +" et "+t2.getIndice().getBorneSup(), a.getNumLigne());     
                			//Pas les même bornes sup
                		}
                	}
                	else
                	{
                		ErreurContext.ErreurAffect.leverErreurContext("Bornes inf différentes : "+ t1.getIndice().getBorneInf() +" et "+t2.getIndice().getBorneInf(), a.getNumLigne());     
                		//Pas les même bornes inf
                	}
                }
                
                // . <place> et <expression> de type Type.Interval (pas forcément avec les mêmes bornes) ;                
                else if (t1.getNature().equals(NatureType.Interval))
                {
                	if (!(t2.getNature().equals(NatureType.Interval)))
                	{
                		ErreurContext.ErreurInterval.leverErreurContext(t2.toString(), a.getNumLigne());   
                	}
                }
                
                //. <place> et <expression> de type Type.Real ;
                //. <place> de type Type.Real et <expression> de type Type.Interval ;
                else if (t1.equals(Type.Real))
                {
                	if (t2.equals(Type.Real))
                	{
                		;//compatible tel quel
                	}
                	else if(t2.getNature().equals(NatureType.Interval))
                	{
                		//On a un cast a faire
            			Arbre cast = Arbre.creation1(Noeud.Conversion, a.getFils2(), a.getNumLigne());
            			cast.setDecor(new Decor(Type.Real));
            	   		a.setFils2(cast);
                	}
                	else
                	{
                		ErreurContext.ErreurAffect.leverErreurContext("Cast impossible de "+ t2.toString() +" à "+t1.toString(), a.getNumLigne());                         	                                        			
                	}
                }
                
                //. <place> et <expression> de type Type.Boolean ; 
                else if (t1.equals(Type.Boolean))
                {
                	if (t2.equals(Type.Boolean))
                	{
                		;
                	}
                	else
                	{
                		ErreurContext.ErreurBool.leverErreurContext(t2.toString(), a.getNumLigne());         
                	}
                }
                else
                {
            		ErreurContext.ErreurAffect.leverErreurContext("Array ou Real ou Interval ou Boolean attendu. Obtenu : "+ t1.toString(), a.getNumLigne());
                }
                
                a.setDecor(new Decor(t2));               
    			break;
    			
            case Pour:
                verifier_PAS(a.getFils1());
                verifier_LISTE_INST(a.getFils2());
                break;
            
            case TantQue:           	
                verifier_EXP(a.getFils1());
                verifier_LISTE_INST(a.getFils2());
                
            	t1 = a.getFils1().getDecor().getType();
                
                //On attend de l'expression qu'elle retourne un boolean
                if(!(t1.equals(Type.Boolean))) 
                {
                	ErreurContext.ErreurBool.leverErreurContext(t1.toString(), a.getNumLigne());
                } 
                else
                {
                	break;
                }
                
            case Si:
                verifier_EXP(a.getFils1());
                verifier_LISTE_INST(a.getFils2());
                verifier_LISTE_INST(a.getFils3());
                
            	t1 = a.getFils1().getDecor().getType();
                
              //On attend de l'expression qu'elle retourne un boolean
                if(!(t1.equals(Type.Boolean)))  
                {
                	ErreurContext.ErreurBool.leverErreurContext(t1.toString(), a.getNumLigne());
                } 
                else
                {
                	break;
                }
            //Instruction read : la place doit etre de type Type.Interval ou Type.Real.
            case Lecture:
                verifier_PLACE(a.getFils1());
                
            	t1 = a.getFils1().getDecor().getType();
                
                if( !(t1.getNature().equals(NatureType.Interval)) && !(t1.equals(Type.Real)))
                {
                	ErreurContext.ErreurLecture.leverErreurContext(t1.getNature().toString() + "/" + t1.toString(), a.getNumLigne());
                } 
                else 
                {
                	break;
                }        
             
            // - Instruction write : les expressions doivent être de type Type.Interval, Type.Real ou Type.String.
            case Ecriture:
                verifier_LISTE_EXP(a.getFils1());
                
                t1 = a.getFils1().getDecor().getType();
                
                if( !(t1.getNature().equals(NatureType.Interval)) && !(t1.equals(Type.Real)) && !(t1.equals(Type.String)))
                {
                	ErreurContext.ErreurEcriture.leverErreurContext(t1.getNature().toString() + "/" + t1.toString(), a.getNumLigne());
                } 
                else 
                {
                	break;
                }              
            
            case Ligne:
                break;
                
    		default:
    			throw new ErreurInterneVerif("Appel incorrect a verifier_INST ligne " + a.getNumLigne());         
    	}
    	
    }
   
   /**************************************************************************
    * PAS 		-> Noeud.Increment(IDENT, EXP, EXP) 
				|  Noeud.Decrement(IDENT, EXP, EXP) 	
    **************************************************************************/
    private void verifier_PAS(Arbre a) throws ErreurVerif 
    {   
    	Arbre f1;
    	Arbre f2;
    	Arbre f3;
    	
        switch(a.getNoeud()) 
        {         
            case Increment:
            case Decrement:
            	f1 = a.getFils1();
            	f2 = a.getFils2();
            	f3 = a.getFils3();
                
            	Decor d = verifier_IDENT(f1);
                verifier_EXP(f2);
                verifier_EXP(f3);
                
                Type t1 = d.getDefn().getType();
                Type t2 = f2.getDecor().getType();
                Type t3 = f3.getDecor().getType();

                
                if(!(t1.getNature().equals(NatureType.Interval))) 
                {
                	ErreurContext.ErreurInterval.leverErreurContext(t1.getNature().toString(), a.getNumLigne());
                	//On attends un interval
                } 
                else if(!(t2.getNature().equals(NatureType.Interval))) 
                {
            		ErreurContext.ErreurInterval.leverErreurContext(t2.getNature().toString(), a.getNumLigne());
            		//On attends un interval
            	} 
                else if(!(t3.getNature().equals(NatureType.Interval))) 
                {
            		ErreurContext.ErreurInterval.leverErreurContext(t3.getNature().toString(), a.getNumLigne());
            		//On attends un interval
            	} 
                else 
                {
            		break;
            	} 
            default:
                throw new ErreurInterneVerif("Appel incorrect a verifier_PAS ligne " + a.getNumLigne());         
        }         
    }
   
   /**************************************************************************
    * PLACE 		-> IDENT | IDENT est un identificateur de variable  
					|  Noeud.Index(PLACE, EXP)     
    **************************************************************************/
    private void verifier_PLACE(Arbre a) throws ErreurVerif 
    {    
        switch(a.getNoeud())
        {    
            case Ident:
            	Decor d = verifier_IDENT(a);
            	if(d
            			.getDefn()
            			.getNature()
            			.equals(NatureDefn.Var)) 
            	{  		
            		a.setDecor(d);
                    break;   		
            	} 
            	else 
            	{
            		ErreurContext.ErreurIdentVar.leverErreurContext(a.getChaine(), a.getNumLigne());
            		//L'ident devrait être un Var
            	}
            case Index:
            	Arbre f1 = a.getFils1();
            	Arbre f2 = a.getFils2();
            	
            	verifier_PLACE(f1);
                verifier_EXP(f2);
                
                Type t1 = f1.getDecor().getType();
                Type t2 = f2.getDecor().getType();
                
                if (!(t1.getNature().equals(NatureType.Array)))
                {
                	ErreurContext.ErreurIndexationTableau.leverErreurContext(t1.getNature().toString(), a.getNumLigne());
                	//On ne peux indexer qu'un tableau               	
                }                             
                else if( !(t2.getNature().equals(NatureType.Interval))) 
                {
                	ErreurContext.ErreurIndexInterval.leverErreurContext(t2.getNature().toString(), a.getNumLigne());
                	//Un indice est de type Interval
                } 
                else 
                {
                	a.setDecor(new Decor(t1.getElement()));
                    break;
                }
            default:
                throw new ErreurInterneVerif("Appel incorrect a verifier_PLACE ligne " + a.getNumLigne());
           
        }
    }
   
   /**************************************************************************
    * LISTE_EXP   	-> Noeud.Vide
					|  Noeud.ListeExp(LISTE_EXP, EXP) 
 * @throws ErreurVerif 
    **************************************************************************/
   private void verifier_LISTE_EXP(Arbre a) throws ErreurVerif
   {
       
     switch(a.getNoeud()) 
     {   
         case Vide:
             break;
             
         case ListeExp:
        	 Arbre f2 = a.getFils2();
             verifier_LISTE_EXP(a.getFils1());
             verifier_EXP(f2);
             
             a.setDecor(f2.getDecor());
             break;
                
             default:
                 throw new ErreurInterneVerif("Appel incorrect a verifier_LISTE_EXP ligne " + a.getNumLigne());
        
     }
   }
   
   /**************************************************************************
    * EXP 		-> Noeud.Et(EXP, EXP) 
  				|  Noeud.Ou(EXP, EXP) 
				|  Noeud.Egal(EXP, EXP)
				|  Noeud.InfEgal(EXP, EXP)
				|  Noeud.SupEgal(EXP, EXP)
				|  Noeud.NonEgal(EXP, EXP)
				|  Noeud.Inf(EXP, EXP)
				|  Noeud.Sup(EXP, EXP)
				|  Noeud.Plus(EXP, EXP)
				|  Noeud.Moins(EXP, EXP)
				|  Noeud.Mult(EXP, EXP)
				|  Noeud.DivReel(EXP, EXP)
				|  Noeud.Reste(EXP, EXP)
				|  Noeud.Quotient(EXP, EXP)
				|  Noeud.Index(PLACE, EXP) 
				|  Noeud.PlusUnaire(EXP) 
				|  Noeud.MoinsUnaire(EXP) 
				|  Noeud.Non(EXP)
				|  Noeud.Conversion(EXP) 
				|  Noeud.Entier                  -- attribut de type Entier
				|  Noeud.Reel                    -- attribut de type Reel
				|  Noeud.Chaine                  -- attribut de type Chaine
				|  IDENT            
    **************************************************************************/  
   private void verifier_EXP(Arbre a) throws ErreurVerif
   {
	   Type t1 = null,t2 = null; //On aura maximum 2 fils
	   Arbre f1 = null,f2 = null; //On aura maximum 2 fils  
	   
	   switch (a.getNoeud())
	   {
	   //Type.Boolean -> Type.Boolean
	   case Non:
		   f1 = a.getFils1();
		   verifier_EXP(f1);
		   t1 = f1.getDecor().getType();
		   
		   if(t1.equals(Type.Boolean)) 
		   {
			   a.setDecor(new Decor(Type.Boolean));
			   break;
		   } 
		   else ErreurContext.ErreurOperateurUnaireBool.leverErreurContext(t1.toString(), a.getNumLigne());
    	//Operateur Unaire attend un bool
		   
	   //Type.Boolean, Type.Boolean -> Type.Boolean   
	   case Et:
	   case Ou:
		   f1 = a.getFils1();
		   f2 = a.getFils2();
		   
      		verifier_EXP(f1);
      		verifier_EXP(f2);
      		
 		   t1 = f1.getDecor().getType();
		   t2 = f2.getDecor().getType();
 		   
		   if (t1.equals(t2) && t1.equals(Type.Boolean) )
		   {
			   a.setDecor(new Decor(Type.Boolean));
			   break; 
		   }
		   else ErreurContext.ErreurOperateurBinaireBool.leverErreurContext(t1.toString() + " , " + t2.toString(), a.getNumLigne());
		   //Operateur Binaire attend deux bool
		   
	   //Type.Interval, Type.Interval -> Type.Boolean
  	   //Type.Interval, Type.Real     -> Type.Boolean
  	   //Type.Real,     Type.Interval -> Type.Boolean
  	   //Type.Real,     Type.Real     -> Type.Boolean
	   case Egal:
	   case Inf:
	   case Sup:
	   case NonEgal:
	   case InfEgal:
	   case SupEgal:  
		   f1 = a.getFils1();
		   f2 = a.getFils2();
		   
      		verifier_EXP(f1);
      		verifier_EXP(f2);
      		
 		   t1 = f1.getDecor().getType();
		   t2 = f2.getDecor().getType();
		   
		   if ( (t1.getNature().equals(NatureType.Interval) || t1.equals(Type.Real)) && (t2.getNature().equals(NatureType.Interval) || t2.equals(Type.Real))) 		   
		   {
			    Arbre cast;
			    if(!t1.equals(Type.Real))
			    {
			    	cast = Arbre.creation1(Noeud.Conversion, f1, a.getNumLigne());
			    	cast.setDecor(new Decor(Type.Real));
              		a.setFils1(cast);
			    }
			    if(!t2.equals(Type.Real))
			    {
			    	cast = Arbre.creation1(Noeud.Conversion, f2, a.getNumLigne());
			    	cast.setDecor(new Decor(Type.Real));
              		a.setFils2(cast);
			    }
       			a.setDecor(new Decor(Type.Boolean));
           		break;
		   }   
      	   else ErreurContext.ErreurOperateurBinaireIntervalOuReel.leverErreurContext(t1.getNature().toString()+"/"+t1.toString() + " , " + t2.getNature().toString()+"/"+t2.toString(), a.getNumLigne());
		   //Operateur binaire attends deux Interval ou deux Reel ou un de chaque.
		
	   //Type.Interval -> Type.Integer
  	   //Type.Real     -> Type.Real 
	   case PlusUnaire:
	   case MoinsUnaire:
 		   f1 = a.getFils1();
     		verifier_EXP(f1);
     		
 		   t1 = f1.getDecor().getType();
 		   
		   if ( t1.getNature().equals(NatureType.Interval) ) a.setDecor(new Decor(Type.Integer));
		   else if (t1.equals(Type.Real))        a.setDecor(new Decor(Type.Real));
		   else ErreurContext.ErreurOperateurUnaireIntervalOuReel.leverErreurContext(t1.getNature().toString()+"/"+t1.toString(), a.getNumLigne());
		   break;
		   //Operateur Unaire attends un Interval ou un reel
		   
	   //Type.Interval, Type.Interval -> Type.Integer
  	   //Type.Interval, Type.Real     -> Type.Real
  	   //Type.Real,     Type.Interval -> Type.Real 		
  	   //Type.Real,     Type.Real     -> Type.Real	   
	   case Plus:
	   case Moins:
	   case Mult:
		   f1 = a.getFils1();
		   f2 = a.getFils2();
		   
      		verifier_EXP(f1);
      		verifier_EXP(f2);
      		
 		   t1 = f1.getDecor().getType();
		   t2 = f2.getDecor().getType();
		   
		   if ( (t1.getNature().equals(NatureType.Interval) || t1.equals(Type.Real)) && (t2.getNature().equals(NatureType.Interval) || t2.equals(Type.Real))) 		   
		   {
		  	    if (t1.getNature().equals(t2.getNature()) && t1.getNature().equals(NatureType.Interval)) 
		  	    {
		  	    	a.setDecor(new Decor(Type.Integer));
		  	    }
		  	    else //L'un des deux est un interval et l'autre un reel , on ajoute donc un noeud conversion;
		  	    {
				    Arbre cast;
				    if(!t1.equals(Type.Real))
				    {
				    	cast = Arbre.creation1(Noeud.Conversion, f1, a.getNumLigne());
				    	cast.setDecor(new Decor(Type.Real));
	               		a.setFils1(cast);
				    }
				    if(!t2.equals(Type.Real))
				    {
				    	cast = Arbre.creation1(Noeud.Conversion, f2, a.getNumLigne());
				    	cast.setDecor(new Decor(Type.Real));
	               		a.setFils2(cast);
				    }	
		  	    	a.setDecor(new Decor(Type.Real));
		  	    }
			    
			    break;
			   }   
	      	   else ErreurContext.ErreurOperateurBinaireIntervalOuReel.leverErreurContext(t1.getNature().toString()+"/"+t1.toString() + " , " + t2.getNature().toString()+"/"+t2.toString(), a.getNumLigne());
			   //Operateur binaire attends deux Interval ou deux Reel ou un de chaque.
	
	   //Type.Interval, Type.Interval -> Type.Integer
	   case Quotient:
	   case Reste:
		   f1 = a.getFils1();
		   f2 = a.getFils2();
		   
      		verifier_EXP(f1);
      		verifier_EXP(f2);
      		
 		   t1 = f1.getDecor().getType();
		   t2 = f2.getDecor().getType();
     		
     		if (t1.getNature().equals(t2.getNature()) && t1.getNature().equals(NatureType.Interval) )
     		{
     			a.setDecor(new Decor(Type.Integer));
     			break;
     		}
     		else ErreurContext.ErreurOperateurBinaireInterval.leverErreurContext(t1.getNature().toString() + " , " + t2.getNature().toString(), a.getNumLigne());
     		//Operateur Binaire attends deux Interval
		
	   //Type.Interval, Type.Interval -> Type.Real 
	   //Type.Interval, Type.Real     -> Type.Real
	   //Type.Real,     Type.Interval -> Type.Real
	   //Type.Real,     Type.Real     -> Type.Real	   
	   case DivReel:
		   f1 = a.getFils1();
		   f2 = a.getFils2();
		   
      		verifier_EXP(f1);
      		verifier_EXP(f2);
      		
 		   t1 = f1.getDecor().getType();
		   t2 = f2.getDecor().getType();
		   
		   if ( (t1.getNature().equals(NatureType.Interval) || t1.equals(Type.Real)) && (t2.getNature().equals(NatureType.Interval) || t2.equals(Type.Real))) 		   
		   {
			    Arbre cast;
			    if(!t1.equals(Type.Real))
			    {
			    	cast = Arbre.creation1(Noeud.Conversion, f1, a.getNumLigne());
			    	cast.setDecor(new Decor(Type.Real));
               		a.setFils1(cast);
			    }
			    if(!t2.equals(Type.Real))
			    {
			    	cast = Arbre.creation1(Noeud.Conversion, f2, a.getNumLigne());
			    	cast.setDecor(new Decor(Type.Real));
               		a.setFils2(cast);
			    }
			    a.setDecor(new Decor(Type.Real));

           		break;
		   }   
      	   else ErreurContext.ErreurOperateurBinaireIntervalOuReel.leverErreurContext(t1.getNature().toString()+"/"+t1.toString() + " , " + t2.getNature().toString()+"/"+t2.toString(), a.getNumLigne());
		  //Operateur binaire attend deux Interval ou deux Reel ou un de chaque.
		   
	   //Array(Type.Interval, <type>), Type.Interval -> <type>	   
       case Index:
		   f1 = a.getFils1();
		   f2 = a.getFils2();
		   
      		verifier_PLACE(f1);
      		verifier_EXP(f2);
      		
 		   t1 = f1.getDecor().getType();
		   t2 = f2.getDecor().getType();
           
           if(!(t1.getNature().equals(NatureType.Array))) ErreurContext.ErreurIndexationTableau.leverErreurContext(t1.getNature().toString(), a.getNumLigne());
       		//On ne peut indexer qu'un tableau
            else if (!(t2.getNature().equals(NatureType.Interval)))	ErreurContext.ErreurIndexInterval.leverErreurContext(t2.getNature().toString(), a.getNumLigne()); 
	   		//L'indexation se fait par intervalle
            else a.setDecor(new Decor(a.getFils1().getDecor().getType().getElement()));
            
	   		break;
	   
       case Entier:
           a.setDecor(new Decor(Type.Integer));
           break;

       case Reel:
    	   a.setDecor(new Decor(Type.Real));
       	   break;

       case Chaine:
    	   a.setDecor(new Decor(Type.String));
    	   break;
	   		
	   case Ident:
		    Decor dec = verifier_IDENT(a);
      		NatureDefn nature = dec
      				.getDefn()
      				.getNature(); 
      		if (nature.equals(NatureDefn.Type)) ErreurContext.ErreurIdentVarOuConst.leverErreurContext(a.getChaine(), a.getNumLigne());
      		//Identifiant de Variable ou de Constante attendue
      		else a.setDecor(dec);
               break;
       	
       
       default:
           throw new ErreurInterneVerif("Appel incorrect a verifier_EXP ligne " + a.getNumLigne());
	   }

   }
   
   
   // ------------------------------------------------------------------------
   // COMPLETER les operations de vérifications et de décoration pour toutes 
   // les constructions d'arbres
   // ------------------------------------------------------------------------
 
   
}

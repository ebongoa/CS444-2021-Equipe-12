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
   private void verifier_PROGRAMME(Arbre a) throws ErreurVerif {
      initialiserEnv();
      verifier_LISTE_DECL(a.getFils1());
      verifier_LISTE_INST(a.getFils2());
   }

   /**************************************************************************
    * LISTE_DECL
    * LISTE_DECL 	-> Noeud.ListeDecl(LISTE_DECL, DECL) 
					|  Noeud.Vide 
    **************************************************************************/
   private void verifier_LISTE_DECL(Arbre a) throws ErreurVerif {
      // A COMPLETER
   }
   
   /**************************************************************************
    * DECL	 	-> Noeud.Decl(LISTE_IDENT, TYPE)
    **************************************************************************/
// A COMPLETER
   
   /**************************************************************************
    * LISTE_IDENT 	-> Noeud.ListeIdent(LISTE_IDENT, IDENT) 
	*				|  Noeud.Vide
    **************************************************************************/
// A COMPLETER
   private Type verifier_LISTE_IDENT(Arbre a) throws ErreurVerif
   { return null;}
   
   /**************************************************************************
   *IDENT 		-> Noeud.Ident                   -- attribut de type Chaine
   **************************************************************************/
// A COMPLETER
   private Type verifier_IDENT(Arbre a) throws ErreurVerif
   { return null;}
   
   /**************************************************************************
    * TYPE		-> IDENT
    *			|  TYPE_INTERVALLE
	*			|  Noeud.Tableau(TYPE_INTERVALLE, TYPE) 
    **************************************************************************/
// A COMPLETER
   
   /**************************************************************************
    * TYPE_INTERVALLE -> Noeud.Intervalle(EXP_CONST, EXP_CONST)
    **************************************************************************/
// A COMPLETER
   
   /**************************************************************************
    * EXP_CONST 	-> IDENT
                	|  Noeud.Entier                  -- attribut de type Entier
					|  Noeud.PlusUnaire(EXP_CONST)
					|  Noeud.MoinsUnaire(EXP_CONST)
    **************************************************************************/
// A COMPLETER
   
   /**************************************************************************
    * LISTE_INST 	-> Noeud.Vide 
					|  Noeud.ListeInst(LISTE_INST, INST) 
    **************************************************************************/
   private void verifier_LISTE_INST(Arbre a) throws ErreurVerif {
      // A COMPLETER
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
// A COMPLETER  
   
   /**************************************************************************
    * PAS 		-> Noeud.Increment(IDENT, EXP, EXP) 
				|  Noeud.Decrement(IDENT, EXP, EXP) 	
    **************************************************************************/
// A COMPLETER
   
   /**************************************************************************
    * PLACE 		-> IDENT
					|  Noeud.Index(PLACE, EXP) 
    **************************************************************************/
// A COMPLETER
   
   /**************************************************************************
    * LISTE_EXP   	-> Noeud.Vide
					|  Noeud.ListeExp(LISTE_EXP, EXP) 
    **************************************************************************/
// A COMPLETER
   
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
// A COMPLETER   
   private void verifier_EXP(Arbre a) throws ErreurVerif
   {
	   Type t1,t2; //On aura maximum 2 fils
	   Arbre f1,f2; //On aura maximum 2 fils
	   
	   switch (a.getNoeud())
	   {
	   //Type.Boolean -> Type.Boolean
	   case Non:
		   
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
		   else ErreurContext.ErreurNonRepertoriee.leverErreurContext("("+t1 + " , " + t2+")", a.getNumLigne());
		   
		   
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
		
	   //Type.Interval -> Type.Integer
  	   //Type.Real     -> Type.Real 
	   case PlusUnaire:
	   case MoinsUnaire:
		   
	   //Type.Interval, Type.Interval -> Type.Integer
  	   //Type.Interval, Type.Real     -> Type.Real
  	   //Type.Real,     Type.Interval -> Type.Real 		
  	   //Type.Real,     Type.Real     -> Type.Real	   
	   case Plus:
	   case Moins:
	   case Mult:
	
	   //Type.Interval, Type.Interval -> Type.Integer
	   case Quotient:
	   case Reste:
		
	   //Type.Interval, Type.Interval -> Type.Real 
	   //Type.Interval, Type.Real     -> Type.Real
	   //Type.Real,     Type.Interval -> Type.Real
	   //Type.Real,     Type.Real     -> Type.Real	   
	   case DivReel:
		
	   //Array(Type.Interval, <type>), Type.Interval -> <type>	   
	   case Index:
	   
       default:
           throw new ErreurInterneVerif("Appel incorrect a verifier_EXP ligne " + a.getNumLigne());
	   }

   }
   
   
   // ------------------------------------------------------------------------
   // COMPLETER les operations de vérifications et de décoration pour toutes 
   // les constructions d'arbres
   // ------------------------------------------------------------------------
 
   
}

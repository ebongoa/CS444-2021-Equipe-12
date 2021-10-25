/**
 * Type énuméré pour les erreurs contextuelles.
 * Ce type énuméré définit toutes les erreurs contextuelles possibles et 
 * permet l'affichage des messages d'erreurs pour la passe 2.
 */

// -------------------------------------------------------------------------
// A COMPLETER, avec les différents types d'erreur et les messages d'erreurs 
// correspondants
// -------------------------------------------------------------------------

package fr.esisar.compilation.verif;

public enum ErreurContext {
   
    ErreurNonRepertoriee,

    ErreurOperateurBinaireIntervalOuReel,
	ErreurOperateurBinaireInterval,
	ErreurOperateurBinaireBool,
	
	ErreurOperateurUnaireIntervalOuReel,
	ErreurOperateurUnaireBool,
	
	ErreurIndexationTableau,
	ErreurIndexInterval,
	
	ErreurConstInteger,
	
	ErreurIdent,
	ErreurIdentVar,
	ErreurIdentVarOuConst,
	
	ErreurInterval,
	ErreurIntervalInteger,
	ErreurLecture,
	ErreurEcriture,
	
	ErreurTypeInconnu,
	ErreurDejaDefini,
	
	ErreurBool,
	ErreurAffect,
	;
	
   void leverErreurContext(String s, int numLigne) throws ErreurVerif {
      String custom = "";
	  System.err.println("Erreur contextuelle : ");
      switch (this) 
      {
      //Dans EXP , retourne n1/t1 , n2/t2
      case ErreurOperateurBinaireIntervalOuReel:
          System.err.print("Operandes de type Interval ou Real attendu.Obtenu : " + s);
    	  break;     
      //Dans EXP , retourne n1 , n2
      case ErreurOperateurBinaireInterval:
          System.err.print("Operandes de type Interval attendu.Obtenu : " + s);
    	  break;
      //Dans EXP , retourne t1 , t2
      case ErreurOperateurBinaireBool:
          System.err.print("Operandes de type Boolean attendu.Obtenu : " + s);
    	  break;
    	  
      //Dans EXP , retourne n/t	  
      case ErreurOperateurUnaireIntervalOuReel:
          System.err.print("Operandesde type Interval ou Real attendu.Obtenu : "  + s);
    	  break;
      //Dans EXP , retourne t  
      case ErreurOperateurUnaireBool:
          System.err.print("Operande de type Boolean attendu.Obtenu : " + s);
    	  break;
    	  
      //Dans EXP et PLACE , retourne n
      case ErreurIndexationTableau:
          System.err.print("Seul un tableau peut être indexé.Tentative d'indexation d'un " + s);
    	  break;
      //Dans EXP et PLACE et TYPE , retourne n
      case ErreurIndexInterval:
          System.err.print("L'indexation se fait avec un Interval.Tentative d'indexation avec un " + s);
    	  break;
      //Dans EXP_CONST ; retourne n
      case ErreurConstInteger:
          System.err.print("Les bornes des intervalles doivent être des ConstInteger.Obtenu : " + s);
    	  break;
     //Dans IDENT et EXP_CONST , retourne s
      case ErreurIdent:
          System.err.print(s+" indéfini");
    	  break;
      //Dans PLACE , retourne s
      case ErreurIdentVar:
          System.err.print("Un nom de variable est attendu.Identificateur obtenu : " + s);
    	  break;
      //Dans EXP , retourne s
      case ErreurIdentVarOuConst:
          System.err.print("Dans une expression , l'utilisation de Type est interdite.Identificateur utilisé : " + s);
    	  break;
    
      //Dans INST et PAS , retourne n
      case ErreurInterval:
          System.err.print("On attend un Interval.Obtenu : "+ s);
    	  break;
      //Dans TYPE_INTERVALLE , retourne t
      case ErreurIntervalInteger:
          System.err.print("Les bornes d'un Interval doivent être des Integer.Obtenu : " + s);
    	  break;
      //Dans INST; retourne n/t
      case ErreurLecture:
          System.err.print("Read attend un Interval ou un Real.Obtenu : " + s);
    	  break;
      //Dans INST; retourne n/t
      case ErreurEcriture:
          System.err.print("Write attend un Interval ou un Real ou un String.Obtenu : " + s);
          break;
    	  
      //Dans TYPE , retourne s
      case ErreurTypeInconnu:
          System.err.print(s + " n'est pas un identificateur de Type.");
    	  break;
      //Dans IDENT , retourne s
      case ErreurDejaDefini:
          System.err.print("Redefinition de "+s);
    	  break;
      //Dans INST , retourn t
      case ErreurBool:
          System.err.print("Boolean attendu.Obtenu : "+ s);
    	  break;
      //Dans INST , retourne message
      case ErreurAffect:
          System.err.print( s);
    	  break;
      
      	default:
            System.err.print("non repertoriee");
      }
      System.err.println(" ... ligne " + numLigne);
      throw new ErreurVerif();
   }

}



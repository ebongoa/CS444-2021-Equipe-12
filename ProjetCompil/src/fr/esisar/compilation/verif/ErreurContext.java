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
	ErreurIntervalReel,
	
	ErreurTypeInconnu,
	ErreurDejaDefini,
	
	ErreurBool,
	ErreurAffect,
	;
	
   void leverErreurContext(String s, int numLigne) throws ErreurVerif {
      System.err.println("Erreur contextuelle : ");
      switch (this) 
      {
      //Dans EXP , retourne n1/t1 , n2/t2
      case ErreurOperateurBinaireIntervalOuReel:
    	  break;     
      //Dans EXP , retourne n1 , n2
      case ErreurOperateurBinaireInterval:
    	  break;
      //Dans EXP , retourne t1 , t2
      case ErreurOperateurBinaireBool:
    	  break;
    	  
      //Dans EXP , retourne n/t	  
      case ErreurOperateurUnaireIntervalOuReel:
    	  break;
      //Dans EXP , retourne t  
      case ErreurOperateurUnaireBool:
    	  break;
    	  
      //Dans EXP et PLACE , retourne n
      case ErreurIndexationTableau:
    	  break;
      //Dans EXP et PLACE et TYPE , retourne n
      case ErreurIndexInterval:
    	  break;
      //Dans EXP_CONST ; retourne n
      case ErreurConstInteger:
    	  break;
     //Dans IDENT et EXP_CONST , retourne s
      case ErreurIdent:
    	  break;
      //Dans PLACE , retourne s
      case ErreurIdentVar:
    	  break;
      //Dans EXP , retourne s
      case ErreurIdentVarOuConst:
    	  break;
    
      //Dans INST et PAS , retourne n
      case ErreurInterval:
    	  break;
      //Dans TYPE_INTERVALLE , retourne t
      case ErreurIntervalInteger:
    	  break;
      //Dans INST; retourne [Mode] n/t
      case ErreurIntervalReel:
    	  break;
      //Dans TYPE , retourne s
      case ErreurTypeInconnu:
    	  break;
      //Dans IDENT , retourne s
      case ErreurDejaDefini:
    	  break;
      //Dans INST , retourn t
      case ErreurBool:
    	  break;
      //Dans INST , retourne message
      case ErreurAffect:
    	  break;
      
      	default:
            System.err.print("non repertoriee");
      }
      System.err.println(" ... ligne " + numLigne);
      throw new ErreurVerif();
   }

}



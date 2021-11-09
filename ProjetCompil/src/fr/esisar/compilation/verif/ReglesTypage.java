package fr.esisar.compilation.verif;

import fr.esisar.compilation.global.src.*;

/**
 * La classe ReglesTypage permet de définir les différentes règles 
 * de typage du langage JCas.
 */

public class ReglesTypage {

   /**
    * Teste si le type t1 et le type t2 sont compatibles pour l'affectation, 
    * c'est à dire si on peut affecter un objet de t2 à un objet de type t1.
    */

   static ResultatAffectCompatible affectCompatible(Type t1, Type t2) {
      ResultatAffectCompatible res = new ResultatAffectCompatible();
      res.setOk(false);
      res.setConv2(false);
      
      if(t1.getNature().equals(t2.getNature())) {
    	  
    	  if(t1.getNature().equals(NatureType.Array)) {
    		  
    		  if(t1.getIndice().getNature().equals(NatureType.Interval) &&
    				  t2.getIndice().getNature().equals(NatureType.Interval) &&
    				  t1.getIndice().getBorneInf() == t2.getIndice().getBorneInf() &&
    				  t1.getIndice().getBorneSup() == t2.getIndice().getBorneSup() &&
    				  affectCompatible(t1.getElement(), t2.getElement()).getOk()) {
    			  res.setOk(true);
    		  }
    		  
    	  }
      }
      else if(t1.equals(Type.Real) && t2.getNature().equals(NatureType.Interval)) {
    	  res.setOk(true);
      }
      else if (t1.equals(Type.Real) && t2.equals(Type.Integer)) {
    	  res.setConv2(true);
      }
      
      return res;
   }

   /**
    * Teste si le type t1 et le type t2 sont compatible pour l'opération 
    * binaire représentée dans noeud.
    */

   static ResultatBinaireCompatible binaireCompatible
      (Noeud noeud, Type t1, Type t2) {
	   ResultatBinaireCompatible res = new ResultatBinaireCompatible();
	   
	   if((noeud.equals(Noeud.Et) || noeud.equals(Noeud.Ou))  && 
			   t1.equals(Type.Boolean) && t2.equals(Type.Boolean)) {
		   res.setConv1(false);
		   res.setConv2(false);
		   res.setOk(true);
		   res.setTypeRes(Type.Boolean);
	   }
	   else if(noeud.equals(Noeud.Inf) || noeud.equals(Noeud.Sup) ||
			   noeud.equals(Noeud.Egal) || noeud.equals(Noeud.InfEgal) ||
			   noeud.equals(Noeud.SupEgal) || noeud.equals(Noeud.NonEgal)) {
		   res.setTypeRes(Type.Boolean);
		   res.setOk(true);
		   
		   if ((t1.getNature().equals(NatureType.Interval) && 
				   t2.getNature().equals(NatureType.Interval)) ||
				   (t1.getNature().equals(NatureType.Real) && 
						   t2.getNature().equals(NatureType.Real))) {
			   res.setConv1(false);
			   res.setConv2(false);
		   }
		   else if(t1.getNature().equals(NatureType.Interval) && 
				   t2.getNature().equals(NatureType.Real)) {
			   res.setConv1(true);
			   res.setConv2(false);
		   }
		   else if(t1.getNature().equals(NatureType.Real) && 
				   t2.getNature().equals(NatureType.Interval)) {
			   res.setConv1(false);
			   res.setConv2(true);
		   }
		   else {
			   res.setConv1(false);
			   res.setConv2(false);
			   res.setOk(false);
			   res.setTypeRes(null);
		   }
		   
	   }
	   else if(noeud.equals(Noeud.Plus) || noeud.equals(Noeud.Moins) ||
			   noeud.equals(Noeud.Mult)) {
		   res.setOk(true); 
		   
		   if (t1.getNature().equals(NatureType.Interval) && 
				   t2.getNature().equals(NatureType.Interval)) {
			   res.setConv1(false);
			   res.setConv2(false);
			   res.setTypeRes(Type.Integer);
		   }
		   else if(t1.getNature().equals(NatureType.Real) && 
				   t2.getNature().equals(NatureType.Real)) {
			   res.setConv1(false);
			   res.setConv2(false);
			   res.setOk(true);
			   res.setTypeRes(Type.Real);
		   }
		   else if(t1.getNature().equals(NatureType.Interval) && 
				   t2.getNature().equals(NatureType.Real)) {
			   res.setConv1(true);
			   res.setConv2(false);
			   res.setTypeRes(Type.Real);
		   }
		   else if(t1.getNature().equals(NatureType.Real) && 
				   t2.getNature().equals(NatureType.Interval)) {
			   res.setConv1(false);
			   res.setConv2(true);
			   res.setTypeRes(Type.Real);
		   }
		   else {
			   res.setConv1(false);
			   res.setConv2(false);
			   res.setOk(false);
			   res.setTypeRes(null);
		   }
		   
	   }
	   else if((noeud.equals(Noeud.Reste) || noeud.equals(Noeud.Quotient)) &&
			   t1.getNature().equals(NatureType.Interval) && 
			   t2.getNature().equals(NatureType.Interval)) {
		   res.setConv1(false);
		   res.setConv2(false);
		   res.setOk(true);
		   res.setTypeRes(Type.Integer);
	   }
	   else if(noeud.equals(Noeud.DivReel)) {
		   res.setTypeRes(Type.Real);
		   res.setOk(true); 
		   
		   if ((t1.getNature().equals(NatureType.Interval) && 
				   t2.getNature().equals(NatureType.Interval)) ||
				   (t1.getNature().equals(NatureType.Real) && 
						   t2.getNature().equals(NatureType.Real))) {
			   res.setConv1(false);
			   res.setConv2(false);
		   }
		   else if(t1.getNature().equals(NatureType.Interval) && 
				   t2.getNature().equals(NatureType.Real)) {
			   res.setConv1(true);
			   res.setConv2(false);
		   }
		   else if(t1.getNature().equals(NatureType.Real) && 
				   t2.getNature().equals(NatureType.Interval)) {
			   res.setConv1(false);
			   res.setConv2(true);
		   }
		   else {
			   res.setConv1(false);
			   res.setConv2(false);
			   res.setOk(false);
			   res.setTypeRes(null);
		   }
		   
	   }
	   else if(noeud.equals(Noeud.Tableau) && 
			   t1.getNature().equals(NatureType.Array)
			   && t2.getNature().equals(NatureType.Interval)) {
		   res.setConv1(false);
		   res.setConv2(false);
		   res.setOk(true);
		   res.setTypeRes(t1.getElement());
	   }
	   else {
		   res.setConv1(false);
		   res.setConv2(false);
		   res.setOk(false);
		   res.setTypeRes(null);
	   }
	   
	   return res;
   }

   /**
    * Teste si le type t est compatible pour l'opération binaire représentée 
    * dans noeud.
    */
   static ResultatUnaireCompatible unaireCompatible
         (Noeud noeud, Type t) {
	   ResultatUnaireCompatible res = new ResultatUnaireCompatible();
	   
	   if(noeud.equals(Noeud.Non) && 
			   t.getNature().equals(NatureType.Boolean)) {
		   res.setOk(true);
		   res.setTypeRes(Type.Boolean);
	   }
	   else if((noeud.equals(Noeud.PlusUnaire) || 
			   noeud.equals(Noeud.MoinsUnaire))
			   && t.getNature().equals(NatureType.Interval)) {
		   res.setOk(true);
		   res.setTypeRes(Type.Integer);
	   }
	   else if((noeud.equals(Noeud.PlusUnaire) || 
			   noeud.equals(Noeud.MoinsUnaire))
			   && t.getNature().equals(NatureType.Real)) {
		   res.setOk(true);
		   res.setTypeRes(Type.Real);
	   }
	   else {
		   res.setOk(false);
		   res.setTypeRes(null);
	   }
	   
	   return res;
   }
         
}


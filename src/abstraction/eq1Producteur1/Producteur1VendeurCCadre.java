/**@authors ER-RAHMAOUY Abderrahmane & Haythem*/
package abstraction.eq1Producteur1;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.BourseCacao;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

public class Producteur1VendeurCCadre extends Producteur1VendeurBourse implements IVendeurContratCadre {

	protected SuperviseurVentesContratCadre supCC;
	protected List<ExemplaireContratCadre> contratsEnCours;
	protected List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCoC;

	public Producteur1VendeurCCadre() {
		super();
		this.contratsEnCours=new LinkedList<ExemplaireContratCadre>();
		this.contratsTermines=new LinkedList<ExemplaireContratCadre>();
		this.journalCoC = new Journal(this.getNom()+" journal CC", this);
	}

	public void initialiser() {
		super.initialiser();
		this.supCC = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
	}
	/**
	 * Gère les actions à effectuer à chaque étape pour le vendeur en contrat cadre.
	 */
	public void next() {
		super.next();
		this.journalCoC.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		for (Feve f : stock.keySet()) {
			//List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(f);
			//System.out.println(acheteurs);
			/*
			System.out.println(f.toString());
			System.out.println(this.Stocck.get(f)-restantDu(f)>1200);
			System.out.println(this.Stocck.get(f)-restantDu(f));
			System.out.println(stock.get(f).getValeur()-restantDu(f)>1200);
			System.out.println(stock.get(f).getValeur()-restantDu(f));
			*/
			if (stock.get(f).getValeur()-restantDu(f)>1200 ) { // au moins 100 tonnes par step pendant 6 mois
				this.journalCoC.ajouter("   "+f+" suffisamment en stock pour passer un CC");
				double parStep = Math.max(100, (stock.get(f).getValeur()-restantDu(f))/2); // au moins 100, et pas plus que la moitie de nos possibilites divisees par 2
				Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12, parStep);
				List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(f);
				//acheteurs.remove(acheteurs.size()-1);
				//System.out.println(acheteurs);
				//System.out.println(Filiere.LA_FILIERE.getMarquesChocolat());
				if (acheteurs.size()>0) {
					IAcheteurContratCadre acheteur = acheteurs.get(Filiere.random.nextInt(acheteurs.size()));
					journalCoC.ajouter("   "+acheteur.getNom()+" retenu comme acheteur parmi "+acheteurs.size()+" acheteurs potentiels");
					ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur, this, f, e, cryptogramme, false);
					if (contrat==null) {
						journalCoC.ajouter(Color.RED, Color.white,"   echec des negociations");
					} else {
						this.contratsEnCours.add(contrat);
						journalCoC.ajouter(Color.GREEN, acheteur.getColor(), "   contrat signe");
					}
				} else {
					journalCoC.ajouter("   pas d'acheteur");
				}
			}

		}
	}
	/**
	 * Calcule la quantité restante à livrer pour un type de fève donné.
	 * @param f Le type de fève.
	 * @return La quantité restante à livrer.
	 */
	public double restantDu(Feve f) {
		double res=0;
		for (ExemplaireContratCadre c : this.contratsEnCours) {
			if (c.getProduit().equals(f)) {
				res+=c.getQuantiteRestantALivrer();
			}
		}
		return res;
	}
	/**
	 * Calcule le prix des fèves en fonction des contrats en cours et terminés.
	 * @param f Le type de fève.
	 * @re)turn Le prix des fèves.
	 */
	public double prix(Feve f) {
		double res = 0;
		int count = 0;
		for (ExemplaireContratCadre c : this.contratsEnCours) {
			if (c.getProduit().equals(f)) {
				res += c.getPrix();
				count += 1;
			}
		}
		for (ExemplaireContratCadre c : this.contratsTermines) {
			if (c.getProduit().equals(f)) {
				res += c.getPrix();
				count +=1;
			}
		}
		if (count != 0) {return res/count;}
		Gamme gamme = f.getGamme();
		boolean bio =f.isBio();
		boolean equitable = f.isEquitable();
		double prime = 0;
		/*
		if(gamme== Gamme.BQ) {
			throw new IllegalArgumentException("Gamme BQ ne peut pas etre solde en Contrat Cadre.");
		}
		if (gamme == Gamme.MQ && !bio && !equitable ) {
			throw new IllegalArgumentException("Gamme MQ sans label ne se vend pas en Contrat Cadre.");
		}
		if (gamme == Gamme.HQ && !bio && !equitable ) {
			throw new IllegalArgumentException("Gamme HQ sans label ne se vend pas en Contrat Cadre.");
		}
		 */
		if (bio) {
			prime += 100;
		}
		if (equitable) {
			prime += 60;
		}
		if (gamme == Gamme.HQ) {
			prime += 300;
		}
		res += prime + 1472;
		return res;

	}

	@Override
	public boolean vend(IProduit produit) {
		String s = produit.getType();
		if (s.equals("Feve")) {
			Feve f = (Feve) produit;
			this.stock.get(f);
			if (this.stock.get(f).getValeur()>10) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
		journalCoC.ajouter("      contreProposition("+contrat.getProduit()+" avec echeancier "+contrat.getEcheancier());
		Echeancier ec = contrat.getEcheancier();
		IProduit produit = contrat.getProduit();
		boolean accepted = false;
		//Echeancier res = ec;
		String type = produit.getType();
		if (type != "Feve") {
			journalCoC.ajouter("Ce n'est pas une feve");
			return null;
		}
		Feve f = (Feve) produit;
		Double stockdispo = stock.get((Feve) produit).getValeur()-restantDu((Feve) produit);
		if (stockdispo < 600) { //Au moins 50 tonnes par step
			journalCoC.ajouter("Je n'ai que" +stockdispo);
			return null;
		}
		int duree = ec.getStepFin()-ec.getStepDebut();
		
		if (duree < 10) {
			journalCoC.ajouter("Pas de contract avec une duree inferieure a 5 mois");
			return null;
		}
		if (Filiere.LA_FILIERE.getEtape() < 1) {
			journalCoC.ajouter("On fait pas de contract pendant la 1ere etape");
			return null;
		}
		if (this.contratsEnCours.size() >= 5 ) {
			journalCoC.ajouter("On fait pas plus que de 3 contracts en meme temps");
			return null;
		}
		if (ec.getStepDebut()<Filiere.LA_FILIERE.getEtape()+8) {
			accepted = true;
		}
		if (accepted = false) {
			if (ec.getQuantiteTotale()<=stock.get((Feve)produit).getValeur()-restantDu((Feve)produit)) {
				journalCoC.ajouter("      je retourne "+new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12,  (int)(ec.getQuantiteTotale()/12)));
				return new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12,  (int)(ec.getQuantiteTotale()/12));
			} else {
				journalCoC.ajouter("      je retourne "+new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12,  (int)((stock.get((Feve)produit).getValeur()-restantDu((Feve)produit)/12))));
				return new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12,  (int)((stock.get((Feve)produit).getValeur()-restantDu((Feve)produit)/12)));
			}
		}
		journalCoC.ajouter("Echencier accepted");
		return ec;		

	}

	@Override
	public double propositionPrix(ExemplaireContratCadre contrat){
		// TODO Auto-generated method stub
		if (!contrat.getProduit().getType().equals("Feve")) {
			return 0;  
		}
		
		return prix((Feve) contrat.getProduit());



	}

	@Override
	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
		// TODO Auto-generated method stub
		List<Double> prix = contrat.getListePrix();
		if (prix.get(prix.size()-1)>=0.975*prix.get(0)) {
			journalCoC.ajouter("      contrePropose le prix demande : "+contrat.getPrix());
			return contrat.getPrix();
		} else {
			double p = prix.get(0)/prix.get(1);
			journalCoC.ajouter("      contreproposition("+contrat.getPrix()+") retourne "+prix.get(0)*1.05);
			return prix.get(0)*1.05;
		}

	}

	@Override
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
		// TODO Auto-generated method stub
		this.journal.ajouter("Nouveau contract sur le marche :" + contrat);
	}

	@Override
	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
		// TODO Auto-generated method stub
		double stockActuel = stock.get(produit).getValeur((Integer)cryptogramme);
		double aLivre = Math.min(quantite, stockActuel);
		journalCoC.ajouter("   Livraison de "+aLivre+" T de "+produit+" sur "+quantite+" exigees pour contrat "+contrat.getNumero());
		stock.get(produit).setValeur(this, aLivre, (Integer)cryptogramme);
		return aLivre;

	}
	/**
	 * Renvoie les journaux de l'acteur, y compris le journal des contrats cadre.
	 * @return Une liste contenant les journaux de l'acteur.
	 */
	public List<Journal> getJournaux() {
		List<Journal> res=super.getJournaux();
		res.add(journalCoC);
		return res;
	}
}

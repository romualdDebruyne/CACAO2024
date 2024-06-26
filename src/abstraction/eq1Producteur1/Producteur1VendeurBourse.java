/**@authors Fatima-Ezzahra  */
package abstraction.eq1Producteur1;

import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

public class Producteur1VendeurBourse extends Producteur1Production implements  IVendeurBourse {
	public double  pourcentageHQ=0.02 ;
	public double  pourcentageBQ=0.02 ;
	public double  pourcentageMQ=0.02 ;
	private Journal journalBourse;
	protected ArrayList<Double> bourseBQ; 
	protected ArrayList<Double> bourseMQ; 
	protected ArrayList<Double> bourseHQ; 
	/**
	 * Constructeur de la classe Producteur1VendeurBourse.
	 */
	public Producteur1VendeurBourse() {
		super();
		this.journalBourse = new Journal(this.getNom()+" journal Bourse", this);
		bourseBQ = new ArrayList<Double>();
		bourseMQ = new ArrayList<Double>();
		bourseHQ = new ArrayList<Double>();
	}



	// Fatima-ezzahra
	@Override

	/**
	 * Offre une quantité de fèves sur la bourse en fonction de son prix seuil et du cours actuel.
	 * @param f Le type de fève à offrir.
	 * @param cours Le cours actuel de la fève sur la bourse.
	 * @return La quantité de fèves offerte sur la bourse.
	 */
	public double offre(Feve f, double cours) {
		// TODO Auto-generated method stub

		//if (f.isBio() || f.isEquitable()) {
			//journalBourse.ajouter("On vend pas de bio ni equitable en bourse");
			//return 0;
		//}
		double quantiteEnT = this.getQuantiteEnStock(  f ,   cryptogramme);


		if (quantiteEnT!=0) { 
			double Seuil = getCoutUnitaireProduction(f);


			if (f.getGamme()==Gamme.MQ) {
				if (true) {
				//if(cours>= (pourcentageMQ+1)*Seuil*quantiteEnT) {
					journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je met en vente "+quantiteEnT+" T de "+f);

					return quantiteEnT;

				}
				
			}
			if (f.getGamme()==Gamme.HQ) {
				if (true) {
				//if(cours>= (pourcentageHQ+1)*Seuil*quantiteEnT) {
					journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je met en vente "+quantiteEnT+" T de "+f);

					return quantiteEnT;
				}
			
			}
			if (f.getGamme()==Gamme.BQ) {
				if (true) {
				//if(cours>= (pourcentageMQ+1)*Seuil*quantiteEnT) {
					//double offre =  this.stock.get(f).getValeur()*(Math.min(cours, 3000)/3000.0);
					journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je met en vente "+quantiteEnT+" T de "+f);
					return quantiteEnT;
				}
				
			}
		}
		journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je met en vente 0.0 T de "+f);
		return 0;


	}
	/**
	 * Gère la notification de vente d'une quantité de fèves et met à jour le stock et le solde.
	 * @param f Le type de fève vendu.
	 * @param quantiteEnT La quantité de fèves vendue.
	 * @param coursEnEuroParT Le cours de vente de la fève.
	 * @return La quantité de fèves retirée du stock.
	 */
	@Override
	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT) {
		// TODO Auto-generated method stub

		double retire = Math.min(this.stock.get(f).getValeur(), quantiteEnT);
		this.stock.get(f).retirer(this, retire, cryptogramme);

		journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : j'ai vendu "+quantiteEnT+" T de "+f+" -> je retire "+retire+" T du stock qui passe a "+this.stock.get(f).getValeur((Integer)cryptogramme));
		super.notificationOperationBancaire(retire*coursEnEuroParT);
		super.getSolde();
		

		return retire;
	}
	/**
	 * Notifie l'acteur qu'il est blacklisté pour une certaine durée.
	 * @param dureeEnStep La durée de la mise en blacklist en nombre d'étapes.
	 */
	@Override
	public void notificationBlackList(int dureeEnStep) {
		// TODO Auto-generated method stub
		journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je suis blackliste pour une duree de "+dureeEnStep+" etapes");


	}
	public void changePlant() {
		double ameBQ = 0; double ameMQ= 0; double ameHQ = 0;
		if (bourseBQ.size() > 12) {
			for (int i = 0; i < bourseBQ.size()-1;i++) {
				ameBQ += (bourseBQ.get(i+1)-bourseBQ.get(i+1))/bourseBQ.get(i);
			}
		}
	}
	/**
	 * Renvoie les journaux de l'acteur, y compris le journal de la bourse.
	 * @return Une liste contenant les journaux de l'acteur.
	 */
	public List<Journal> getJournaux() {
		List<Journal> res=super.getJournaux();
		res.add(journalBourse);
		return res;
	}
	public void updatePlant() {
		
	}
	public void next() {
		super.next();
		changePlant();
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		bourseBQ.add(bourse.getCours(Feve.F_BQ).getValeur());
		bourseMQ.add(bourse.getCours(Feve.F_MQ).getValeur());
		bourseHQ.add(bourse.getCours(Feve.F_HQ).getValeur());
		
	}

}

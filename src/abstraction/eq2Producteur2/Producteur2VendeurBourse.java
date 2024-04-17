package abstraction.eq2Producteur2;

import java.util.List;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;

//Faite par Maxime 

public abstract class Producteur2VendeurBourse extends Producteur2_Plantation implements IVendeurBourse {
	
	private Journal journalBourse;
	
	public Producteur2VendeurBourse() {
		super();
		this.journalBourse = new Journal(this.getNom()+" journal Bourse", this);

	}

	@Override
	public double offre(Feve f, double cours) {
		if (f.getGamme()==Gamme.MQ) {
			double offre = this.stock.get(f)*(Math.min(cours, 5000)/5000.0);
			journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je met en vente "+offre+" T de "+f);
			return offre;
		} else if (f.getGamme()==Gamme.BQ) {
			double offre =  this.stock.get(f)*(Math.min(cours, 3000)/3000.0);
			journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je met en vente "+offre+" T de "+f);
			return offre;			
		} else { // normalement impossible vu que le HQ n'est pas en bourse
			journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je met en vente 0.0 T de "+f);
			return 0.0;
		}
	}

	@Override
	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT) {
		double retire = Math.min(this.stock.get(f), quantiteEnT);;
		stock.put(f, stock.get(f)-retire);
		journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : j'ai vendu "+quantiteEnT+" T de "+f+" -> je retire "+retire+" T du stock qui passe a "+this.stock.get(f));
		return retire;
	}

	@Override
	public void notificationBlackList(int dureeEnStep) {
		journalBourse.ajouter(Filiere.LA_FILIERE.getEtape()+" : je suis blackliste pour une duree de "+dureeEnStep+" etapes");
	}
	
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalBourse);
		return jx;
	}
	public void next() {
		journalBourse.ajouter("------------ ETAPE " + Filiere.LA_FILIERE.getEtape() + " ---------------");
		super.next();
	}
}

package abstraction.eq2Producteur2;

import abstraction.eqXRomu.produits.Feve;

public abstract class Producteur2_Plantation extends Producteur2_MasseSalariale {
	protected int nb_hectares_max;
	protected int nb_hectares_actuel;
	protected int prix_plantation_hectare;
	
	protected int qualitee;
	protected double pourcentage_HQ = 0.02;
	protected double pourcentage_MQ = 0.38;
	protected double pourcentage_BQ = 0.6;
	
	protected double rend_pest_BQ = 0.9;
	protected double rend_pest_MQ = 0.85;
	protected double rend_pest_HQ = 0.80;
	protected double rend_no_pest_BQ = 0.82;
	protected double rend_no_pest_MQ = 0.77;
	protected double rend_no_pest_HQ = 0.72;
	

	public double getNb_hectares_max() {
		return nb_hectares_max;
	}

	public void setNb_hectares_max(int nb_hectares_max) {
		this.nb_hectares_max = nb_hectares_max;
	}

	public int getNb_hectares_actuel() {
		return nb_hectares_actuel;
	}

	public void setNb_hectares_actuel(int nb_hectares_actuel) {
		this.nb_hectares_actuel = nb_hectares_actuel;
	}

	public double getPrix_plantation_hectare() {
		return prix_plantation_hectare;
	}

	public void setPrix_plantation_hectare(int prix_plantation_hectare) {
		this.prix_plantation_hectare = prix_plantation_hectare;
	}
	
	public double getPourcentage_HQ() {
		return pourcentage_HQ;
	}

	public void setPourcentage_HQ(double pourcentage_HQ) {
		this.pourcentage_HQ = pourcentage_HQ;
	}

	public double getPourcentage_MQ() {
		return pourcentage_MQ;
	}

	public void setPourcentage_MQ(double pourcentage_MQ) {
		this.pourcentage_MQ = pourcentage_MQ;
	}

	public double getPourcentage_BQ() {
		return pourcentage_BQ;
	}

	public void setPourcentage_BQ(double pourcentage_BQ) {
		this.pourcentage_BQ = pourcentage_BQ;
	}
	
	public void initialiser() {
		super.initialiser();
		setNb_hectares_max(5000000);
		setNb_hectares_actuel(5000000);
		setPrix_plantation_hectare(0); // à définir
		return;
	}
	
	
	
	public void planter(int nb_hectares) {
		if (getNb_hectares_actuel() + nb_hectares > getNb_hectares_max()) { //achat impossible
			return;
		}
		else { 
			setNb_hectares_actuel(getNb_hectares_actuel() + nb_hectares);
		}
	}
	
	public double production_cacao() { // retourne la production actuelle de cacao sur 2 semaines en kg
		return getNb_hectares_actuel() * 500 / 26;
	}
	
	public double production_HQ() { // retourne la production de cacao de haute qualité sur 2 semaines en kg
		return production_cacao()* getPourcentage_HQ();
		
	}

	

	public double production_BQ() { // retourne la production de cacao de basse qualité sur 2 semaines en kg
		return production_cacao() * getPourcentage_BQ();
	}
	public double production_MQ() { // retourne la production de cacao de moyenne qualité sur 2 semaines en kg
		return production_cacao() * getPourcentage_MQ();
	}
	
	
	// Retourne la production de cacao BQ, MQ et HQ après calculs des rendements en kilos
	public double get_prod_no_pest_HQ() { 
		return this.production_HQ() * rend_no_pest_HQ; //feve HQ_BE
	}
	
	public double get_prod_no_pest_MQ() {
		return this.production_MQ() * rend_no_pest_MQ;
	}
	
	public double get_prod_no_pest_BQ() {
		return this.production_BQ() * rend_no_pest_BQ; 
	}
	
	public double get_prod_pest_HQ() {
		return this.production_HQ() * rend_pest_HQ; //feve HQ_E et HQ=0
	}
	
	public double get_prod_pest_MQ() {
		return this.production_MQ() * rend_pest_MQ; //feve MQ=98%*get_prod_pest et MQ_E=2%*get_prod_pest
	}
	
	public double get_prod_pest_BQ() {
		return this.production_BQ() * rend_pest_BQ; //feve BQ
	}
	
	public void nouveau_stock() { // ajoute la producution sur 2 semaines aux stocks
		ajout_stock(production_BQ(),production_MQ(),0,production_HQ(),0,0);
	}
	
	public void achat_hectare(int nb_hectare) { //fonction permettant d'achter un hectare
		cout_plantation();
	}
	public void achat_plantation() {
		if (getStockTotal(this.cryptogramme) == 0.0) {
			if (nb_hectares_actuel * 1.02 > nb_hectares_max) {
				planter(nb_hectares_max - nb_hectares_actuel);
			}
			planter((int) (nb_hectares_actuel * 0.02)); //on replante 2% de la plantation actuel
		}
	}
	
	public void modifie_prodParStep() {
	    this.prodParStep.put(Feve.F_HQ, production_HQ());
	    this.prodParStep.put(Feve.F_MQ, production_MQ());
	    this.prodParStep.put(Feve.F_BQ, production_BQ());

	    this.prodParStep.put(Feve.F_HQ_BE, 0.0);
	    this.prodParStep.put(Feve.F_HQ_E, 0.0);
	    this.prodParStep.put(Feve.F_MQ_E, 0.0);
	}
	
	public double cout_plantation() {
		return 0 ;
	}
} 
// 1hectare = 500kg / an cacao
// implémenter la qualité 

// B219

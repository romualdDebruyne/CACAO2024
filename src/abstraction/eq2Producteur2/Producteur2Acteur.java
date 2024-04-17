package abstraction.eq2Producteur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariableReadOnly;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

public abstract class Producteur2Acteur implements IActeur {
	protected int cryptogramme;
	protected Journal journal;

	protected HashMap<Feve,Double> stock; //Feve = qualite et Variable = quantite
	protected HashMap<Feve,Double> prodParStep;
	private static final double PART=0.1;
	protected HashMap <Feve, Variable> stock_variable;

	public abstract double get_prod_pest_BQ();
	public abstract double get_prod_pest_MQ();
	public abstract double get_prod_pest_HQ();
	
	public Producteur2Acteur() {
		this.journal = new Journal(this.getNom()+" journal", this);
		this.stock = new HashMap<Feve, Double>();
		this.prodParStep= new HashMap<Feve, Double>();
		this.stock_variable= new HashMap<Feve, Variable>();
		
		this.init_stock(Feve.F_BQ, 103846153.8);
		this.init_stock(Feve.F_MQ, 62115384.62);
		this.init_stock(Feve.F_HQ_E, 3076923.076);
		this.lot_to_hashmap();
		
		prodParStep.put(Feve.F_HQ_BE, 0.0);
		prodParStep.put(Feve.F_HQ_E, 0.0);
		prodParStep.put(Feve.F_HQ, 0.0);
		prodParStep.put(Feve.F_MQ_E, 0.0);
		prodParStep.put(Feve.F_MQ, 0.0);
		prodParStep.put(Feve.F_BQ, 0.0);
		
		for (Feve f : Feve.values()) {
			this.stock_variable.put(f,  new Variable("EQ2Stock"+f, this, 0));
		}
	}
	
	public abstract void init_stock(Feve type_feve, double quantite);
	public abstract void lot_to_hashmap();
	
	public void initialiser() {
		
		
		//initialisation prodparstep pour faire marcher get indicateur || à modifier		
	}
	

	public HashMap<Feve, Variable> getStock_variable() {
		return stock_variable;
	}
	public void setStock_variable(HashMap<Feve, Variable> stock_variable) {
		this.stock_variable = stock_variable;
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ2";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////
	
	public void next() {
		this.DebiteCoutParStep();
			
		//this.getIndicateurs();
		this.journal.ajouter("--------------- étape = " + Filiere.LA_FILIERE.getEtape()+ " -----------------------------");
		this.journal.ajouter("cout de stockage moyen de la filiere " + Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur());
		this.journal.ajouter("\n Argent sortant : " + this.getCoutTotalParStep());
		this.journal.ajouter("Solde après débit : " + this.getSolde()+"\n");
		
		for (Feve f : Feve.values()) {
			this.stock_variable.get(f).setValeur(this, this.stock.get(f));
		}
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(244, 198, 156); 
	}

	public String getDescription() {
		return "Nous sommes CacaoLand, producteur au sein de la filière du cacao. Notre objectif est de produire du cacao de haute qualité de manière équitable avec également du cacao de basse et moyenne qualité en quantité.";
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		for (Feve f: Feve.values()) {
			res.add(this.stock_variable.get(f));
		}
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		res.add(journal);
		return res;
	}

	////////////////////////////////////////////////////////
	//               En lien avec la Banque               //
	////////////////////////////////////////////////////////

	// Appelee en debut de simulation pour vous communiquer 
	// votre cryptogramme personnel, indispensable pour les
	// transactions.
	public void setCryptogramme(Integer crypto) {
		this.cryptogramme = crypto;
	}

	// Appelee lorsqu'un acteur fait faillite (potentiellement vous)
	// afin de vous en informer.
	public void notificationFaillite(IActeur acteur) {
	}

	// Apres chaque operation sur votre compte bancaire, cette
	// operation est appelee pour vous en informer
	public void notificationOperationBancaire(double montant) {
	}
	
	// Renvoie le solde actuel de l'acteur
	protected double getSolde() {
		return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
	}

	////////////////////////////////////////////////////////
	//        Pour la creation de filieres de test        //
	////////////////////////////////////////////////////////

	// Renvoie la liste des filieres proposees par l'acteur
	public List<String> getNomsFilieresProposees() {
		ArrayList<String> filieres = new ArrayList<String>();
		return(filieres);
	}

	// Renvoie une instance d'une filiere d'apres son nom
	public Filiere getFiliere(String nom) {
		return Filiere.LA_FILIERE;
	}

	//Faite par Quentin
	//Retourne la quantité stockée pour un type de produit (un type de fève)
	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			double quantite_stockee_prod = this.stock.get(p);
			return quantite_stockee_prod;
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
	
	//Faite par Quentin
	//Retourne la quantité totale de fèves stockée
	public double getStockTotal(int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			double quantite_stockee = 0;
			for(Feve f : this.stock.keySet()) {
				quantite_stockee += this.stock.get(f);
			}
			return quantite_stockee;
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
	
	// Fait par Noémie
	public abstract double cout_total_stock();
	public abstract double cout_humain_par_step();
	public abstract double cout_plantation();
	
	//
	public double getCoutTotalParStep() {
		double somme = this.cout_total_stock() + this.cout_humain_par_step() + this.cout_plantation();
		return somme;
	}
		
	public void DebiteCoutParStep() {
		retireArgent(this.cout_total_stock(), "coût des stocks");	
		retireArgent(this.cout_humain_par_step(), "coût humain");	
		retireArgent(this.cout_plantation(), "coût de la plantation");	
	}
	
	public void retireArgent(double montant, String raison) {
		if (montant>0) {
			Filiere.LA_FILIERE.getBanque().payerCout(this, this.cryptogramme, raison, montant);
		}
			
	}
}


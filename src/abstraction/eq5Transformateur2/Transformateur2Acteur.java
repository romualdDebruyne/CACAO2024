package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.acteurs.Romu;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.filiere.IMarqueChocolat;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariablePrivee;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur2Acteur implements IActeur,IMarqueChocolat, IFabricantChocolatDeMarque {
	
	protected Journal journal;
	protected int cryptogramme;
	private double coutStockage;
	

	protected static final double STOCKINITIAL = 50000.0;
	
	protected List<Feve> lesFeves;
	protected List<Chocolat> lesChocolats;
	protected List<ChocolatDeMarque>chocosProduits;
	protected HashMap<Feve, Variable> stockFeves;
	protected HashMap<Chocolat, Variable> stockChoco;
	protected HashMap<ChocolatDeMarque, Variable> stockChocoMarque;
	protected HashMap<ChocolatDeMarque, Double> VariationStockChocoMarque; // pour le calcul des coûts de transfo
	protected HashMap<Feve, HashMap<Chocolat, Double>> pourcentageTransfo; // dictionnaire de dictionnaire [feve : [Type chocolat : % cacao ]]
	protected List<ChocolatDeMarque> chocolatsFusion;
	protected Variable totalStocksFeves;  // La quantite totale de stock de feves 
	protected Variable totalStocksChoco;  // La qualntite totale de stock de chocolat 
	protected Variable totalStocksChocoMarque;  // La quantite totale de stock de chocolat de marque 
	
	////////////////////////////////////////////
	// Constructor & Initialization of stocks //
	////////////////////////////////////////////
	/**
	 * @Robin 
	 */
	public Transformateur2Acteur() {
		this.journal = new Journal(this.getNom()+" journal", this);
		this.totalStocksFeves = new VariablePrivee("Eq5TStockFeves", "<html>Quantite totale de feves en stock</html>",this, 0.0, 1000000.0, 0.0);
		this.totalStocksChoco = new VariablePrivee("Eq5TStockChoco", "<html>Quantite totale de chocolat en stock</html>",this, 0.0, 1000000.0, 0.0);
		this.totalStocksChocoMarque = new VariablePrivee("Eq5TStockChocoMarque", "<html>Quantite totale de chocolat de marque en stock</html>",this, 0.0, 1000000.0, 0.0);
		
		this.lesFeves = new LinkedList<Feve>();
		this.journal.ajouter("Les Feves sont :");
		for (Feve f : Feve.values()) {
			if (f.getGamme()!=Gamme.HQ) {
			this.lesFeves.add(f);
			this.journal.ajouter("   - "+f);
			}
		}
		
		this.stockFeves=new HashMap<Feve,Variable>();
		for (Feve f : this.lesFeves) {
			if (f.getGamme()!=Gamme.HQ) {
				this.stockFeves.put(f, new Variable("Eq5Stock "+f, this, STOCKINITIAL));
				this.journal.ajouter("ajout de "+STOCKINITIAL+" tonnes de : "+f+" au stock total de fèves // stock total : "+this.totalStocksFeves.getValeur(this.cryptogramme));
			}
		}
		
		this.lesChocolats = new LinkedList<Chocolat>();
		this.journal.ajouter("Nos Chocolats sont :");
		for (Chocolat c : Chocolat.values()) {
			if (c.getGamme()!=Gamme.HQ) {
				this.lesChocolats.add(c);
				this.journal.ajouter("   - "+c);
			}
		}
		this.stockChoco=new HashMap<Chocolat,Variable>();
		for (Chocolat c : this.lesChocolats) {
			this.stockChoco.put(c, new Variable("Eq5Stock "+c, this, STOCKINITIAL));
			this.journal.ajouter("ajout de "+STOCKINITIAL+" tonnes de : "+c+" au stock total de Chocolat // stock total : "+this.totalStocksChoco.getValeur(this.cryptogramme));
		}
		
	}
	
	/**
	 * @Robin 
	 * @Erwann
	 */
	public void initialiser() {
		
		this.totalStocksFeves.ajouter(this, STOCKINITIAL, this.cryptogramme);
		
		this.totalStocksChoco.ajouter(this, STOCKINITIAL, this.cryptogramme);


		this.chocosProduits = new LinkedList<ChocolatDeMarque>();
		this.journal.ajouter("Les Chocolats de marque sont :");
		for (ChocolatDeMarque cm : Filiere.LA_FILIERE.getChocolatsProduits()) {
			if ((Filiere.LA_FILIERE.getMarquesDistributeur().contains(cm.getMarque()) & cm.getGamme()!=Gamme.HQ )  || cm.getMarque().equals("CacaoFusion")){
				this.chocosProduits.add(cm);
				this.journal.ajouter("   - "+cm);
			}
		}
		this.stockChocoMarque = new HashMap<ChocolatDeMarque,Variable>();
		for (ChocolatDeMarque cm : this.chocosProduits) {
			this.stockChocoMarque.put(cm, new Variable("Eq5Stock "+cm, this, STOCKINITIAL));
			this.totalStocksChocoMarque.ajouter(this, STOCKINITIAL, this.cryptogramme);
			this.journal.ajouter("ajout de "+STOCKINITIAL+" tonnes de : "+cm+" au stock total de Chocolat de marque // stock total : "+this.totalStocksChocoMarque.getValeur(this.cryptogramme));
		}
		
		this.coutStockage = Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur()*4;
		
		this.VariationStockChocoMarque = new HashMap<ChocolatDeMarque,Double>();
		for (ChocolatDeMarque cm : this.chocosProduits) {
			this.VariationStockChocoMarque.put(cm, 0.0);
		
		}
		
		// Remplissage de pourcentageTransfo avec 0.1% de plus de cacao que le seuil minimal
		this.pourcentageTransfo = new HashMap<Feve, HashMap<Chocolat, Double>>();
		this.pourcentageTransfo.put(Feve.F_HQ_BE, new HashMap<Chocolat, Double>());
		double conversion = 0.1 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao HQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_HQ_BE).put(Chocolat.C_HQ_BE, conversion);// la masse de chocolat obtenue est plus importante que la masse de feve vue l'ajout d'autres ingredients
		
		this.pourcentageTransfo.put(Feve.F_MQ_E, new HashMap<Chocolat, Double>());
		conversion = 0.1 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao MQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_MQ_E).put(Chocolat.C_MQ_E, conversion);
		this.pourcentageTransfo.put(Feve.F_MQ, new HashMap<Chocolat, Double>());
		this.pourcentageTransfo.get(Feve.F_MQ).put(Chocolat.C_MQ, conversion);
		
		this.pourcentageTransfo.put(Feve.F_BQ, new HashMap<Chocolat, Double>());
		conversion = 0.1 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao BQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_BQ).put(Chocolat.C_BQ, conversion);
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ5";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////
	/**
	 * @Robin 
	 * @Erwann
	 */
	public void next() {
		this.journal.ajouter(" ===ETAPE = " + Filiere.LA_FILIERE.getEtape()+ " A L'ANNEE " + Filiere.LA_FILIERE.getAnnee()+" ===");
		this.journal.ajouter("=====STOCKS=====");
		this.journal.ajouter("prix stockage chez producteur : "+ Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur());
		for (Feve f : lesFeves) {
		this.journal.ajouter("Quantité en stock de feves " +f+ ": "+stockFeves.get(f).getValeur());
		}
		for (Chocolat c : lesChocolats) {
		this.journal.ajouter("Quantité en stock de Chocolat " +c+ ": "+stockChoco.get(c).getValeur());
		}
		for (ChocolatDeMarque cm : chocosProduits) {
		this.journal.ajouter("Quantité en stock de chocolat de marque " +cm+ ": " +stockChocoMarque.get(cm).getValeur());
		}
		this.journal.ajouter("stocks feves : "+this.totalStocksFeves.getValeur(this.cryptogramme));
		this.journal.ajouter("stocks chocolat : "+this.totalStocksChoco.getValeur(this.cryptogramme));
		this.journal.ajouter("stocks chocolat marque: "+this.totalStocksChocoMarque.getValeur(this.cryptogramme));
		
		// Paiment coûts de stockage, le stockage du chocolat de marque n'est pas encore operationnel donc on ne le prend pas en compte.
		// à rajouter pour choco marque : +this.totalStocksChocoMarque.getValeur(this.cryptogramme))
		Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Stockage", (this.totalStocksFeves.getValeur(this.cryptogramme)+this.totalStocksChoco.getValeur(this.cryptogramme))*this.coutStockage);
		
		// Transformation de tous les chocolats en chocolats de marque`avec une répartition équitable entre les marques
		for (Chocolat c : lesChocolats) {
			for (ChocolatDeMarque cm : chocosProduits) {
				if(c.getGamme() == cm.getGamme()) {
					double nbr_de_marque = chocosProduits.size();
					stockChocoMarque.get((ChocolatDeMarque) cm).ajouter(this, stockChoco.get(c).getValeur()/nbr_de_marque, this.cryptogramme);
					stockChoco.get((Chocolat) c).retirer(this, stockChoco.get(c).getValeur()/nbr_de_marque, this.cryptogramme);
					VariationStockChocoMarque.put(cm, stockChoco.get(c).getValeur()/nbr_de_marque);
				}
			}
		}
		//Regarder quantite a chaque step (a virer dans la version finale)
		//System.out.println("Step "+Filiere.LA_FILIERE.getEtape()+" on a "+this.getQuantiteEnStock(Feve.F_BQ, cryptogramme)+" t de feves BQ");
		//System.out.println("Step "+Filiere.LA_FILIERE.getEtape()+" on a "+this.getQuantiteEnStock(Chocolat.C_BQ, cryptogramme)+" t de chocolat BQ");
		//System.out.println("Step "+Filiere.LA_FILIERE.getEtape()+" on a "+this.getQuantiteEnStock(Feve.F_MQ, cryptogramme)+" t de feves MQ");
		//System.out.println("Step "+Filiere.LA_FILIERE.getEtape()+" on a "+this.getQuantiteEnStock(Chocolat.C_MQ, cryptogramme)+" t de chocolat MQ");

	}

	
	
	public Color getColor() {// NE PAS MODIFIER
		return new Color(165, 235, 195); 
	}

	public String getDescription() {
		return "Fuuuuuuusion";
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		for (Feve f : lesFeves) {
				res.add(this.stockFeves.get(f));
		}
		for (Chocolat c : lesChocolats) {
				res.add(this.stockChoco.get(c));
		}
		res.add(this.totalStocksChocoMarque);
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
		res.add(this.journal);
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
	
	/***
	 * @Robin
	 * @Erwann
	 */
	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			if (p.getType().equals("Feve")) {
				if (this.stockFeves.keySet().contains(p)) {
					return this.stockFeves.get(p).getValeur();
				} else {
					return 0.0;
				}
			} else if (p.getType().equals("Chocolat")) {
				if (this.stockChoco.keySet().contains(p)) {
					return this.stockChoco.get(p).getValeur();
				} else {
					return 0.0;
				}
			} else {
				if (this.stockChocoMarque.keySet().contains(p)) {
					return this.stockChocoMarque.get(p).getValeur();
				} else {
					return 0.0;
				}
			}
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}

	
	
	////////////////////////////////////////////////////////
	//        Déclaration de la marque CacaoFusion        //
	////////////////////////////////////////////////////////
	/**
	 * @Erwann
	 */
	public List<String> getMarquesChocolat() {
		LinkedList<String> marques = new LinkedList<String>();
		marques.add("CacaoFusion");
		return marques;
	}
	/**
	 * @Erwann
	 */
	public List<ChocolatDeMarque> getChocolatsProduits() {
		List<String> marquesDistributeurs = Filiere.LA_FILIERE.getMarquesDistributeur();
		if (this.chocosProduits == null) {
			this.chocosProduits = new LinkedList<ChocolatDeMarque>();
			for (Chocolat c : Chocolat.values()) {
				if (c.getGamme()!= Gamme.HQ) {
					int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
					this.chocosProduits.add(new ChocolatDeMarque(c, "CacaoFusion", pourcentageCacao));
					for (String marque : marquesDistributeurs) {
						this.chocosProduits.add(new ChocolatDeMarque(c, marque, pourcentageCacao));
					}
				}	
			}
		}
		return chocosProduits;
	}
}

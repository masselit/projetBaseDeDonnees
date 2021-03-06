package modele;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.InsertionScanner;
import exception.SQLWarningsExceptions;
import modele.type.Conferencier;
import modele.type.Salle;
import modele.type.Seminaire;
import requete.Requetes;

/**
 * 
 * @author Corentin Dervieux
 * @author Thibaut Masselin
 *
 */
public class Creation extends ActionSeminaire {


	@Override
	public void action(Connection conn) {
		creatSeminaire(conn);
	}
	
	/**
	 * Cr�ation d'un s�minaire
	 * @param conn
	 */
	private void creatSeminaire(Connection conn){
		//Cr�er un s�minaire avec :
		try {
			
			Seminaire seminaire = new Seminaire();
			
			//-------------------- Num�ro du S�minaire --------------------
			seminaire.setNumSeminaire(Requetes.selectMaxSeminaire(conn));
			seminaire.setEtatSemi("ATT");
			
			//-------------------- animateur --------------------		
			Requetes.affichePersonneSelect(conn,"ACT");
			seminaire.setNumAnimateur(InsertionScanner.saisirEntier("Choisir un num�ro d'animateur :"));
	        
			
			//-------------------- Titre S�minaire --------------------		
			seminaire.setLibelle(InsertionScanner.saisirString("Choisir un titre pour le S�minaire :"));
	        
			//-------------------- Th�me --------------------
			Requetes.afficheThemeSelect(conn);
			seminaire.setNumTheme(InsertionScanner.saisirEntier("Entrer le th�me selcetionner :"));
			//Propos� de cr�er un nouveau theme
			
	        //-------------------- date --------------------
			seminaire.setDate(InsertionScanner.DateStringOracle("D�terminer une date :"));
	        
	        //-------------------- journ�e --------------------
			seminaire.setDureeSemi(InsertionScanner.saisirEntier(0,2,"Choisir la dur�e du seminaire :(0 = matin | 1 = apr�s-midi | 2 = journ�e)"));
	        
			//-------------------- repat --------------------
			if(seminaire.getDureeSemi() == 2 || InsertionScanner.saisirEntier(0,1,"Voulez vous un repas :(0 = non | 1 = oui)")== 1){
				Requetes.afficheRepasSelect(conn);
				seminaire.setNumRepas(InsertionScanner.saisirEntier("Choisir un repas :"));
			}
			
			//-------------------- programme initial (activit�s) --------------------
	        Requetes.afficheActiviteSelect(conn);
	        seminaire.setLesActivites(InsertionScanner.activiteSelected(seminaire.getDureeSemi(),"Choisir les avtivit�es souhaiter :"));
	        
			//� le cas �ch�ant, le ou les conf�renciers, avec titre, transparents (dans les d�lais pr�vus),tarif de la prestation
			
	        this.ajouterDesConferencier(conn,seminaire);

			
			//-------------------- nombre de places --------------------
			seminaire.setNombrePlace(InsertionScanner.saisirEntier("D�finir le nombre de personne maximum qui vous semble adapter au s�minaire:"));
	        
			//-------------------- tarif de l'inscription --------------------
			seminaire.setPrixUnePlace(InsertionScanner.saisirDecimal("D�finir un prix pour une place :"));
	        
			//-------------------- prestataire --------------------
	        Requetes.affichePrestataireSelect(conn, seminaire.getDateToString());
	        seminaire.setNumPerstataire(InsertionScanner.saisirEntier("Choisir un prestataire :"));  
	        
	        //-------------------- Salle ------------------------
	        List<Salle> lesSalles = Requetes.selectLesSalles(conn, seminaire);
			List<Integer> lesIdSalle = null;
			System.out.println("Les Salles disponible :");
			for(Salle s : lesSalles){
				System.out.println(s.toString());
				lesIdSalle.add(s.getNumSalle());
			}
			seminaire.setNumSeminaire(InsertionScanner.saisirEntier(lesIdSalle, true, "Saisir le num�ro d'une Salle :"));

			//-------------------- Pause ------------------------
			Requetes.afficheLesPause(conn, seminaire.getNumPerstataire());
			List<Integer> lesPauses = ajouterDesPauses(seminaire.getDureeSemi());
			
			
			//-------------------- total des recettes pr�vus (min, max) --------------------
	        seminaire.setRecettePrevuMin(InsertionScanner.saisirDecimal("D�finir une recette minimal :"));
	        seminaire.setRecettePrevuMax(InsertionScanner.saisirDecimal("D�finir une recette maximal :"));
	        
	        //-------------------- total des d�penses pr�vus (min, max)--------------------
	        seminaire.setDepencePrevuMin(InsertionScanner.saisirDecimal("D�finir une d�pence minimal :"));
	        seminaire.setDepencePrevuMax(InsertionScanner.saisirDecimal("D�finir une d�pence maximal :"));
	        
	        // INSERT INTO
	        
	        
	        Requetes.insertSeminaire(conn,seminaire);
	        Requetes.insertOrganise(conn, seminaire);
	        
	        if(seminaire.getLesConferenciers().size() > 0){
	        	this.insertionDesConf�rencier(conn,seminaire);
	        }
	        
	        this.insertionPause(conn, lesPauses, seminaire.getNumSeminaire());
	        
	        //Prevue
	        this.insertionActiviter(conn, seminaire);
	        
		} catch (SQLException e) {
			System.err.println("Erreur base de donn�es : "+ e.getMessage());
			SQLWarningsExceptions.printExceptions(e);
		}
	}
	
	/**
	 * Permet de cr�er des conf�renciers
	 * @param conn
	 * @param seminaire
	 * @throws SQLException
	 */
	private void ajouterDesConferencier(Connection conn,Seminaire seminaire) throws SQLException{
		List<Conferencier> lesConferenciers =  new ArrayList<>();
		while(InsertionScanner.saisirEntier(0,1," - 0 = Arr�ter d'ajouter des conf�renciers \n"
											  + " - 1 = Ajouter un nouveau conf�rencier ") == 0){
			System.out.println(" -- Ajoute nouveau conf�rencier : -- ");
			Requetes.affichePersonneSelect(conn,"CON");
			int numConferencier = InsertionScanner.saisirEntier("Entrer le num�ro du conf�rencier :");
			String titre = InsertionScanner.saisirString("Entrer le titre de la conf�rence :");
			String  transparents = InsertionScanner.saisirString("Entrer les transparents :");
			float prixDePrestation = InsertionScanner.saisirDecimal("Le prix de la prestation :");
			
			lesConferenciers.add(new Conferencier(numConferencier,titre,transparents,prixDePrestation));
		}
		seminaire.addAllLesConferenciers(lesConferenciers);
		// Pour savoir si il y a quelque chose � ajouter
	}
	
	/**
	 * Ajouter une liste de conf�rencier
	 * @param conn
	 * @param seminaire
	 * @throws SQLException
	 */
	private void insertionDesConf�rencier(Connection conn, Seminaire seminaire) throws SQLException{
		for(Conferencier conferencier : seminaire.getLesConferenciers()){
			Requetes.insertFaitUneConf(conn, seminaire.getNumSeminaire(), conferencier);
		}
	}
	
	/**
	 * Ajouter une liste d'activiter
	 * @param conn
	 * @param seminaire
	 * @throws SQLException
	 */
	private void insertionActiviter(Connection conn, Seminaire seminaire) throws SQLException{
		for(Integer i : seminaire.getLesActivites()){
			Requetes.insertUneActiviter(conn,seminaire.getNumSeminaire(),i);
		}
	}
	
	/**
	 *  liste des pause
	 * @param conn
	 * @param seminaire
	 * @throws SQLException
	 */
	private void insertionPause(Connection conn, List<Integer> lesPauses,int numSeminaire) throws SQLException{
		for(Integer i : lesPauses){
			Requetes.insertUnePause(conn, i, numSeminaire);
		}
	}
	
	/**
	 * Permet de saisir une ou plusieurs pauses
	 * @param typeJour
	 * @return
	 */
	private List<Integer> ajouterDesPauses(int typeJour){
		List<Integer> inte = new ArrayList<>();
		int nbPauseMax = 1;
		if(typeJour != 2){nbPauseMax = 2;}
		for(int i=0;i<nbPauseMax;i++){
			inte.add(InsertionScanner.saisirEntier("Saisir l'id d'une pause :"));
		}
		return inte;
	}
	
}

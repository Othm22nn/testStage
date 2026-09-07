package com.skytrace.bagages;

/**
 * Les 6 etapes du parcours d'un bagage, dans l'ordre exact.
 * Stocke en base comme texte (colonne "statut" de la table Bagage).
 */
public enum StatutBagage {
    ENREGISTREMENT,
    DEPOT_TAPIS,
    TRI_TRANSFERT,
    CHARGEMENT,
    DECHARGEMENT,
    LIVRAISON;

    /**
     * Retourne l'etape suivante dans le parcours, ou null si deja a la derniere etape (LIVRAISON).
     */
    public StatutBagage suivante() {
        StatutBagage[] valeurs = values();
        int indexSuivant = this.ordinal() + 1;
        if (indexSuivant >= valeurs.length) {
            return null;
        }
        return valeurs[indexSuivant];
    }

    public boolean estDerniereEtape() {
        return this == LIVRAISON;
    }

    /**
     * Libelle francais lisible, utilise pour remplir la colonne texte "point_scan" de la table Scan.
     */
    public String libelle() {
        return switch (this) {
            case ENREGISTREMENT -> "Enregistrement";
            case DEPOT_TAPIS -> "Depot sur tapis";
            case TRI_TRANSFERT -> "Tri & transfert";
            case CHARGEMENT -> "Chargement";
            case DECHARGEMENT -> "Dechargement";
            case LIVRAISON -> "Livraison";
        };
    }
}

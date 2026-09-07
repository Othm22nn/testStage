package com.skytrace.bagages;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatutBagageTest {

    @Test
    void doitParcourirToutesLesEtapesDansLOrdre() {
        assertThat(StatutBagage.ENREGISTREMENT.suivante()).isEqualTo(StatutBagage.DEPOT_TAPIS);
        assertThat(StatutBagage.DEPOT_TAPIS.suivante()).isEqualTo(StatutBagage.TRI_TRANSFERT);
        assertThat(StatutBagage.TRI_TRANSFERT.suivante()).isEqualTo(StatutBagage.CHARGEMENT);
        assertThat(StatutBagage.CHARGEMENT.suivante()).isEqualTo(StatutBagage.DECHARGEMENT);
        assertThat(StatutBagage.DECHARGEMENT.suivante()).isEqualTo(StatutBagage.LIVRAISON);
        assertThat(StatutBagage.LIVRAISON.suivante()).isNull();
        assertThat(StatutBagage.LIVRAISON.estDerniereEtape()).isTrue();
    }
}

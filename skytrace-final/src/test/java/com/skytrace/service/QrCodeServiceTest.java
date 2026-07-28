package com.skytrace.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeServiceTest {

    private final QrCodeService service = new QrCodeService();

    @Test
    void doitGenererUnCodeAuFormatAttenduEtUneImagePng() {
        String code = service.genererCodeUnique(12);

        assertThat(code).matches("BAG-0012-[A-F0-9]{4}");
        assertThat(service.genererQrCodeBase64(code)).isNotBlank();
    }

    @Test
    void deuxGenerationsDoiventProduireDesCodesDifferents() {
        assertThat(service.genererCodeUnique(1)).isNotEqualTo(service.genererCodeUnique(1));
    }
}

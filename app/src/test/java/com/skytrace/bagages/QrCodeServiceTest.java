package com.skytrace.bagages;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeServiceTest {

    private final QrCodeService service = new QrCodeService();

    @Test
    void doitGenererUnCodeAuFormatAttenduEtUneImagePng() {
        String code = service.genererCodeUnique();

        assertThat(code).matches("BAG-[A-F0-9]{32}");
        assertThat(service.genererQrCodeBase64(code)).isNotBlank();
    }

    @Test
    void deuxGenerationsDoiventProduireDesCodesDifferents() {
        assertThat(service.genererCodeUnique()).isNotEqualTo(service.genererCodeUnique());
    }
}

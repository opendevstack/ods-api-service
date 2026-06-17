package org.opendevstack.apiservice.marketplacemetrics.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MarketplaceMetricsMapperTest {

    @InjectMocks
    private MarketplaceMetricsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MarketplaceMetricsMapper();
    }

    @Test
    void fakeTest() {
        assertThat(true).isTrue();
    }

}


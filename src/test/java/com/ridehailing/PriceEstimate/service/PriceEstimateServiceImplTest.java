package com.ridehailing.PriceEstimate.service;

import com.ridehailing.PriceEstimate.model.PriceEstimate;
import com.ridehailing.PriceEstimate.repository.PriceEstimateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceEstimateServiceImplTest {

    @Mock
    PriceEstimateRepository repo;

    @InjectMocks
    PriceEstimateServiceImpl service;

    private void repoEchoesSavedEntity(){
        when(repo.save(any(PriceEstimate.class))).thenAnswer(inv->inv.getArgument(0));
    }

    @ParameterizedTest(name="{0}km costs {1}")
    @CsvSource({
            "0, 5.00",
            "1, 7.50",
            "2, 10.00",
            "4, 15.00",
            "10, 30.00",
            "100, 255.00"
    })

    @DisplayName("calculatePrice: price is 5 + 2.5 per km")
    void calculatePrice(String distance,String expected) {
        repoEchoesSavedEntity();

        PriceEstimate result=service.calculatePrice(1L, new BigDecimal(distance));

        assertThat(result.getPrice()).isEqualByComparingTo(expected);

    }

    @Test
    void calculatePrice_passesBuiltEstimateToRepository() {
        repoEchoesSavedEntity();
        ArgumentCaptor<PriceEstimate> captor = ArgumentCaptor.forClass(
                PriceEstimate.class);
        service.calculatePrice(77L, new BigDecimal("4"));
        verify(repo).save(captor.capture());
        PriceEstimate saved = captor.getValue();
        assertThat(saved.getRideId()).isEqualTo(77L);
        assertThat(saved.getPrice()).isEqualByComparingTo("15.00");
        assertThat(saved.getDistance()).isEqualByComparingTo("4");
    }

    @Test
    @DisplayName("calculatePrice: returns the base fare when the distance is zero")
    void returnsBaseFare_whenDistanceIsZero() {
        repoEchoesSavedEntity();

        PriceEstimate result=service.calculatePrice(11L,new BigDecimal("0"));

        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("5"));
    }


    @Test
    void findByRideId_returnsEstimate_whenPresent() {
        PriceEstimate estimate = new PriceEstimate();
        estimate.setRideId(5L);
        when(repo.findByRideId(5L)).thenReturn(Optional.of(estimate));
        assertThat(service.findByRideId(5L)).isSameAs(estimate);
    }

    @Test
    void findByRideId_throws_whenMissing() {
        when(repo.findByRideId(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByRideId(404L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ride not found");
    }

    @Test
    @DisplayName("KNOWN ISSUE: a negative distance produces a negative fare")
    void calculatePrice_producesNegativeFare_forNegativeDistance_knownIssue() {
        repoEchoesSavedEntity();
        PriceEstimate result = service.calculatePrice(1L, new BigDecimal("-5"));
        assertThat(result.getPrice().toPlainString()).isEqualTo("-7.50");
    }

    @Test
    @DisplayName("KNOWN ISSUE: a null distance throws NullPointerException before anything is saved")
    void calculatePrice_throwsNpe_forNullDistance_knownIssue() {
        assertThatThrownBy(() -> service.calculatePrice(1L, null))
                .isInstanceOf(NullPointerException.class);
    }
    @Test
    @DisplayName("boundary: a very large distance does not overflow")
    void calculatePrice_handlesVeryLargeDistance() {
        repoEchoesSavedEntity();
        PriceEstimate result = service.calculatePrice(1L, new BigDecimal("100000"))
                ;
        assertThat(result.getPrice().toPlainString()).isEqualTo("250005.00");
    }
}
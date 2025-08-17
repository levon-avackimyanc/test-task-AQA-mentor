package apiTests;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import service.DeliveryCalc;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static service.DeliveryCalc.calcDeliveryCost;

public class DeliveryCalcTest {

    public static Stream<Arguments> dataForDistTest() {
        return Stream.of(
                Arguments.of(0.5, 150),
                Arguments.of(2, 150),
                Arguments.of(2.1, 200),
                Arguments.of(10, 200),
                Arguments.of(10.1, 300),
                Arguments.of(30, 300),
                Arguments.of(30.1, 400));

    }

    @ParameterizedTest(name = "distance={0} км → надбавка корректна, small, not fragile → ожидаем {1} руб (без коэфф.)")
    @MethodSource(value = "dataForDistTest")
    void baseSumsByDistance(double distance, int baseSum) {
        var cost = calcDeliveryCost(distance, "small", false, "default");
        int expected = Math.max(baseSum, 400);
        assertEquals(baseSum, DeliveryCalc.getBaseCost());
        // из-за минималки 400 — проверим, что если стоимость доставки < 400, вернётся 400
        assertEquals(expected, cost);
    }


    @Test
    @DisplayName("Хрупкий груз на > 30 км → IllegalArgumentException")
    void fragileOver30Exception() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calcDeliveryCost(30.01, "small", true, "default")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("хрупкие грузы нельзя перевозить"));
    }

    @Test
    @DisplayName("Хрупкий груз в пределах допустимого расстояния")
    void fragileWithinLimit() {
        // distance 5 → 100, size small → 100, fragile → 300 ; итого 500
        var cost = calcDeliveryCost(5.0, "small", true, "default");
        assertEquals(500, cost);
    }

    @ParameterizedTest(name = "Регистронезависимая обработка габаритов груза c размером {0}")
    @CsvSource(value = {"LARGE", "SmaLL"})
    void SizeCaseInsensitive(String size) {
        var lower = calcDeliveryCost(5.0, size.toLowerCase(), true, "increased");
        var upper = calcDeliveryCost(5.0, size, true, "increased");
        assertEquals(lower, upper);
    }

    @Test
    @DisplayName("Некорректный размер → IllegalArgumentException")
    void invalidSizeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> calcDeliveryCost(10.0, "medium", false, "default"));
    }

    @ParameterizedTest(name = "load={0} → коэффициент применён корректно")
    @CsvSource({
            // baseCost = 300 {distance(>30)} + 200 {size(big)} + 0 {fragile(false)} = 500
            "increased, 600",   // 500 * 1.2 = 600
            "high,      700",   // 500 * 1.4 = 700
            "very_high, 800",   // 500 * 1.6 = 800
            "default,     500"    // 500 * 1.0 = 500
    })
    void loadFactorsApplied(String load, double expected) {
        var cost = calcDeliveryCost(31.0, "large", false, load);
        assertEquals(expected, cost);
    }

    @Test
    @DisplayName("Если меньше сумма доставки < 400 → возвращаем 400")
    void minCostApplied() {
        var cost = calcDeliveryCost(1., "small", false, "default");
        assertEquals(400, cost);
    }
}

package service;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
public class DeliveryCalc {


    private static final HashMap<String, Double> serviceLoadMap = new HashMap<>();
    @Getter
    private static Double baseCost;

    static {
        serviceLoadMap.put("increased", 1.2);
        serviceLoadMap.put("high", 1.4);
        serviceLoadMap.put("very_high", 1.6);
    }

    enum PackageSize {
        LARGE,
        SMALL
    }

    private static final double MIN_DELIVERY_COST = 400;

    public static Double calcDeliveryCost(Double distanceKm, String size, Boolean fragile, String serviceLoad) {
        baseCost = 0.0;

        if (distanceKm > 30) {
            if (fragile) {
                throw new IllegalArgumentException("Хрупкие грузы нельзя перевозить дальше 30 км");
            }
            baseCost += 300;
        } else if (distanceKm > 10) {
            baseCost += 200;
        } else if (distanceKm > 2) {
            baseCost += 100;
        } else {
            baseCost += 50;
        }

        if (size.equalsIgnoreCase(String.valueOf(PackageSize.LARGE))) {
            baseCost += 200;
        } else if (size.equalsIgnoreCase(String.valueOf(PackageSize.SMALL))) {
            baseCost += 100;
        } else {
            throw new IllegalArgumentException("Некорректный параметр размера: " + size);
        }

        if (fragile) baseCost += 300;

        if (serviceLoadMap.containsKey(serviceLoad)) baseCost *= serviceLoadMap.get(serviceLoad);

        return baseCost > MIN_DELIVERY_COST ? baseCost : MIN_DELIVERY_COST;
    }

}


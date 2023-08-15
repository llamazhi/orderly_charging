package com.example.charging.interactive;

import com.example.charging.entity.EVData;
import com.example.charging.utils.Utils;

import java.util.Objects;

public class OrderlyChargingController {
    private final Utils utils = new Utils();
    public OrderlyChargingController() {}

    public EVData createNewEV(double maxSOC, double chargingPower, double remainingSOC, double chargingStartingTime,
                              double leavingTime, boolean ifAcceptOrderlyCharging) {
        EVData ev = new EVData();
        ev.setMaxSOC(maxSOC);
        ev.setChargingPower(chargingPower);
        ev.setRemainingSOC(remainingSOC);
        ev.setChargingStartTime(chargingStartingTime);
        ev.setLeavingTime(leavingTime);
        ev.setIfAcceptOrderlyCharging(ifAcceptOrderlyCharging);
        double chargeTime = (maxSOC * (1 - remainingSOC / 100)) / chargingPower;
        ev.setChargingTime(chargeTime);
        ev.setAlreadyOptimized(false);
        ev.setChargingEndTime(utils.convertTimeToNextDay(chargingStartingTime + chargeTime));
        return ev;
    }

    public static void main(String[] args) {
        double maxSOC = Double.parseDouble(args[0]);
        double chargingPower = Double.parseDouble(args[1]);
        double remainingSOC = Double.parseDouble(args[2]);
        double chargingStartingTime = Double.parseDouble(args[3]);
        double leavingTime = Double.parseDouble(args[4]);
        boolean ifAcceptOrderlyCharging = Objects.equals(args[5], "yes");

        OrderlyChargingController ocController = new OrderlyChargingController();
        EVData ev = ocController.createNewEV(maxSOC, chargingPower, remainingSOC, chargingStartingTime,
                leavingTime, ifAcceptOrderlyCharging);
        OrderlyChargingClient ocClient = new OrderlyChargingClient();
        ocClient.sendEVData(ev);
    }
}
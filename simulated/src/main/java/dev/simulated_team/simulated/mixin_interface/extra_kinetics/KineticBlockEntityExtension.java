package dev.simulated_team.simulated.mixin_interface.extra_kinetics;

public interface KineticBlockEntityExtension {

    void simulated$setValidationCountdown(int validationCountdown);

    void simulated$setConnectedToExtraKinetics(boolean connectedToExtraKinetics);

    boolean simulated$getConnectedToExtraKinetics();

}

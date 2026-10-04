package siren.controller;

import net.fabricmc.api.ModInitializer;

public final class SirenMod implements ModInitializer {
    public static final String MOD_ID = "siren_controller";

    @Override
    public void onInitialize() {
        SirenSounds.register();
        SirenBlocks.register();
        SirenBlockEntities.register();
        SirenNetworking.registerCommon();
    }
}

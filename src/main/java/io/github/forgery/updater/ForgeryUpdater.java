package io.github.forgery.updater;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(name = ForgeryUpdater.MOD_NAME, version = ForgeryUpdater.VERSION, modid = ForgeryUpdater.MOD_ID)
public class ForgeryUpdater {

    public static final String MOD_ID = "forgeryupdater";
    public static final String MOD_NAME = "Forgery UpdateChecker";
    public static final String VERSION = "0.0.1";

    @Mod.Instance(MOD_ID)
    public static ForgeryUpdater INSTANCE;

    @Mod.EventHandler
    protected void onPreInit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    protected void onInit(FMLInitializationEvent event) {

    }


    @Mod.EventHandler
    protected void onPostInit(FMLPostInitializationEvent event) {
    }
}
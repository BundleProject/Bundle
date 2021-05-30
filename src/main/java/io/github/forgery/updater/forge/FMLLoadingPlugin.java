package io.github.forgery.updater.forge;

import io.github.forgery.updater.UpdateChecker;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class FMLLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        UpdateChecker.updateMods();
        return new String[]{};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
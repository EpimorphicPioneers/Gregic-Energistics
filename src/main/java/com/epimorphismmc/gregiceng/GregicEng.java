package com.epimorphismmc.gregiceng;

import com.epimorphismmc.gregiceng.client.ClientProxy;
import com.epimorphismmc.gregiceng.common.CommonProxy;
import com.epimorphismmc.gregiceng.config.GEConfigHolder;
import com.epimorphismmc.gregiceng.data.lang.GELangHandler;
import com.epimorphismmc.monomorphism.MOMod;
import com.epimorphismmc.monomorphism.datagen.MOProviderTypes;
import com.epimorphismmc.monomorphism.registry.registrate.MORegistrate;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.networking.INetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(GregicEng.MODID)
public class GregicEng extends MOMod<CommonProxy> {
    public static final String MODID = "gregiceng";
    public static final String NAME = "Gregic Energistics";

    public static GregicEng instance;

    public GregicEng() {
        super();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, FormattingUtil.toLowerCaseUnder(path));
    }

    public static Logger logger() {
        return instance.getLogger();
    }

    public static CommonProxy proxy() {
        return instance.getProxy();
    }

    public static MORegistrate registrate() {
        return instance.getRegistrate();
    }

    public static INetworking network() {
        return instance.getNetwork();
    }

    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public String getModName() {
        return NAME;
    }

    @Override
    protected void onModConstructed() {
        instance = this;
        GEConfigHolder.init();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected CommonProxy createClientProxy() {
        return new ClientProxy();
    }

    @Override
    @OnlyIn(Dist.DEDICATED_SERVER)
    protected CommonProxy createServerProxy() {
        return new CommonProxy();
    }

    @Override
    public void addDataGenerator(MORegistrate registrate) {
        registrate.addDataGenerator(MOProviderTypes.MO_LANG, GELangHandler::init);
    }

}

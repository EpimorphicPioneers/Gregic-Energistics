package com.epimorphismmc.gregiceng.api.misc;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.me.ManagedGridNode;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigurableAESlot<T extends AEKey> implements IConfigurableAESlot<T>, ITagSerializable<CompoundTag>, IContentChangeAware {
    protected final static String CONFIG_TAG = "config";
    @Getter @Setter
    protected Runnable onContentsChanged = () -> {};
    @Getter
    protected T config;

    public ConfigurableAESlot() {/**/}

    public ConfigurableAESlot(T config) {
        this.config = config;
    }

    protected abstract ManagedGridNode getNode();
    protected abstract IActionSource getActionSource();

    @Override
    public long request(long amount, boolean simulate) {
        if (config != null && getNode().isActive()) {
            var grid = getNode().getGrid();
            if (grid != null) {
                var storage = grid.getStorageService().getInventory();
                return storage.extract(config, amount, Actionable.ofSimulate(simulate), getActionSource());
            }
        }
        return 0;
    }

    @Override
    public void setConfig(@Nullable T config) {
        this.config = config;
        onContentsChanged.run();
    }
}

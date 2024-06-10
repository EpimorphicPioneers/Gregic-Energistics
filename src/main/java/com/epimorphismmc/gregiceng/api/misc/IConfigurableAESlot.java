package com.epimorphismmc.gregiceng.api.misc;

import appeng.api.stacks.AEKey;

import org.jetbrains.annotations.Nullable;

public interface IConfigurableAESlot<T extends AEKey> {
    @Nullable T getConfig();

    void setConfig(@Nullable T what);

    default long getAmount() {
        return request(Long.MAX_VALUE, true);
    }

    long request(long amount, boolean simulate);

    IConfigurableAESlot<T> copy();
}

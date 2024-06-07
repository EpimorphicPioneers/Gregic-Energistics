package com.epimorphismmc.gregiceng.api.misc;

import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class SerializableFluidTransferList extends FluidTransferList implements IContentChangeAware {
    @Getter
    @Setter
    protected Runnable onContentsChanged = () -> {};

    public SerializableFluidTransferList(IFluidTransfer... transfers) {
        super(transfers);
    }

    public SerializableFluidTransferList(List<IFluidTransfer> transfers) {
        super(transfers);
    }
}

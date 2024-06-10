package com.epimorphismmc.gregiceng.api.gui.wight;

import com.epimorphismmc.gregiceng.api.misc.IConfigurableAESlot;

import appeng.api.stacks.AEKey;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class AESlotWidget<T extends AEKey> extends Widget {
    @Setter
    protected BiConsumer<AESlotWidget<T>, List<Component>> onAddedTooltips;

    protected IConfigurableAESlot<T> aeSlot;
    protected T key;
    protected long amount;

    public AESlotWidget(IConfigurableAESlot<T> aeSlot, Position selfPosition) {
        super(selfPosition, new Size(18, 18));
        this.aeSlot = aeSlot;
    }

    public AESlotWidget(IConfigurableAESlot<T> aeSlot, int x, int y) {
        this(aeSlot, new Position(x, y));
    }

    @Override
    public final void setSize(Size size) {
        // you cant modify size.
    }

    protected abstract void fromPacket(@Nullable FriendlyByteBuf buf);

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        this.key = aeSlot.getConfig();
        this.amount = aeSlot.getAmount();
        if (key != null && amount > 0) {
            buffer.writeBoolean(true);
            key.writeToPacket(buffer);
            buffer.writeVarLong(amount);
        } else {
            buffer.writeBoolean(false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        if (buffer.readBoolean()) {
            fromPacket(buffer);
            this.amount = buffer.readVarLong();
        } else {
            fromPacket(null);
            this.amount = 0;
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        var newKey = aeSlot.getConfig();
        var newAmount = aeSlot.getAmount();
        // TODO 是否用equal
        if (newKey != key || amount != newAmount) {
            this.key = newKey;
            this.amount = newAmount;
            writeUpdateInfo(1, buffer -> {
                if (key != null && amount > 0) {
                    buffer.writeBoolean(true);
                    key.writeToPacket(buffer);
                    buffer.writeVarLong(amount);
                } else {
                    buffer.writeBoolean(false);
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            if (buffer.readBoolean()) {
                fromPacket(buffer);
                this.amount = buffer.readVarLong();
            } else {
                fromPacket(null);
                this.amount = 0;
            }
        }
    }

    protected List<Component> getToolTips(List<Component> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }
        return list;
    }
}

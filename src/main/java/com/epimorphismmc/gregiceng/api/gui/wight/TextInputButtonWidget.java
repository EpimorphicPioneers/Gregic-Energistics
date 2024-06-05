package com.epimorphismmc.gregiceng.api.gui.wight;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

public class TextInputButtonWidget extends WidgetGroup {

    @Setter
    @Accessors(chain = true)
    private Consumer<String> onConfirm;
    @Setter
    @Accessors(chain = true)
    @Getter
    private String text = "";
    @Getter
    private boolean isInputting;
    private TextFieldWidget textField;

    public TextInputButtonWidget() {
    }

    public TextInputButtonWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public TextInputButtonWidget(Position position) {
        super(position);
    }

    public TextInputButtonWidget(Position position, Size size) {
        super(position, size);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.addWidget(new ToggleButtonWidget(getSizeWidth() - getSizeHeight(), 0,
                getSizeHeight(), getSizeHeight(),
                this::isInputting, pressed -> {
            isInputting = pressed;
            if (pressed) {
                this.textField = new TextFieldWidget(0, 0, getSizeWidth() - getSizeHeight() - 2, getSizeHeight(), this::getText, this::setText);
                this.addWidget(textField);
            } else {
                onConfirm.accept(text);
                this.removeWidget(textField);
            }
        }).setTexture(
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✎")),
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✎"))
        ));
    }
}

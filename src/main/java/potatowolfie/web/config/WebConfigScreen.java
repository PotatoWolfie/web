package potatowolfie.web.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import potatowolfie.web.config.WebConfig;

public class WebConfigScreen extends Screen {

    private final Screen parent;
    private EditBox rarityField;

    private int rarityValue;

    public WebConfigScreen(Screen parent) {
        super(Component.translatable("web.config.title"));
        this.parent = parent;
        this.rarityValue = WebConfig.get().spiderEggClusterRarity;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldWidth = 100;

        this.rarityField = new EditBox(
                this.font,
                centerX - fieldWidth / 2,
                80,
                fieldWidth,
                20,
                Component.translatable("web.config.spider_egg_cluster_rarity")
        );
        this.rarityField.setValue(String.valueOf(this.rarityValue));
        this.rarityField.setTextColor(0xFFFFFFFF);
        this.rarityField.setResponder(this::onRarityFieldChanged);
        this.addRenderableWidget(this.rarityField);

        this.addRenderableWidget(
                Button.builder(Component.translatable("web.config.reset_to_default"), button -> this.onResetToDefault())
                        .bounds(centerX - 100, this.height - 54, 200, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                        .bounds(centerX - 100, this.height - 28, 96, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("web.config.save_and_exit"), button -> this.onDone())
                        .bounds(centerX + 4, this.height - 28, 96, 20)
                        .build()
        );
    }

    private void onRarityFieldChanged(String value) {
        boolean valid = isValidRarityInput(value);
        this.rarityField.setTextColor(valid ? 0xFFFFFFFF : 0xFFFF5555);
    }

    private boolean isValidRarityInput(String value) {
        if (value.isEmpty()) return false;
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void onResetToDefault() {
        this.rarityField.setValue(String.valueOf(WebConfig.DEFAULT_SPIDER_EGG_CLUSTER_RARITY));
    }

    private void onDone() {
        try {
            int parsed = Integer.parseInt(this.rarityField.getValue());
            if (parsed > 0) {
                WebConfig.get().spiderEggClusterRarity = parsed;
                WebConfig.save();
            }
        } catch (NumberFormatException ignored) {
        }
        this.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);

        graphics.centeredText(
                this.font,
                Component.translatable("web.config.spider_egg_cluster_rarity"),
                this.width / 2,
                65,
                0xFFFFFFFF
        );

        graphics.centeredText(
                this.font,
                Component.translatable("web.config.spider_egg_cluster_rarity.hint"),
                this.width / 2,
                105,
                0xFFFFFFFF
        );
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
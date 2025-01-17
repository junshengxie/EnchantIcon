package mods.enchanticon.forge;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import mods.enchanticon.enums.ApplyingScope;
import mods.enchanticon.enums.BackgroundType;
import mods.enchanticon.enums.LevelMarkType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class ModConfigScreenImpl {

    public static Screen create(Minecraft mc, Screen previous) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(previous)
                .setTitle(new TranslatableComponent("enchant_icon.config"));
        ConfigCategory generalCategory = builder.getOrCreateCategory(new TranslatableComponent("enchant_icon.config.general"));
        generalCategory.addEntry(builder.entryBuilder()
                .startEnumSelector(new TranslatableComponent("enchant_icon.config.background_type"), BackgroundType.class, ModConfigImpl.backgroundTypeProp.get())
                .setDefaultValue(BackgroundType.STICKY_NOTE)
                .setTooltip(new TranslatableComponent("enchant_icon.config.background_type.tooltip"))
                .setSaveConsumer(newValue -> ModConfigImpl.backgroundTypeProp.set(newValue))
                .build());
        generalCategory.addEntry(builder.entryBuilder()
                .startEnumSelector(new TranslatableComponent("enchant_icon.config.level_mark_type"), LevelMarkType.class, ModConfigImpl.levelTypeProp.get())
                .setDefaultValue(LevelMarkType.DIGIT_COLORED)
                .setTooltip(new TranslatableComponent("enchant_icon.config.level_mark_type.tooltip"))
                .setSaveConsumer(newValue -> ModConfigImpl.levelTypeProp.set(newValue))
                .build());
        generalCategory.addEntry(builder.entryBuilder()
                .startEnumSelector(new TranslatableComponent("enchant_icon.config.scope_for_gui_like"), ApplyingScope.class, ApplyingScope.ENCHANTED_BOOK_ONLY)
                .setDefaultValue(ApplyingScope.ENCHANTED_BOOK_ONLY)
                .setTooltip(new TranslatableComponent("enchant_icon.config.scope_for_gui_like.tooltip"))
                .setSaveConsumer(newValue -> ModConfigImpl.applyingScopeInGuiProp.set(newValue))
                .build());
        generalCategory.addEntry(builder.entryBuilder()
                .startEnumSelector(new TranslatableComponent("enchant_icon.config.scope_for_hand_held"), ApplyingScope.class, ApplyingScope.ENCHANTED_BOOK_ONLY)
                .setDefaultValue(ApplyingScope.ENCHANTED_BOOK_ONLY)
                .setTooltip(new TranslatableComponent("enchant_icon.config.scope_for_hand_held.tooltip"))
                .setSaveConsumer(newValue -> ModConfigImpl.applyingScopeInHandProp.set(newValue))
                .build());
        return builder.build();
    }
}

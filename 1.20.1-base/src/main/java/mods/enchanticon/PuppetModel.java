package mods.enchanticon;

import mods.enchanticon.enums.ApplyingScope;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A special {@link ModelWithSeparateTransform} implementation, where its {@link #applyTransform(ItemDisplayContext)}
 * is controlled by external configuration (thus the name "puppet").
 */
public class PuppetModel implements BakedModel, ModelWithSeparateTransform {

    public interface Factory {
        public static final class Holder {
            public static Factory impl = PuppetModel::new;
        }

        BakedModel create(BakedModel base, BakedModel override, boolean isEnchantedBook);

        static BakedModel createFrom(BakedModel base, BakedModel override, boolean isEnchantedBook) {
            return Holder.impl.create(base, override, isEnchantedBook);
        }
    }

    public static final Set<ItemDisplayContext> GUI_LIKE = EnumSet.of(
            ItemDisplayContext.GUI,
            ItemDisplayContext.FIXED
    );

    public static final Set<ItemDisplayContext> HAND_HELD_LIKE = EnumSet.of(
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
            ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
    );

    private final BakedModel base, override;
    private final boolean isEnchantedBook;

    public PuppetModel(BakedModel base, BakedModel override, boolean isEnchantedBook) {
        this.base = base;
        this.override = override;
        this.isEnchantedBook = isEnchantedBook;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
        return this.base.getQuads(blockState, direction, randomSource);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.base.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.base.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.base.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.base.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.base.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.base.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.base.getOverrides();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType) {
        if (this.isEnchantedBook) {
            if (GUI_LIKE.contains(transformType)) {
                return EnchantIconConfig.instance.guiScope != ApplyingScope.NONE ? this.override : this.base;
            } else if (HAND_HELD_LIKE.contains(transformType)) {
                return EnchantIconConfig.instance.inHandScope != ApplyingScope.NONE ? this.override : this.base;
            } else {
                return this.base;
            }
        } else {
            if (GUI_LIKE.contains(transformType)) {
                return EnchantIconConfig.instance.guiScope == ApplyingScope.ALL ? this.override : this.base;
            } else if (HAND_HELD_LIKE.contains(transformType)) {
                return EnchantIconConfig.instance.inHandScope == ApplyingScope.ALL ? this.override : this.base;
            } else {
                return this.base;
            }
        }
    }
}

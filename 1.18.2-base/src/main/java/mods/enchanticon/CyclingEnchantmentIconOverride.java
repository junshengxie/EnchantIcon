package mods.enchanticon;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CyclingEnchantmentIconOverride extends ItemOverrides {

    private final Map<String, BakedModel> bg;
    private final Map<ResourceLocation, BakedModel> enchantIcons;
    private final Map<String, BakedModel> defaultLevelMarks;
    private final Map<String, Map<String, BakedModel>> levelMarks;

    private final Map<AssembledKey, BakedModel> cache = new HashMap<>();

    public CyclingEnchantmentIconOverride(Map<String, BakedModel> bg, Map<ResourceLocation, BakedModel> enchantIcons, Map<String, BakedModel> defaultLevelMarks, Map<String, Map<String, BakedModel>> levelMarks) {
        // Note: this no-arg constructor is private for 1.19, 1.19.1 and 1.19.2.
        // Access transformer / widener is required to make this accessible from our code.
        // Forge itself has declared such AT entry on its own; while it is not the case for
        // Fabric's transitive-access-widener.
        // As such, please check that the AW entry is in place before compiling the project.
        super();
        this.bg = bg;
        this.enchantIcons = enchantIcons;
        this.defaultLevelMarks = defaultLevelMarks;
        this.levelMarks = levelMarks;
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack item, @Nullable ClientLevel level, @Nullable LivingEntity holder, int tintIndex) {
        // Try getting enchantments from the ItemStack using the regular way
        var enchants = item.getEnchantmentTags();
        // If it is empty, then try getting them using the Enchanted-Book-specific way
        if (enchants.isEmpty()) {
            enchants = EnchantedBookItem.getEnchantments(item);
        }
        // If there are enchantments, display them in a cyclic fashion
        if (!enchants.isEmpty()) {
            // Gather necessary info: current time (so that we can cycle through all enchantments),
            // current enchantment type & level to display.
            int currentIndexToDisplay = (int) (System.currentTimeMillis() / 1200) % enchants.size();
            var enchant = enchants.getCompound(currentIndexToDisplay);
            // Assemble look-up cache key
            var lookupKey = new AssembledKey(
                    EnchantmentHelper.getEnchantmentId(enchant),
                    EnchantmentHelper.getEnchantmentLevel(enchant),
                    EnchantIconConfig.instance.backgroundType.type,
                    EnchantIconConfig.instance.levelMarkType.type
            );
            // Get the cached model, create one if cache misses.
            var assembled = this.cache.get(lookupKey);
            if (assembled == null) {
                this.cache.put(lookupKey, assembled = this.assemble(lookupKey));
            }
            return PuppetModel.Factory.createFrom(model, assembled, item.is(Items.ENCHANTED_BOOK));
        }
        return model;
    }

    private BakedModel assemble(AssembledKey key) {
        var bgModel = this.bg.get(key.bg);
        var enchantModel = this.enchantIcons.get(key.type);
        var enchantLevel = Integer.toString(key.level);
        var levelMark = this.levelMarks.getOrDefault(key.mark, Map.of()).get(enchantLevel);
        if (levelMark == null) {
            levelMark = this.defaultLevelMarks.get(key.mark);
        }
        return new AssembledIcon(bgModel, enchantModel, levelMark);
    }

    private static record AssembledKey(ResourceLocation type, int level, String bg, String mark) {}

    private static final class AssembledIcon implements BakedModel {

        private final BakedModel bg, enchantIcon, levelMark;
        private final Map<Direction, List<BakedQuad>> quadsByDirection = new EnumMap<>(Direction.class);
        private List<BakedQuad> quadsWithoutDirection = null;

        private AssembledIcon(BakedModel bg, BakedModel enchantIcon, BakedModel levelMark) {
            this.bg = bg;
            this.enchantIcon = enchantIcon;
            this.levelMark = levelMark;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
            if (direction == null) {
                if (this.quadsWithoutDirection == null) {
                    var list = new ArrayList<BakedQuad>();
                    list.addAll(this.bg.getQuads(blockState, null, random));
                    list.addAll(this.enchantIcon.getQuads(blockState, null, random));
                    list.addAll(this.levelMark.getQuads(blockState, null, random));
                    this.quadsWithoutDirection = List.copyOf(list);
                }
                return this.quadsWithoutDirection;
            } else {
                if (!this.quadsByDirection.containsKey(direction)) {
                    var list = new ArrayList<BakedQuad>();
                    list.addAll(this.bg.getQuads(blockState, direction, random));
                    list.addAll(this.enchantIcon.getQuads(blockState, direction, random));
                    list.addAll(this.levelMark.getQuads(blockState, direction, random));
                    this.quadsByDirection.put(direction, List.copyOf(list));
                }
                return this.quadsByDirection.getOrDefault(direction, List.of());
            }
        }

        @Override
        public boolean useAmbientOcclusion() {
            return enchantIcon.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return enchantIcon.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return enchantIcon.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return enchantIcon.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return enchantIcon.getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return enchantIcon.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return enchantIcon.getOverrides();
        }
    }
}

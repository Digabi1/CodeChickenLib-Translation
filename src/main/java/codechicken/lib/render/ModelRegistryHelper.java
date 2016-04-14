package codechicken.lib.render;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModelRegistryHelper {
    private static List<Pair<ModelResourceLocation, IBakedModel>> registerModels = new LinkedList<Pair<ModelResourceLocation, IBakedModel>>();

    static {
        MinecraftForge.EVENT_BUS.register(new ModelRegistryHelper());
    }

    public static void register(ModelResourceLocation location, IBakedModel model) {
        registerModels.add(new ImmutablePair<ModelResourceLocation, IBakedModel>(location, model));
    }

    /**
     * Inserts the item renderer at itemRegistry.getNameForObject(block)#inventory and binds it to the item with a custom mesh definition
     */
    public static void registerItemRenderer(Item item, IItemRenderer renderer) {
        final ModelResourceLocation modelLoc = new ModelResourceLocation(Item.itemRegistry.getNameForObject(item), "inventory");
        register(modelLoc, renderer);
        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                return modelLoc;
            }
        });
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        for (Pair<ModelResourceLocation, IBakedModel> pair : registerModels) {
            event.getModelRegistry().putObject(pair.getKey(), pair.getValue());
        }
    }

    /**
     * Creates a dummy model at blockRegistry.getNameForObject(block)#particle for all states of the block overriding getParticleTexture
     */
    public static void setParticleTexture(Block block, final ResourceLocation tex) {
        final ModelResourceLocation modelLoc = new ModelResourceLocation(Block.blockRegistry.getNameForObject(block), "particle");
        register(modelLoc, new BuiltInModel(BlockRenderer.blockCameraTransform, ItemOverrideList.NONE) {
            @Override
            public TextureAtlasSprite getParticleTexture() {
                return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(tex.toString());
            }
        });
        ModelLoader.setCustomStateMapper(block, new IStateMapper() {
            @Override
            public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
                return Maps.toMap(blockIn.getBlockState().getValidStates(), new Function<IBlockState, ModelResourceLocation>() {
                    @Override
                    public ModelResourceLocation apply(IBlockState input) {
                        return modelLoc;
                    }
                });
            }
        });
    }
}

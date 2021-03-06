package slimeknights.tconstruct.library.client.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import gnu.trove.map.hash.THashMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ToolModel implements IModel {

  private final List<MaterialModel> partBlocks;
  private final List<MaterialModel> brokenPartBlocks;
  private final Float[] layerRotations;
  private final ModifierModel modifiers;
  private final ImmutableMap<TransformType, TRSRTransformation> transforms;
  private final ImmutableMap<TransformType, TRSRTransformation> transformsBlocking;
  private final ImmutableList<ResourceLocation> textures;

  public ToolModel(ImmutableList<ResourceLocation> defaultTextures,
                   List<MaterialModel> parts,
                   List<MaterialModel> brokenPartBlocks,
                   Float[] layerRotations,
                   ModifierModel modifiers,
                   ImmutableMap<TransformType, TRSRTransformation> transforms,
                   ImmutableMap<TransformType, TRSRTransformation> transformsBlocking) {
    this.partBlocks = parts;
    this.brokenPartBlocks = brokenPartBlocks;
    this.layerRotations = layerRotations;
    this.modifiers = modifiers;
    this.transforms = transforms;
    this.transformsBlocking = transformsBlocking;
    this.textures = defaultTextures;
  }

  @Override
  public Collection<ResourceLocation> getDependencies() {
    return ImmutableList.of();
  }

  @Override
  public Collection<ResourceLocation> getTextures() {
    ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

    builder.addAll(textures);

    // modifier textures
    if(modifiers != null) {
      builder.addAll(modifiers.getTextures());
    }

    return builder.build();
  }

  @Override
  public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
    IBakedModel base = new ItemLayerModel(textures).bake(state, format, bakedTextureGetter);

    BakedMaterialModel[] partModels = new BakedMaterialModel[partBlocks.size()];
    BakedMaterialModel[] brokenPartModels = new BakedMaterialModel[partBlocks.size()]; // has to be same size

    // we build simple models for the parts, so we can extract the UV information AND have depth
    for(int i = 0; i < partBlocks.size(); i++) {
      MaterialModel m = partBlocks.get(i);
      partModels[i] = m.bakeIt(getStateForPart(i, state), format, bakedTextureGetter);
    }
    for(int i = 0; i < brokenPartBlocks.size(); i++) {
      if(brokenPartBlocks.get(i) != null) {
        brokenPartModels[i] = brokenPartBlocks.get(i).bakeIt(getStateForPart(i, state), format, bakedTextureGetter);
      }
    }

    Map<String, IBakedModel> modifierModels;
    if(modifiers != null) {
      modifierModels = modifiers.bakeModels(state, format, bakedTextureGetter);
    }
    else {
      modifierModels = new THashMap<String, IBakedModel>();
    }

    Map<TransformType, TRSRTransformation> builder = Maps.newHashMap();
    builder.putAll(IPerspectiveAwareModel.MapWrapper.getTransforms(state));
    builder.putAll(transforms); // only contains actual entries, so we override default values

    // same for blocking
    Map<TransformType, TRSRTransformation> builder2 = Maps.newHashMap();
    builder2.putAll(IPerspectiveAwareModel.MapWrapper.getTransforms(state));
    builder2.putAll(transformsBlocking); // only contains actual entries, so we override default values

    return new BakedToolModel(base, partModels, brokenPartModels, modifierModels, ImmutableMap.copyOf(builder), ImmutableMap.copyOf(builder2));
  }

  private IModelState getStateForPart(int i, IModelState originalState) {
    if(layerRotations.length > i) {
      return new ModelStateComposition(originalState, TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, TRSRTransformation.quatFromXYZ(0, 0, (float) (layerRotations[i] * Math.PI / 180)), null, null)));
    }
    return originalState;
  }

  @Override
  public IModelState getDefaultState() {
    return ModelHelper.DEFAULT_TOOL_STATE;
  }
}

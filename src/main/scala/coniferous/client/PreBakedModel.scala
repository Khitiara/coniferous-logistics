package coniferous.client

import java.util
import java.util.Collections

import net.minecraft.client.render.model.{BakedModel, ModelBakeSettings, ModelLoader, UnbakedModel}
import net.minecraft.client.texture.Sprite
import net.minecraft.util.Identifier

final class PreBakedModel(val baked: BakedModel) extends UnbakedModel {
  override def getModelDependencies: util.Collection[Identifier] = Collections.emptyList[Identifier]

  def getTextureDependencies(var1: util.function.Function[Identifier, UnbakedModel], var2: util.Set[String]): util.Collection[Identifier] =
    Collections.emptyList[Identifier]

  def bake(var1: ModelLoader, var2: util.function.Function[Identifier, Sprite], settings: ModelBakeSettings): BakedModel = baked
}

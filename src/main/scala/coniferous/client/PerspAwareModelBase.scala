package coniferous.client

import java.util
import java.util.Random

import com.google.common.collect.ImmutableList
import net.minecraft.block.{BlockState, Blocks}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.json.{ModelItemPropertyOverrideList, ModelTransformation}
import net.minecraft.client.render.model.{BakedModel, BakedQuad}
import net.minecraft.client.texture.{MissingSprite, Sprite}
import net.minecraft.util.math.Direction

import scala.collection.JavaConverters._

object PerspAwareModelBase {
  def missingModel: util.List[BakedQuad] = {
    val model = MinecraftClient.getInstance.getBlockRenderManager.getModels.getModelManager.getMissingModel
    model.getQuads(Blocks.AIR.getDefaultState, null, new Random)
  }
}

class PerspAwareModelBase(var quads: Seq[BakedQuad], var particle: Sprite) extends BakedModel {
  if (quads == null) quads = Seq.empty
  if (particle != null) {} else particle = MissingSprite.getMissingSprite

  def getQuads(state: BlockState, side: Direction, rand: Random): util.List[BakedQuad] =
    if (side == null) quads.asJava else ImmutableList.of[BakedQuad]

  def useAmbientOcclusion = false

  def hasDepthInGui = false

  def isBuiltin = false

  def getSprite: Sprite = particle

  def getTransformation: ModelTransformation = ModelTransformation.NONE

  def getItemPropertyOverrides: ModelItemPropertyOverrideList = ModelItemPropertyOverrideList.EMPTY
}
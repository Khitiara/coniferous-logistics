package coniferous.client

import java.util.Random
import java.util.function.Supplier

import coniferous.pipe.PipeEntity
import coniferous.shade.io.github.kvverti.msu.WorldAccess._
import javax.annotation.Nullable
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ExtendedBlockView

import scala.collection.JavaConverters._

class PipeBlockModel(kind: PipeEntity.PipeType.Value)
  extends PerspAwareModelBase(Seq.empty, PipeBaseModelGenStandard.getCenterSprite(PipeEntity.PipeRenderData.apply(null, null, null, kind)))
    with FabricBakedModel {

  override def isVanillaAdapter = false

  def emitBlockQuads(blockView: ExtendedBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier[Random],
    context: RenderContext): Unit = {
    val model = blockView match {
      case ex: RenderAttachedBlockView =>
        ex.typedBlockEntity[PipeEntity](pos).map(_.renderData).map(bakeModel).get
      case _ => bakeModel(null)
    }
    context.fallbackConsumer.accept(model)
  }

  private def bakeModel(@Nullable state: PipeEntity.PipeRenderData): BakedModel = {
    val quads = PipeBaseModelGenStandard.generateCutout(state)
    new PerspAwareModelBase(quads.asScala, if (quads.isEmpty) getSprite else quads.get(0).getSprite)
  }

  override def emitItemQuads(stack: ItemStack, randomSupplier: Supplier[Random], context: RenderContext): Unit = {}
}

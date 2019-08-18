package coniferous.client

import java.util.Random
import java.util.function.Supplier

import coniferous.pipe.PipeEntity
import javax.annotation.Nullable
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceManager
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ExtendedBlockView

class PipeBlockModel(kind: PipeEntity.PipeType.Value)
  extends PerspAwareModelBase(Seq.empty, PipeBaseModel.getCenterSprite(kind)) with FabricBakedModel {

  override def isVanillaAdapter = false

  def emitBlockQuads(blockView: ExtendedBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier[Random],
    context: RenderContext): Unit = {
    val model = blockView match {
      case ex: RenderAttachedBlockView => bakeModel(ex.getBlockEntityRenderAttachment(pos).asInstanceOf[PipeEntity.PipeRenderData])
      case _ => bakeModel(null)
    }
    context.fallbackConsumer.accept(model)
  }

  private def bakeModel(@Nullable state: PipeEntity.PipeRenderData): BakedModel = {
    val quads = PipeBaseModel.generateCutout(state)
    new PerspAwareModelBase(quads, if (quads.isEmpty) getSprite else quads(0).getSprite)
  }

  override def emitItemQuads(stack: ItemStack, randomSupplier: Supplier[Random], context: RenderContext): Unit = {}
}

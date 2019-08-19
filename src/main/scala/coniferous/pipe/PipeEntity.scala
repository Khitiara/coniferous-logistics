package coniferous.pipe

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import coniferous.Coniferous
import coniferous.api.PipeConnectable
import coniferous.pipe.PipeEntity.PipeRenderData
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.minecraft.block.{Block, BlockState}
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.util.Tickable
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.{BlockView, World}

class PipeEntity(beType: BlockEntityType[_ <: PipeEntity]) extends BlockEntity(beType) with Tickable
  with RenderAttachmentBlockEntity {
  val initialized: AtomicBoolean = new AtomicBoolean()
  val connections: util.EnumSet[Direction] = util.EnumSet.noneOf[Direction](classOf[Direction])
  var state: PipeEntityState = _
  var kind: PipeEntity.PipeType.Value = _

  def fixConnections(): Unit = {
    connections.clear()
    for {
      face <- Direction.values()
      spot = pos.offset(face)
      st = world.getBlockState(spot)
      if state.pipe.canConnect(connections, st) // We can connect to it
      if Option(world.getBlockEntity(spot)).map(_.asInstanceOf[PipeConnectable])
        .forall(_.canConnect(state.pipe, face.getOpposite)) // It can connect to us (if it defines those semantics)
    } connections.add(face)
  }

  override def tick(): Unit = {
    if (!initialized.compareAndExchange(false, true)) {
      kind = getCachedState.get(Coniferous.Properties.KIND_PROPERTY)
      state = PipeEntityState(PipeEntity.kinds(kind)(pos, world))
    }
  }

  override def getRenderAttachmentData: AnyRef = PipeRenderData(connections, state.pipe, world.getBlockState(pos), kind)

  case class PipeEntityState(pipe: Pipe)

}

object PipeEntity {
  val tpe: BlockEntityType[PipeEntity] = BlockEntityType.Builder.create(() => new PipeEntity(tpe), BlockPipe).build(null)
  var kinds: Map[PipeType.Value, (BlockPos, World) => Pipe] = Map.empty

  case class PipeRenderData(connections: util.EnumSet[Direction], pipe: Pipe, state: BlockState, kind: PipeType.Value) {
    def isConnected(face: Direction): Boolean = connections.contains(face)

    def block: Block = state.getBlock
  }

  object PipeType extends Enumeration {
    val provider, supplier, transport, crafting, terminal, satellite = Value
  }

}

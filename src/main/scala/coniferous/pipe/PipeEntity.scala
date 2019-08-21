package coniferous.pipe

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import com.mojang.datafixers
import coniferous.api.PipeConnectable
import coniferous.pipe.PipeEntity.PipeRenderData
import coniferous.pipe.routing.{JunctionPipe, ProviderPipe, SupplierPipe, TransportPipe}
import coniferous.shade.io.github.kvverti.msu.WorldAccess._
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.client.world.ClientWorld
import net.minecraft.datafixers.NbtOps
import net.minecraft.nbt.{CompoundTag, EndTag, Tag}
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Tickable
import net.minecraft.util.math.{BlockPos, ChunkPos, Direction}
import net.minecraft.world.World

import scala.collection.JavaConverters._
import scala.collection.immutable.BitSet

class PipeEntity(beType: BlockEntityType[_ <: PipeEntity]) extends BlockEntity(beType) with Tickable
  with BlockEntityClientSerializable {

  val setup: AtomicBoolean = new AtomicBoolean()
  val connections: util.EnumSet[Direction] = util.EnumSet.noneOf[Direction](classOf[Direction])
  var pipe: Pipe = _
  var kind: PipeEntity.PipeType.Value = PipeEntity.PipeType.transport
  var pipeData: Tag = _

  def initialize(): Unit = {
    if (setup.compareAndSet(false, true)) {
      kind = world.getBlockState(pos).typedBlock[BlockPipe].map(_.kind).get
      pipe = PipeEntity.kinds(kind)(pos, world)
      pipe.fromTag(new datafixers.Dynamic[Tag](NbtOps.INSTANCE, pipeData))
    }
    fixConnections()
  }

  def fixConnections(): Unit = {
    if (!world.isClient) {
      val oldConns = util.EnumSet.copyOf(connections)
      connections.clear()
      for {
        face <- Direction.values()
        spot = pos.offset(face)
        st = world.getBlockState(spot)
        if pipe == null || pipe.canConnect(connections, st, face) // We can connect to it
        if (world.getBlockEntity(spot) match {
          case pc: PipeConnectable => pc.canConnect(pipe, face.getOpposite)
          case pi: PipeEntity if pi.pipe != null => pi.pipe.canConnect(pi.connections, st, face.getOpposite)
          case _ => true
        })
      } connections.add(face)
      oldConns.asScala.diff(connections.asScala).union(connections.asScala.diff(oldConns.asScala))
        .foreach(dir => world.typedBlockEntity[PipeEntity](pos.offset(dir)).foreach(_.fixConnections()))
    }
    refreshModel()
  }

  override def fromClientTag(compoundTag: CompoundTag): Unit = {
    fromTag(compoundTag)
    refreshModel()
  }

  def renderData: PipeRenderData = {
    PipeRenderData(connections, pipe, world.getBlockState(pos), kind)
  }

  override def tick(): Unit = {
  }

  override def fromTag(compoundTag: CompoundTag): Unit = {
    super.fromTag(compoundTag)
    connections.clear()
    connections.addAll(BitSet.fromBitMask(Array(compoundTag.getLong("Connections")))
      .map(Direction.byId).asJavaCollection)
    pipeData = compoundTag.getTag("PipeData")
  }

  case class PipeEntityState(pipe: Pipe)

  override def toClientTag(compoundTag: CompoundTag): CompoundTag = {
    toTag(compoundTag)
  }

  override def toTag(compoundTag: CompoundTag): CompoundTag = {
    super.toTag(compoundTag)
    compoundTag.putLong("Connections", connections.asScala.map(_.getId).foldLeft(BitSet.empty)((s, i) => s + i)
      .toBitMask(0))
    if (pipe != null) pipeData = pipe.toTag(NbtOps.INSTANCE).getValue else pipeData = new EndTag
    compoundTag.put("PipeData", pipeData)
    compoundTag
  }

  protected def refreshModel(): Unit = {
    markDirty()
    world match {
      case clientWorld: ClientWorld =>
        clientWorld.scheduleBlockRenders(pos.getX >> 4, pos.getY >> 4, pos.getZ >> 4)
      case serverWorld: ServerWorld =>
        serverWorld.method_14178().threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(pos), false)
          .forEach(player => player.networkHandler.sendPacket(toUpdatePacket))
    }
  }
}

object PipeEntity {
  val tpe: BlockEntityType[PipeEntity] = BlockEntityType.Builder.create(() => new PipeEntity(tpe), BlockPipe.kinds
    .values.toSeq: _*).build(null)
  val kinds: Map[PipeType.Value, (BlockPos, World) => Pipe] = Map(
    PipeType.transport -> TransportPipe.apply,
    PipeType.provider -> ProviderPipe.apply,
    PipeType.supplier -> SupplierPipe.apply,
    PipeType.junction -> JunctionPipe.apply
  )

  case class PipeRenderData(connections: util.EnumSet[Direction], pipe: Pipe, state: BlockState, kind: PipeType.Value) {
    def isConnected(face: Direction): Boolean = connections.contains(face)

    def block: Block = state.getBlock
  }

  object PipeType extends Enumeration {
    val provider, supplier, transport, crafting, terminal, satellite, junction = Value
  }

}

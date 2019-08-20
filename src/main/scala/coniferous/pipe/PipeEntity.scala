package coniferous.pipe

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import com.mojang.datafixers
import coniferous.pipe.PipeEntity.PipeRenderData
import coniferous.pipe.routing.{ProviderPipe, SupplierPipe, TransportPipe}
import coniferous.shade.io.github.kvverti.msu.WorldAccess._
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.block.{Block, BlockState}
import net.minecraft.client.network.packet.BlockEntityUpdateS2CPacket
import net.minecraft.client.world.ClientWorld
import net.minecraft.datafixers.NbtOps
import net.minecraft.nbt.{CompoundTag, EndTag, Tag}
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Tickable
import net.minecraft.util.math.{BlockPos, Direction, Vec3d}
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
    if (!setup.compareAndExchange(false, true)) {
      kind = world.getBlockState(pos).typedBlock[BlockPipe].map(_.kind).get
      pipe = PipeEntity.kinds(kind)(pos, world)
      pipe.fromTag(new datafixers.Dynamic[Tag](NbtOps.INSTANCE, pipeData))
    }
    fixConnections()
  }

  def fixConnections(): Unit = {
    if (!world.isClient) {
      connections.clear()
      for {
        face <- Direction.values()
        spot = pos.offset(face)
        st = world.getBlockState(spot)
        if pipe == null || pipe.canConnect(connections, st) // We can connect to it
        //      if world.collectBlockEntity(pos){
        //        case be: PipeConnectable => be.canConnect(state.pipe, face.getOpposite)
        //      }.getOrElse(true)// It can connect to us (if it defines those semantics)
      } connections.add(face)
      println(s"${connections.size()} connections for $kind pipe at $pos")
      markDirty()
      refreshModel()
    }
  }

  protected def refreshModel(): Unit = {
    val w = world
    w match {
      case serverWorld: ServerWorld => sendPacket(serverWorld, this.toUpdatePacket)
      case clientWorld: ClientWorld => clientWorld.scheduleBlockRenders(pos.getX >> 4, pos.getY >> 4, pos.getZ >> 4)
    }
  }

  protected def sendPacket(w: ServerWorld, packet: BlockEntityUpdateS2CPacket): Unit = {
    w.getPlayers((player: ServerPlayerEntity) => player.squaredDistanceTo(new Vec3d(getPos)) < 24 * 24)
      .forEach((player: ServerPlayerEntity) => player.networkHandler.sendPacket(packet))
  }

  def renderData: PipeRenderData = {
    PipeRenderData(connections, pipe, world.getBlockState(pos), kind)
  }

  override def tick(): Unit = {
  }

  override def fromClientTag(compoundTag: CompoundTag): Unit = {
    fromTag(compoundTag)
    connections.clear()
    connections.addAll(BitSet.fromBitMask(Array(compoundTag.getLong("Connections")))
      .map(Direction.byId).asJavaCollection)
    refreshModel()
  }

  case class PipeEntityState(pipe: Pipe)

  override def fromTag(compoundTag_1: CompoundTag): Unit = {
    super.fromTag(compoundTag_1)
    pipeData = compoundTag_1.getTag("PipeData")
  }

  override def toClientTag(compoundTag: CompoundTag): CompoundTag = {
    compoundTag.putLong("Connections", connections.asScala.map(_.getId).foldLeft(BitSet.empty)((s, i) => s + i)
      .toBitMask(0))
    toTag(compoundTag)
  }

  override def toTag(compoundTag_1: CompoundTag): CompoundTag = {
    super.toTag(compoundTag_1)
    if (pipe != null) pipeData = pipe.toTag(NbtOps.INSTANCE).getValue else pipeData = new EndTag
    compoundTag_1.put("PipeData", pipeData)
    compoundTag_1
  }

  protected def sendPacket(w: ServerWorld, tag: CompoundTag): Unit = {
    tag.putString("id", BlockEntityType.getId(getType).toString)
    sendPacket(w, new BlockEntityUpdateS2CPacket(getPos, 127, tag))
  }
}

object PipeEntity {
  val tpe: BlockEntityType[PipeEntity] = BlockEntityType.Builder.create(() => new PipeEntity(tpe), BlockPipe.kinds
    .values.toSeq: _*).build(null)
  val kinds: Map[PipeType.Value, (BlockPos, World) => Pipe] = Map(
    PipeType.transport -> TransportPipe.apply,
    PipeType.provider -> ProviderPipe.apply,
    PipeType.supplier -> SupplierPipe.apply
  )

  case class PipeRenderData(connections: util.EnumSet[Direction], pipe: Pipe, state: BlockState, kind: PipeType.Value) {
    def isConnected(face: Direction): Boolean = connections.contains(face)

    def block: Block = state.getBlock
  }

  object PipeType extends Enumeration {
    val provider, supplier, transport, crafting, terminal, satellite = Value
  }

}

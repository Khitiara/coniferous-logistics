package coniferous

import coniferous.block.{BlockRegistry, EnumerationProperty}
import coniferous.pipe.{BlockPipe, PipeEntity}
import coniferous.shade.io.github.kvverti.msu.Properties._
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.BlockState
import net.minecraft.item._
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Coniferous extends ModInitializer {
  val modid = "coniferous"
  //noinspection ForwardReference
  val group: ItemGroup = FabricItemGroupBuilder.create(this :/ "general").icon(() => new ItemStack(pipeItems(PipeEntity.PipeType.transport))).build()

  override def onInitialize(): Unit = {
    BlockRegistry.init()
    Registry.register(Registry.BLOCK_ENTITY, this :/ "pipe", PipeEntity.tpe)
  }

  def :/(name: String): Identifier = new Identifier(modid, name)

  val PIPE: BlockPipe.type = BlockPipe
  val pipeItems: Map[PipeEntity.PipeType.Value, Item] = PipeEntity.PipeType.values.map(kind => {
    kind -> Registry.register(Registry.ITEM, Coniferous :/ s"pipe_${kind.toString.toLowerCase}", new BlockItem(PIPE, new Item.Settings().group(group)) {
      override def getPlacementState(itemPlacementContext_1: ItemPlacementContext): BlockState =
        super.getPlacementState(itemPlacementContext_1).withVal(Properties.KIND_PROPERTY, kind)
    })
  }).toMap

  object Properties {
    val KIND_PROPERTY: Property[PipeEntity.PipeType.Value] =
      new EnumerationProperty("kind", PipeEntity.PipeType, classOf[PipeEntity.PipeType.Value])
  }
}

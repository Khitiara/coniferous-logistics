package coniferous

import coniferous.block.BlockRegistry
import coniferous.pipe.{BlockPipe, PipeEntity}
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Coniferous extends ModInitializer {
  val modid = "coniferous"
  val group: ItemGroup = FabricItemGroupBuilder.create(this :/ "general").icon(() => null).build()

  override def onInitialize(): Unit = {
    BlockRegistry.init()
    Registry.register(Registry.BLOCK_ENTITY, this :/ "pipe", PipeEntity.tpe)
  }

  def :/(name: String): Identifier = new Identifier(modid, name)

  object Blocks {
    val PIPE: BlockPipe.type = BlockPipe
  }
}

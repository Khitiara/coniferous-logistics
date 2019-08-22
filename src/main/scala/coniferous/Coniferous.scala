package coniferous

import coniferous.block.BlockRegistry
import coniferous.pipe.{BlockPipe, PipeEntity}
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item._
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Coniferous extends ModInitializer {
  val modid = "coniferous"
  val group: ItemGroup = FabricItemGroupBuilder.create(this :/ "general").icon(() => new ItemStack(BlockPipe
    .kinds(PipeEntity.PipeType.transport))).build()

  override def onInitialize(): Unit = {
    BlockPipe.init()
    BlockRegistry.init()
    Registry.register(Registry.BLOCK_ENTITY, this :/ "pipe", PipeEntity.tpe)
  }

  def :/(name: String): Identifier = new Identifier(modid, name)
}

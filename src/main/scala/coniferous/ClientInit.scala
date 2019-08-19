package coniferous

import coniferous.client.{PipeBlockModel, PreBakedModel}
import coniferous.pipe.PipeEntity
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.texture.SpriteAtlasTexture

object ClientInit extends ClientModInitializer {

  override def onInitializeClient(): Unit = {
    ModelLoadingRegistry.INSTANCE.registerVariantProvider(_ => (resId, _) => {
      if ("inventory".equals(resId.getVariant)) null else resId.getNamespace match {
        case Coniferous.modid if resId.getPath.startsWith("pipe_") =>
          new PreBakedModel(new PipeBlockModel(PipeEntity.PipeType.withName(resId.getPath.substring(5))))
        case _ => null
      }
    })
    ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register((tex, reg) => registerSprites(tex, reg))
  }

  def registerSprites(tex: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry): Unit = {
    def reg(name: String): Unit = registry.register(Coniferous :/ name)

    reg("pipe_transport")
    reg("pipe_provider")
    reg("pipe_provider_main")
    reg("pipe_supplier")
    reg("pipe_supplier_main")
    reg("pipe_terminal")
    reg("pipe_terminal_main")
    reg("pipe_satellite")
    reg("pipe_satellite_main")
    reg("pipe_crafting")
    reg("pipe_crafting_main")
  }

}

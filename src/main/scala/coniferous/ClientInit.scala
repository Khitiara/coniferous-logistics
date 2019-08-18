package coniferous

import coniferous.client.{PipeBlockModel, PreBakedModel}
import coniferous.pipe.PipeEntity
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry

object ClientInit extends ClientModInitializer {
  override def onInitializeClient(): Unit = {
    ModelLoadingRegistry.INSTANCE.registerVariantProvider(_ => (resId, _) => {
      if ("inventory".equals(resId.getVariant)) null else resId.getNamespace match {
        case Coniferous.modid if resId.getPath.startsWith("pipe_") =>
          new PreBakedModel(new PipeBlockModel(PipeEntity.PipeType.withName(resId.getPath.substring(5))))
      }
    })
  }
}

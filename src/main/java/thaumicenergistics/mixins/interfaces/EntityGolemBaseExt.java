package thaumicenergistics.mixins.interfaces;

import java.util.HashMap;

import thaumicenergistics.api.entities.IGolemHookHandler;

public interface EntityGolemBaseExt {

    HashMap<IGolemHookHandler, Object> thenergistic$getHandlers();
}

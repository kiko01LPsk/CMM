package net.kiko.walkman;

import net.kiko.KikoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, KikoMod.MOD_ID);

    public static final RegistryObject<MenuType<WalkmanMenu>> WALKMAN = MENUS.register("walkman",
            () -> IForgeMenuType.create((id, inv, buf) -> new WalkmanMenu(id, inv, buf.readVarInt())));

    private ModMenus() {}

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}

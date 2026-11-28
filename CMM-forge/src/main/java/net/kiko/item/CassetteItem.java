package net.kiko.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;
import java.util.List;

/** A cassette for the walkman. It holds one or more songs (sound files inside the mod, played by the walkman on the client). */
public class CassetteItem extends Item {

    /** @param file path inside assets/kiko, @param ticks length of the song in game ticks (20 per second) */
    public record Track(String file, String name, int ticks) {}

    private final List<Track> tracks;
    private final String artist;

    public CassetteItem(Properties properties, String artist, List<Track> tracks) {
        super(properties.stacksTo(1));
        this.artist = artist;
        this.tracks = tracks;
    }

    public List<Track> tracks() {
        return tracks;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(artist).withStyle(ChatFormatting.GRAY));
    }
}

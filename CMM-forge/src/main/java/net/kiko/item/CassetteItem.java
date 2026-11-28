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

    /** Whole length of the tape in ticks. */
    public long totalTicks() {
        long sum = 0;
        for (Track t : tracks) sum += t.ticks();
        return sum;
    }

    /** Tick where song number {@code index} starts on the tape. */
    public long startOf(int index) {
        long sum = 0;
        for (int i = 0; i < index && i < tracks.size(); i++) sum += tracks.get(i).ticks();
        return sum;
    }

    /** Which song is playing at this spot of the tape. */
    public int trackAt(long pos) {
        long sum = 0;
        for (int i = 0; i < tracks.size(); i++) {
            sum += tracks.get(i).ticks();
            if (pos < sum) return i;
        }
        return tracks.size() - 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(artist).withStyle(ChatFormatting.GRAY));
        if (tracks.size() > 1) {
            for (int i = 0; i < tracks.size(); i++) {
                tooltip.add(Component.literal((i + 1) + ". " + tracks.get(i).name()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}

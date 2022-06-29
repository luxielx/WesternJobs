package Luxielx.Main;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PrefixHolder extends PlaceholderExpansion {
    Main plugin;

    public PrefixHolder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "westernjobs";
    }

    @Override
    public String getAuthor() {
        return "Luxielx";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.equalsIgnoreCase("jobs")) {
            String jobs = "No Jobs";
            if (Main.haveJob(player.getUniqueId().toString())) {
                jobs = Main.getJob(player.getUniqueId().toString()).getPrefix();
            }


            return jobs;
        }
        return null;
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }
}

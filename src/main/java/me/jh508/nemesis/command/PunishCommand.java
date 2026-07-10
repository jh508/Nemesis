package me.jh508.nemesis.command;

import me.jh508.nemesis.Nemesis;
import me.jh508.nemesis.menu.PunishMainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PunishCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) return false;
        if (!sender.hasPermission("moderation.punish")) return false;

        Player player = (Player) sender;

        if (args.length < 1)
        {
            player.sendMessage("Please provide a player name.");
            return true;
        }

        if (args.length < 2)
        {
            player.sendMessage("Please provide a reason");
            return true;
        }

        String targetPlayerName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        PunishMainMenu mainMenu = new PunishMainMenu(Nemesis.getInstance(), player.getUniqueId(), targetPlayerName, reason);
        player.openInventory(mainMenu.getInventory());

        return true;
    }
}

package Luxielx.Main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {


    private boolean compare(String z, String s){
        if(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',z)).contains(ChatColor.stripColor(s))){
            return true;
        }
        return false;
    }
    @EventHandler
    public void click(PlayerInteractEvent e){
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK){
            Block b = e.getClickedBlock();
            Player p = e.getPlayer();
            
            if(b.getType().toString().contains("SIGN")){
                Sign sign = (Sign) e.getClickedBlock().getState();
                String prefix = sign.getLine(0);
                Jobs job = null;
                for(String s : ConfigManager.getJob().getKeys(true)){

                    if(s.contains("prefix")){
                        if(compare(ConfigManager.getJob().getString(s),prefix)){


                            String[] z = s.split("\\.");
                            job = new Jobs(z[0]);
                        }
                    }
                    
                }

                if(prefix.isEmpty()) return;
                if(job != null && job.isExist()){

                    if(Main.haveJob(p.getUniqueId().toString())){

                        p.sendMessage(ChatColor.RED+"You already have a job.");
                    }else{

                        if (!job.addMember(p.getUniqueId().toString())) {

                            p.sendMessage(ChatColor.GRAY + "This Job Is Full");
                        } else {

                            p.sendMessage(ChatColor.GRAY + p.getName() + " is now a part of " + job.getName());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void signchange(SignChangeEvent e){
        String first = e.getLine(0);
        Jobs job = new Jobs(first);
        if(job.isExist() && e.getPlayer().hasPermission("oppermission")){
            e.setLine(0,trans(ConfigManager.getHierarchy().getString("signline1").replace("%prefix%",job.prefix)));
            e.setLine(1,trans(ConfigManager.getHierarchy().getString("signline2").replace("%prefix%",job.prefix)));
            e.setLine(2,trans(ConfigManager.getHierarchy().getString("signline3").replace("%prefix%",job.prefix)));
            e.setLine(3,Main.hideS(job.getName()));
        }

    }


    public String trans(String s){
        return ChatColor.translateAlternateColorCodes('&',s);

    }

}

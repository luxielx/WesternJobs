package Luxielx.Main;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Main extends JavaPlugin {
    static Main instance;
    static RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    FileConfiguration config;

    public static String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static boolean havePermission(UUID user, String permission) {
        User use = LuckPermsProvider.get().getUserManager().getUser(user);
        CachedPermissionData permissionData = use.getCachedData().getPermissionData();

        return permissionData.checkPermission(permission).asBoolean();
    }

    public static void addPermission(UUID userUuid, String permission) {
        LuckPermsProvider.get().getUserManager().modifyUser(userUuid, user -> {
            user.data().add(Node.builder(permission).build());
        });
    }

    public static void removePermission(UUID userUuid,String perm) {
        LuckPermsProvider.get().getUserManager().modifyUser(userUuid, user -> {
            user.data().toCollection().forEach(node -> {
                if (node.getKey().contains(perm)) {
                    user.data().remove(node);
                }
            });
//            user.data().remove(Node.builder(permission).build());
        });
    }

    public static boolean haveJob(String uuid) {
        for (String s : ConfigManager.getJob().getKeys(false)) {
            Jobs j = new Jobs(s);
            if (j.getMembers().contains(uuid)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTop(String uuid) {
        if (!haveJob(uuid)) return false;
        Jobs j = getJob(uuid);
        if (j.getPosition() <= 1) {
            return true;
        }
        return false;
    }

    public static Jobs getJob(String uuid) {
        for (String s : ConfigManager.getJob().getKeys(false)) {
            Jobs j = new Jobs(s);
            if (j.getMembers().contains(uuid)) {
                return j;
            }
        }
        return null;
    }

    public static boolean sameHierarchy(String uuid, String uuid2) {
        if (!haveJob(uuid)) return false;
        if (!haveJob(uuid2)) return false;
        Jobs t1 = getJob(uuid);
        Jobs t2 = getJob(uuid2);
        if (!t1.isInhierarchy()) return false;
        if (!t2.isInhierarchy()) return false;
        if (t1.getHierarchy().getName().equals(t2.getHierarchy().getName())) return true;


        return false;
    }

    @Override
    public void onEnable() {

        new PrefixHolder(this).register();

        instance = this;
        config = getConfig();
        config.options().copyDefaults(true);
        ConfigManager.getInstance().setPlugin(this);
        saveDefaultConfig();
        ConfigManager.getInstance().createNewCustomConfig("jobs.yml");
        this.getServer().getPluginManager().registerEvents(new SignListener(),this);

    }

    @Override
    public void onDisable() {

    }
    public static String hideS(String s) {
        String hidden = "";
        for (char c : s.toCharArray())
            hidden += ChatColor.COLOR_CHAR + "" + c;
        return hidden;
    }

    public static String unhideS(String s) {
        String r = s.replaceAll("§", "");
        return r;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wcjobs") && sender.hasPermission("westernjobs.op")) {

            CommandSender p = sender;
            if (args.length < 1) {
                p.sendMessage(ChatColor.GRAY + "/wcjobs create [job]");
                p.sendMessage(ChatColor.GRAY + "/wcjobs edit [job]");
                p.sendMessage(ChatColor.GRAY + "/wcjobs setjob [player] [job]");
                p.sendMessage(ChatColor.GRAY + "/wcjobs list");
                return false;

            }
            if (args[0].equalsIgnoreCase("test")) {
                Player z = (Player) sender;
                getJob(z.getUniqueId().toString()).getHierarchy().promote(z.getUniqueId().toString());
            }
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length > 1) {
                    Jobs b = new Jobs(args[1]);
                    if (b.isExist()) {
                        p.sendMessage(ChatColor.GREEN + "This Job is already exist, You can edit it by /wcjobs edit " + args[1]);
                        return false;
                    }
                    p.sendMessage(ChatColor.GREEN + args[1] + " created, You can edit it by /wcjobs edit " + args[1] + " or edit it in config file");
                    b.save();
                } else {
                    p.sendMessage(ChatColor.GREEN + "/wcjobs create [jobname]");
                }

            }
            //wcjobs setjob [player] [job]
            if (args[0].equalsIgnoreCase("setjob")) {
                if (Bukkit.getPlayer(args[1]) != null) {
                    Player target = Bukkit.getPlayer(args[1]);
                    Jobs j = new Jobs(args[2]);
                    if (!j.isExist()) {
                        p.sendMessage(ChatColor.RED + "Invalid Job");
                        return false;
                    }
                    if (!j.addMember(target.getUniqueId().toString())) {
                        p.sendMessage(ChatColor.GRAY + "This Job Is Full");
                    } else {
                        p.sendMessage(ChatColor.GRAY + target.getName() + " is now a part of " + j.getName());
                    }

                } else {
                    p.sendMessage(ChatColor.RED + "Invalid player");
                }

            }
            if (args[0].equalsIgnoreCase("list")) {
                p.sendMessage(ChatColor.RED + "Job List");
                for (String s : ConfigManager.getJob().getKeys(false)) {
                    Jobs j = new Jobs(s);
                    if (j.isInhierarchy()) {
                        p.sendMessage(ChatColor.GREEN + j.getName() + ChatColor.GRAY +"["+j.position+"]" +" of " + ChatColor.RED.toString() + j.getHierarchy().getName());
                    } else {
                        p.sendMessage(ChatColor.GREEN + j.getName());
                    }
                }
            }
            if (args[0].equalsIgnoreCase("edit")) {
                if (args.length > 1) {
                    Jobs j = new Jobs(args[1]);
                    if (j.isExist()) {
                        if (args.length > 3 && args[2].equalsIgnoreCase("prefix")) {
                            j.setPrefix(args[3]);
                            p.sendMessage(ChatColor.GRAY + "Prefix is now " + cc(args[3]));
                        } else if (args.length > 2 && args[2].equalsIgnoreCase("hierarchy")) {
                            if (args.length > 3) {
                                Hierarchy h = new Hierarchy(args[3]);
                                h.addJob(j.getName());
                                j.setHierarchy(h);
                                p.sendMessage(ChatColor.GRAY + j.getName() + " is now a part of " + h.getName() + " hierarchy");
                            } else {
                                if (j.isInhierarchy()) {
                                    Hierarchy h = j.getHierarchy();
                                    h.removeJob(j.getName());
                                    j.removeHierarchy();
                                }


                                p.sendMessage(ChatColor.RED + j.getName() + " No longer have hierarchy!");
                            }
                        } else if (args.length > 3 && args[2].equalsIgnoreCase("position")) {
                            int pos = Integer.parseInt(args[3]);
                            j.setPosition(pos);
                            p.sendMessage(ChatColor.GRAY + j.getName() + " now at position " + pos);
                        } else if (args.length > 3 && args[2].equalsIgnoreCase("limit")) {
                            int pos = Integer.parseInt(args[3]);
                            j.setLimit(pos);
                            p.sendMessage(ChatColor.GRAY + j.getName() + " now have limit of " + pos);
                        } else {
                            p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + args[1] + " prefix [newprefix]  || Change Job's prefix");
                            p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + args[1] + " hierarchy [hierarchy/empty] || Change Job's current hierarchy, If no hierarchy set it'll remove current hierarchy");
                            p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + args[1] + " position [position]  || Change Job's position");
                            p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + args[1] + " limit [limit]  || Change Job's limit");
                        }

                    } else {
                        p.sendMessage(ChatColor.RED + args[1] + "Don't exist,You can use /wcjobs create [name]");
                        return false;
                    }
                } else {
                    String s = "[job]";
                    p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + s + " prefix [newprefix]  || Change Job's prefix");
                    p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + s + " hierarchy [hierarchy/empty] || Change Job's current hierarchy, If no hierarchy set it'll remove current hierarchy");
                    p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + s + " position [position]  || Change Job's position");
                    p.sendMessage(ChatColor.GRAY + "/wcjobs edit " + s + " limit [limit]  || Change Job's limit");
                }

            }

            // /fire [player
        } else if (cmd.getName().equalsIgnoreCase("fire")) {
            Player p = (Player) sender;
            if (args.length > 0) {
                if (!isTop(p.getUniqueId().toString())) {
                    p.sendMessage(ChatColor.RED + "You don't have permission to use this command");
                    return false;
                }
                if (Bukkit.getPlayer(args[0]) != null) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (haveJob(target.getUniqueId().toString())) {
                        Jobs j = getJob(target.getUniqueId().toString());
                        if (sameHierarchy(p.getUniqueId().toString(), target.getUniqueId().toString())) {
                            if(j.position <= 1) return false;
                            j.removeMember(target.getUniqueId().toString());
                            p.sendMessage(ChatColor.GRAY + target.getName() + " no longer have a job");
                        } else {
                            p.sendMessage(ChatColor.GRAY + "You and " + target.getName() + " don't share the same hierarchy!");
                        }
                    } else {
                        p.sendMessage(ChatColor.GRAY + "This player don't have a job");
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "/fire [player]");
            }
        }
        else if (cmd.getName().equalsIgnoreCase("quitjob")) {
            Player p = (Player) sender;
            if(haveJob(p.getUniqueId().toString())){
                Jobs j = getJob(p.getUniqueId().toString());
                j.removeMember(p.getUniqueId().toString());
                p.sendMessage(ChatColor.RED+"You've quit from job " + j.getName() );
            }else{
                p.sendMessage(ChatColor.RED+"You don't have a job");
            }
        }
        //appoint player job
        else if (cmd.getName().equalsIgnoreCase("appoint")) {
            Player p = (Player) sender;
            if (!isTop(p.getUniqueId().toString())) {
                p.sendMessage(ChatColor.RED + "You don't have permission to use this command");
                return false;
            }
            if (args.length > 0) {
                if (Bukkit.getPlayer(args[0]) != null) {
                    Player target = Bukkit.getPlayer(args[0]);


                    if (args.length <= 1) return false;
                    Jobs tj = new Jobs(args[1]);
                    if (!tj.isExist()) {
                        p.sendMessage(ChatColor.RED + "This job don't exist!");
                        return false;
                    }
                    Jobs cj = getJob(p.getUniqueId().toString());
                    if (haveJob(target.getUniqueId().toString())) {
                        p.sendMessage(ChatColor.RED + target.getName() + " already have a job! If you manage their job, try /fire them and try again!");
                    } else {
                        if (!tj.isInhierarchy()) return false;
                        if (tj.getHierarchy().getName().equals(cj.getHierarchy().getName())) {
                            if (!tj.addMember(target.getUniqueId().toString())) {
                                p.sendMessage(ChatColor.GRAY + "This Job Is Full");
                            } else {
                                p.sendMessage(ChatColor.GRAY + target.getName() + " is now a part of " + tj.getName());
                            }
                        } else {
                            p.sendMessage(ChatColor.GRAY + "You and " + tj.getName() + " don't share the same hierarchy!");
                        }
                    }
                }
            } else {
                p.sendMessage(ChatColor.GRAY + "/appoint [player] [job]");
            }
        }
        // promote player
        else if (cmd.getName().equalsIgnoreCase("promote")) {
            Player p = (Player) sender;
            if (args.length > 0) {
                if (!isTop(p.getUniqueId().toString())) {
                    p.sendMessage(ChatColor.RED + "You don't have permission to use this command");
                    return false;
                }


                if (Bukkit.getPlayer(args[0]) != null) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (haveJob(target.getUniqueId().toString())) {
                        Jobs j = getJob(target.getUniqueId().toString());
                        if (sameHierarchy(p.getUniqueId().toString(), target.getUniqueId().toString())) {
                            if (j.getPosition() > 1) {
                                Hierarchy h = j.getHierarchy();
                                if (h.promote(target.getUniqueId().toString()) != null) {
                                    Jobs newj = h.promote(target.getUniqueId().toString());
                                    newj.addMember(target.getUniqueId().toString());
                                    p.sendMessage(ChatColor.GRAY + target.getName() + " promoted to " + newj.getName());
                                } else {
                                    p.sendMessage(ChatColor.GRAY + target.getName() + " can't be promote");
                                }

                            } else {
                                p.sendMessage(ChatColor.GRAY + target.getName() + " already at highest position!");
                            }

                        } else {
                            p.sendMessage(ChatColor.GRAY + "You and " + target.getName() + " don't share the same hierarchy!");
                        }
                    } else {
                        p.sendMessage(ChatColor.GRAY + "This player don't have a job");
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "/promote [player]");
            }
        } else if (cmd.getName().equalsIgnoreCase("demote")) {
            Player p = (Player) sender;
            if (args.length > 0) {
                if (!isTop(p.getUniqueId().toString())) {
                    p.sendMessage(ChatColor.RED + "You don't have permission to use this command");
                    return false;
                }
                if (Bukkit.getPlayer(args[0]) != null) {
                    Player target = Bukkit.getPlayer(args[0]);
                    if (haveJob(target.getUniqueId().toString())) {
                        Jobs j = getJob(target.getUniqueId().toString());
                        if (sameHierarchy(p.getUniqueId().toString(), target.getUniqueId().toString())) {
                            Hierarchy h = j.getHierarchy();
                            if (j.getPosition() <= 1) {
                                p.sendMessage(ChatColor.GRAY + target.getName() + " can't be demote");
                                return false;
                            }
                            if (h.demote(target.getUniqueId().toString()) != null) {
                                Jobs newj = h.demote(target.getUniqueId().toString());
                                newj.addMember(target.getUniqueId().toString());
                                p.sendMessage(ChatColor.GRAY + target.getName() + " demoted to " + newj.getName());


                            } else {
                                p.sendMessage(ChatColor.GRAY + target.getName() + " can't be demote");
                            }


                        } else {
                            p.sendMessage(ChatColor.GRAY + "You and " + target.getName() + " don't share the same hierarchy!");
                        }
                    } else {
                        p.sendMessage(ChatColor.GRAY + "This player don't have a job");
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "/promote [player]");
            }
        }


        return false;

    }


}

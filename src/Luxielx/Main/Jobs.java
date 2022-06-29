package Luxielx.Main;

import java.util.ArrayList;
import java.util.UUID;

public class Jobs {

    String name;
    String prefix;

    boolean inhierarchy;
    int position;
    Hierarchy hierarchy;
    int limit = 0;
    ArrayList<String> members = new ArrayList<>();
    ArrayList<String> permissions = new ArrayList<>();
    public Jobs(String name, String prefix, Hierarchy hierarchy, int position, int limit) {
        this.name = name;
        if (isExist()) {
            load();
            return;
        }
        this.prefix = prefix;
        this.inhierarchy = true;
        this.hierarchy = hierarchy;
        this.position = position;
        this.limit = limit;
    }

    public Jobs(String name, int limit) {
        this.name = name;
        if (isExist()) {
            load();
            return;
        }
        this.prefix = "[" + name + "]";
        this.inhierarchy = false;
        this.limit = limit;
    }

    public Jobs(String name) {
        this.name = name;
        if (isExist()) {
            load();
            return;
        }
        this.prefix = "[" + name + "]";
        this.inhierarchy = false;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String s) {
        this.prefix = s;
        save();
    }

    public boolean isInhierarchy() {
        return this.inhierarchy;
    }

    public Hierarchy getHierarchy() {
        return this.hierarchy;
    }

    public void setHierarchy(Hierarchy h) {
        this.hierarchy = h;
        inhierarchy = true;
        save();
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int pos) {
        this.position = pos;
        save();
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
        save();
    }

    public ArrayList<String> getPermission() {
        return permissions;
    }

    public ArrayList<String> getMembers() {
        return this.members;
    }

    public void removeHierarchy() {
        inhierarchy = false;
        save();
    }

    public boolean addMember(String p) {
        if (this.members.contains(p)) return true;
        if (Main.haveJob(p)) {
            Main.getJob(p).removeMember(p);
        }
        getPermission().stream().forEach( perm-> Main.addPermission(UUID.fromString(p), perm));
        int truelimit = limit;
        if (limit == 0) {
            truelimit = Integer.MAX_VALUE;
        }
        if (this.members.size() + 1 > truelimit) {
            return false;
        } else {
            this.members.add(p);
            save();
            return true;
        }
    }

    public void removeMember(String p) {
        if (this.members.contains(p)) {
            getPermission().stream().forEach(perm->{
                Main.removePermission(UUID.fromString(p),perm);
            });
            this.members.remove(p);
            save();
        }

    }


    public boolean isExist() {
        if (ConfigManager.getInstance().getConfig("jobs.yml").contains(name)) return true;
        return false;
    }

    public void load() {
        members.clear();
        this.permissions.clear();
        this.prefix = ConfigManager.getJob().getString(name + ".prefix");
        this.inhierarchy = ConfigManager.getJob().getBoolean(name + ".hierarchy");
        if (this.inhierarchy) {
            this.hierarchy = new Hierarchy(ConfigManager.getJob().getString(name + ".hierarchyname"));
        }
        this.position = ConfigManager.getJob().getInt(name + ".position");
        this.limit = ConfigManager.getJob().getInt(name + ".limit");
        if (ConfigManager.getJob().contains(name + ".permission")) {
            this.permissions = (ArrayList<String>) ConfigManager.getJob().getStringList(name+".permission");
        }
        if (ConfigManager.getJob().contains(name + ".members")) {
            for (Object s : ConfigManager.getJob().getList(name + ".members")) {
                if (s instanceof String) {
                    members.add((String) s);
                }

            }
        }

    }

    public void save() {
        ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".prefix", prefix);
        ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".hierarchy", inhierarchy);
        if(permissions.size() == 0){
            permissions.add("example.permission");
        }
        ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".permission", permissions);
        if (inhierarchy) {
            ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".hierarchyname", hierarchy.getName());
        } else {
            ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".hierarchyname", "");
        }
        ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".position", position);
        ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".limit", limit);
        if (members.isEmpty()) {
            ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".members", members);
        } else {
            ConfigManager.getInstance().setData(ConfigManager.getJob(), name + ".members", members);
        }
        members.clear();
    }


}

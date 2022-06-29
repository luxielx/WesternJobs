package Luxielx.Main;

import java.util.ArrayList;

public class Hierarchy {

    ArrayList<String> joblist = new ArrayList<>();
    String name;

    public Hierarchy(String name) {
        this.name = name;
        if (isExist()) {
            load();
        } else {
            save();
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean isExist() {
        if (ConfigManager.getInstance().getConfig("config.yml").contains(name)) return true;
        return false;
    }

    public Jobs promote(String uuid) {
        if (!Main.haveJob(uuid)) return null;
        Jobs j = Main.getJob(uuid);
        if (joblist.contains(j.getName())) {
            if (j.getPosition() > 1) {
                Jobs current = j;
                int smallest = 100;
                for (String newj : joblist) {
                    if (newj.equals(j.getName())) continue;
                    Jobs jj = new Jobs(newj);
                    if (jj.getPosition() < j.getPosition()) {
                        if (j.getPosition() - jj.getPosition() < smallest) {

                            smallest = j.getPosition() - jj.getPosition();
                            current = jj;
                        }
                    }
                }
                return current;

            }
        }
        return null;
    }


    public Jobs demote(String uuid) {
        if (!Main.haveJob(uuid)) return null;
        Jobs j = Main.getJob(uuid);
        if (joblist.contains(j.getName())) {
            Jobs current = j;
            int smallest = 100;
            for (String newj : joblist) {
                if (newj.equals(j.getName())) continue;
                Jobs jj = new Jobs(newj);
                if (j.getPosition() < jj.getPosition()) {
                    if (jj.getPosition() - j.getPosition() < smallest) {
                        smallest = jj.getPosition() - j.getPosition();

                        current = jj;
                    }
                }
            }
            if (current.getName().equals(j.getName())) return null;
            return current;
        }
        return null;
    }

    public void addJob(String name) {
        if (!joblist.contains(name)) {
            joblist.add(name);
            save();
        }
    }

    public void removeJob(String name) {
        if (joblist.contains(name)) {
            joblist.remove(name);
            save();
        }


    }


    public void load() {
        joblist.clear();
        if (isExist()) {
            for (Object s : ConfigManager.getHierarchy().getList(name)) {
                if (s instanceof String) {

                    joblist.add((String) s);
                }

            }
        }
    }

    public void save() {
        if (joblist.isEmpty()) {
            ConfigManager.getInstance().setData(ConfigManager.getHierarchy(), name, null);
        } else {
            ConfigManager.getInstance().setData(ConfigManager.getHierarchy(), name, joblist);
        }
        joblist.clear();
        load();


    }


}

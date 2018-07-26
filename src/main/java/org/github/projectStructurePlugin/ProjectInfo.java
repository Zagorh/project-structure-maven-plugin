package org.github.projectStructurePlugin;

import java.util.HashMap;
import java.util.Map;

public class ProjectInfo {

    private String name;
    Map<String, ProjectInfo> subProjects;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ProjectInfo> getSubProjects() {
        if (subProjects == null) {
            subProjects = new HashMap<String, ProjectInfo>();
        }
        return subProjects;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "name='" + name + '\'' +
                ", subProjects=" + subProjects.values() +
                '}';
    }
}

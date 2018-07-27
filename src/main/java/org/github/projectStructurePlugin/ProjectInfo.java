package org.github.projectStructurePlugin;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfo {

    private String name;
    List<ProjectInfo> subProjects;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProjectInfo> getSubProjects() {
        if (subProjects == null) {
            subProjects = new ArrayList<ProjectInfo>();
        }
        return subProjects;
    }

    public void setSubProjects(List<ProjectInfo> subProjects) {
        this.subProjects = subProjects;
    }

    @Override
    public String toString() {
        return "ProjectInfo{" +
                "name='" + name + '\'' +
                ", subProjects=" + subProjects +
                '}';
    }
}

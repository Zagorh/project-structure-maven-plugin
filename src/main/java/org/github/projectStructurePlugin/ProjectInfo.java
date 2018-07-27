package org.github.projectStructurePlugin;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfo {

    private String name;
    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;
    List<ProjectInfo> subProjects;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
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
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", packaging='" + packaging + '\'' +
                ", subProjects=" + subProjects +
                '}';
    }
}

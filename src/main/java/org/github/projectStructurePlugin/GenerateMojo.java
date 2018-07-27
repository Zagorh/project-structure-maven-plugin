package org.github.projectStructurePlugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Mojo(name = "generate", aggregator = true, defaultPhase = LifecyclePhase.INITIALIZE)
public class GenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "true", required = true, property = "showParents")
    private boolean showParents;

    @Parameter(defaultValue = "false", required = true, property = "showParentsStructure")
    private boolean showParentsStructure;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Graph<MavenProject, DefaultEdge> graph = new DefaultDirectedGraph<MavenProject, DefaultEdge>(DefaultEdge.class);

        List<MavenProject> collectedProjects = mavenProject.getCollectedProjects();
        graph.addVertex(mavenProject);
        for (MavenProject project : collectedProjects) {
            graph.addVertex(project);
            graph.addVertex(project.getParent());

            graph.addEdge(project.getParent(), project);
        }

        ProjectInfo projectInfo = generateRootProjectInfo(graph);

        if (showParents) {
            projectInfo = processParents(mavenProject, projectInfo);
        }

        getLog().info(projectInfo.toString());

    }

    private ProjectInfo generateRootProjectInfo(Graph<MavenProject, DefaultEdge> graph) {
        Queue<MavenProject> projectQueue = new LinkedList<MavenProject>();
        projectQueue.add(mavenProject);

        ProjectInfo masterProjectInfo = new ProjectInfo();
        masterProjectInfo.setName(mavenProject.getName());
        Map<MavenProject, ProjectInfo> mavenProjectProjectInfoMap = new HashMap<MavenProject, ProjectInfo>();
        mavenProjectProjectInfoMap.put(mavenProject, masterProjectInfo);

        ProjectInfo currentProjectInfo = null;
        while(!projectQueue.isEmpty()) {
            MavenProject elem = projectQueue.poll();
            currentProjectInfo = mavenProjectProjectInfoMap.get(elem);
            for(DefaultEdge edge : graph.outgoingEdgesOf(elem)) {
                MavenProject subElem = graph.getEdgeTarget(edge);
                projectQueue.add(subElem);

                ProjectInfo subProjectInfo = new ProjectInfo();
                subProjectInfo.setName(subElem.getName());

                currentProjectInfo.getSubProjects().add(subProjectInfo);
                mavenProjectProjectInfoMap.put(subElem, subProjectInfo);
            }
        }

        return masterProjectInfo;
    }

    private ProjectInfo processParents(MavenProject mavenProject, ProjectInfo projectInfo) {
        MavenProject currentMavenProject = mavenProject.getParent();
        while (currentMavenProject != null) {
            ProjectInfo parentProjectInfo = new ProjectInfo();
            parentProjectInfo.setName(currentMavenProject.getName());
            parentProjectInfo.getSubProjects().add(projectInfo);

            projectInfo = parentProjectInfo;
            currentMavenProject = currentMavenProject.getParent();
        }

        return projectInfo;
    }

}

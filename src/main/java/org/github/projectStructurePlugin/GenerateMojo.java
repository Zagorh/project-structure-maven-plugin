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
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.List;

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

        getLog().info(graph.toString());

        GraphIterator<MavenProject, DefaultEdge> iterator = new TopologicalOrderIterator<MavenProject, DefaultEdge>(graph);

        ProjectInfo parentProject = null;
        MavenProject parentMavenProject = null;
        while(iterator.hasNext()) {
            MavenProject project = iterator.next();

            if (parentProject == null) {
                parentProject = new ProjectInfo();
                parentProject.setName(project.getArtifactId());
                parentMavenProject = project;
                continue;
            } else if (parentMavenProject == project.getParent()) {
                ProjectInfo projectInfo = new ProjectInfo();
                projectInfo.setName(project.getName());
                parentProject.getSubProjects().put(project.getName(), projectInfo);
            } else {
                parentMavenProject = project.getParent();
                
            }

        }

    }

}

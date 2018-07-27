package org.github.projectStructurePlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Mojo(name = "generate", aggregator = true, defaultPhase = LifecyclePhase.INITIALIZE)
public class GenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", required = true, property = "outputDirectory")
    private File buildDirectory;

    @Parameter(defaultValue = "project-structure", required = true, property = "outputFilename")
    private String filename;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "true", required = true, property = "projectstructure.showParents")
    private boolean showParents;

    @Parameter(defaultValue = "false", required = true, property = "projectstructure.showParentsStructure")
    private boolean showParentsStructure;

    @Parameter(defaultValue = "false", required = true, property = "projectstructure.verbose")
    private boolean verbose;

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

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();

        String jsonString = gson.toJson(projectInfo);

        if (verbose) {
            getLog().info(jsonString);
        }

        File jsonFile = new File(buildDirectory, filename + ".json");

        BufferedOutputStream bs = null;
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(jsonFile);
            bs = new BufferedOutputStream(fs);

            bs.write(jsonString.getBytes());
            bs.flush();

        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }

                if (bs != null) {
                    bs.close();
                }
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

    }

    private ProjectInfo generateRootProjectInfo(Graph<MavenProject, DefaultEdge> graph) {
        Queue<MavenProject> projectQueue = new LinkedList<MavenProject>();
        projectQueue.add(mavenProject);

        ProjectInfo masterProjectInfo = new ProjectInfo();
        setProjectInfoFromMavenProject(mavenProject, masterProjectInfo);

        Map<MavenProject, ProjectInfo> mavenProjectProjectInfoMap = new HashMap<MavenProject, ProjectInfo>();
        mavenProjectProjectInfoMap.put(mavenProject, masterProjectInfo);

        ProjectInfo currentProjectInfo = null;
        while (!projectQueue.isEmpty()) {
            MavenProject elem = projectQueue.poll();
            currentProjectInfo = mavenProjectProjectInfoMap.get(elem);
            for (DefaultEdge edge : graph.outgoingEdgesOf(elem)) {
                MavenProject subElem = graph.getEdgeTarget(edge);
                projectQueue.add(subElem);

                ProjectInfo subProjectInfo = new ProjectInfo();
                setProjectInfoFromMavenProject(subElem, subProjectInfo);

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

    private void setProjectInfoFromMavenProject(MavenProject mavenProject, ProjectInfo projectInfo) {
        projectInfo.setName(mavenProject.getName());
        projectInfo.setGroupId(mavenProject.getGroupId());
        projectInfo.setArtifactId(mavenProject.getArtifactId());
        projectInfo.setVersion(mavenProject.getVersion());
        projectInfo.setPackaging(mavenProject.getPackaging());
    }

}

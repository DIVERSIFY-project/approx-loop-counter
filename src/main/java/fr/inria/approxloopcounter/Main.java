package fr.inria.approxloopcounter;

import fr.inria.approxloops.sqlite.RowWrite;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import spoon.Launcher;
import spoon.reflect.declaration.CtType;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by elmarce on 30/06/16.
 */
public class Main {

    //Common Math
    //public static String inputPath =
    //public static String inputPath =
    //public static String inputPath =
    //public static String inputPath =
    String dataPath = "/home/elmarce/PROJECTS/DATA";
    //ArrayList<String> errors;

    class Row {
        int loopCount = 0;
        int approxCount = 0;
    }


    HashMap<String, Row> loopsPerPackage;


    public static void main(String[] args) throws IOException, XmlPullParserException {

        /*
        new Main().countLoops(new String[]{
                "/home/elmarce/PROJECTS/DATA/jsyn-master",
                "/home/elmarce/PROJECTS/DATA/openimaj-master",
                "/home/elmarce/PROJECTS/DATA/openrocket-master",
                "/home/elmarce/PROJECTS/DATA/sonarqube-master",
                "/home/elmarce/PROJECTS/DATA/jmonkeyengine-master",
                "/home/elmarce/PROJECTS/DATA/mahout-master",
                "/home/elmarce/PROJECTS/DATA/lucene-solr-master"

        });*/


    }


    private void countLoopsToDB(String dbPath, String path, String projectName) throws IOException, XmlPullParserException {
        RowWrite rowWrite = new RowWrite(dbPath, "loopsperpackage", "LOOPS", "APPROXIMATE", "PERCENT", "PACKAGE");
        loopsPerPackage = new HashMap<>();
        walk(path);
        int subTotalLoops = 0;
        int subTotalApprox = 0;
        for (Map.Entry<String, Row> e : loopsPerPackage.entrySet()) {
            int loops = e.getValue().loopCount;
            int approx = e.getValue().approxCount;
            subTotalLoops += loops;
            subTotalApprox += approx;
            double percent = (double) approx / (double) loops * 100;
            rowWrite.write(loops, approx, percent, e.getKey());
        }
        double percent = (double) subTotalApprox / (double) subTotalLoops * 100;
        rowWrite.write(subTotalLoops, subTotalApprox, percent, projectName);
        loopsPerPackage.clear();
    }

    private void countLoops(String[] paths) throws IOException, XmlPullParserException {
        //errors = new ArrayList<>();
        loopsPerPackage = new HashMap<>();
        System.out.println("|Project | Total | Approximable | Percent | ");
        System.out.println("|--- | --- | --- | --- | ");
        for (String s : paths) {
            walk(s);
            int subTotalLoops = 0;
            int subTotalApprox = 0;
            for (Map.Entry<String, Row> e : loopsPerPackage.entrySet()) {
                int loops = e.getValue().loopCount;
                int approx = e.getValue().approxCount;
                subTotalLoops += loops;
                subTotalApprox += approx;
                double percent = (double) approx / (double) loops * 100;
                System.out.println("| " + e.getKey() + " | " + loops + " | " + approx + " | " + percent + " | ");
            }
            double percent = (double) subTotalApprox / (double) subTotalLoops * 100;
            System.out.println("| - | - | - | - |");
            System.out.println("| Subtotal  | " + subTotalLoops + " | " + subTotalApprox + " | " + percent + " | ");
            System.out.println("| - | - | - | - |");

            loopsPerPackage.clear();
        }

    }

    public void countFromFile(String srcPathFile) throws IOException {
        //errors = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(srcPathFile));
        String line;
        while ((line = br.readLine()) != null) {
            int last = line.contains(":") ? line.lastIndexOf(":") : line.length() - 2;
            line = line.substring(2, last);
            String prjName = line;
            line = dataPath + "/" + line;
            if (new File(line).exists()) countProjectLoops(line);
        }
        System.out.println("ERRORS:");
        //for (String s : errors) System.out.println(s);
    }

    public void walk(String path) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
                //System.out.println("Entering :" + f.getAbsoluteFile());
            } else {
                if (f.getName().endsWith(".java")) {
                    //System.out.println("Analysing:" + f.getAbsoluteFile());
                    countProjectLoops(f.getAbsolutePath());
                }
            }
        }
    }
/*
    private ArrayList<String> getProjectSources(String line) throws IOException, XmlPullParserException {
        File pom = new File(line);

        line = line.substring(0, line.lastIndexOf("/"));

        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        FileReader reader = new FileReader(pom);
        Model model = mavenReader.read(reader);
        model.setPomFile(pom);
        MavenProject proj = new MavenProject(model);

        ArrayList<String> sources = new ArrayList<String>();

        String module = proj.getProperties().getProperty("relative-top-level");
        if (module != null) {
            module += "/" + proj.getProperties().getProperty("module-directory");
        }
        Path modulePath = Paths.get(module);
        Path pomPath = Paths.get(line);

        sources.add(pomPath.resolve(modulePath).toString());

        for (String m : proj.getModules()) {
            sources.addAll(getProjectSources(line + "/" + m + "/pom.xml"));
        }
        return sources;
    }
*/

    private void countProjectLoops(String path) {
        try {
            final Launcher launcher = new Launcher();
            launcher.getEnvironment().setNoClasspath(true);
            launcher.addInputResource(path);
            launcher.setSourceOutputDirectory("./target/trash");
            ApproxLoopCounterProcessor detector = new ApproxLoopCounterProcessor();
            launcher.addProcessor(detector);
            launcher.buildModel();
            launcher.process();
            //OUTPUT:


            if (detector.getLoopCount() > 0) {
                try {
                    for (CtType t : launcher.getFactory().Class().getAll()) {
                        Row r;
                        String packName = t.getPackage().getQualifiedName();
                        if (loopsPerPackage.containsKey(packName)) {
                            r = loopsPerPackage.get(packName);
                        } else {
                            r = new Row();
                            loopsPerPackage.put(packName, r);
                        }
                        r.approxCount += detector.getApproximableLoopCount();
                        r.loopCount += detector.getLoopCount();

                        break;
                    }
                } catch (Exception ex) {
                    System.out.println("Error " + ex.getMessage() + " at: " + path);
                }
            }
            /*
                System.out.println("| " + name + " | " + detector.getLoopCount() + " | " + detector.getApproximableLoopCount() + " | " +
                        +percent + " | ");*/

        } catch (Exception e) {
            try {
                System.out.println("Error " + e.getMessage() + " at: " + path);
            } catch (Exception e1) {
                System.out.println("Unknown error in: " + path);
            }
        }
    }

}

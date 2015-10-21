package xtools.gsea.GseaMain;


import org.apache.commons.io.FilenameUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class ClueGsea {

    //Read from properties file
    private static String ANNOTATIONS_DIRECTORY = "annotations";
    private static String GENESETS_DIRECTORY = "gene_sets";
    private static String CHIP_FILE;
    private static String GENE_SET_DATABASE;
    static final String ERROR_FILE_PREFIX = "error";

    static {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("./gsea.properties"));
            ANNOTATIONS_DIRECTORY = props.getProperty("annotations");
            GENESETS_DIRECTORY = props.getProperty("gene_sets");
            CHIP_FILE = props.getProperty("chip_file");
            GENE_SET_DATABASE = props.getProperty("gene_set_database");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
    public static Map<String, Object> yamlToJavaMap(final URL urlToConfigFile) throws Exception {

        //download data into from S3
        InputStream input = urlToConfigFile.openStream();
        Yaml yaml = new Yaml();
        return (Map<String, Object>) yaml.load(input);
    }
    public static boolean hasProtocol(String value) {
        try {
            new URL(value.trim());
            return true;

        } catch (Exception ee) {

        }
        return false;
    }

    public static URL extractURL(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                if (hasProtocol(value)) {
                    return new URL(value);
                } else {
                    //we assume it is a file path
                    final String prefix = FilenameUtils.getPrefix(value);
                    if (prefix != null && !prefix.isEmpty()) {
                        return new File(value).toURI().toURL();
                    }
                }
            } catch (Exception ee) {

            }
        }
        return null;
    }

    public static String[] toStringArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    public static String renameGseaoutputDirectory(File outputDirectory, String rptLabel, String errorLabel) {
        GseaFileFilter gseaCreatedDirectoryFilter = new GseaFileFilter(rptLabel);
        File[] gseaCreatedDirectory = outputDirectory.listFiles(gseaCreatedDirectoryFilter);
        if (gseaCreatedDirectory == null || gseaCreatedDirectory.length == 0) {
            gseaCreatedDirectoryFilter = new GseaFileFilter(errorLabel);
            gseaCreatedDirectory = outputDirectory.listFiles(gseaCreatedDirectoryFilter);
        }
        final File src = gseaCreatedDirectory[0];
        final File destination = new File(outputDirectory, outputDirectory.getName());

        final boolean renameTo = src.renameTo(destination);
        if (renameTo) {
            return destination.getAbsolutePath();
        }
        return null;

    }
    public static List<String> toList(final File outDirectory, final Map<String, Object> data) {
        final List<String> dataToAdd = new ArrayList<String>();
        dataToAdd.add("-out " + outDirectory.getAbsolutePath());

        if (data.get("chip") == null) {
            dataToAdd.add("-chip " + downloadFile(new File(ANNOTATIONS_DIRECTORY), CHIP_FILE));
        }
        if (data.get("gmx") == null) {//path to gmx file
            dataToAdd.add("-gmx " + downloadFile(new File(GENESETS_DIRECTORY), GENE_SET_DATABASE));
        }
        Set<String> keys = data.keySet();


        for (String key : keys) {
            String value = data.get(key).toString().trim();
            key = key.trim();
            if (hasProtocol(value)) {
                value = downloadFile(outDirectory, value);
            } else {//these are local files
                if (key.trim().equalsIgnoreCase("chip")) {//path to remote chip file
                    value = downloadFile(new File(ANNOTATIONS_DIRECTORY), value);
                } else if (key.equalsIgnoreCase("gmx")) {//path to gmx file
                    value = downloadFile(new File(GENESETS_DIRECTORY), value);
                }
            }
            dataToAdd.add("-" + key + " " + value);
        }
        return dataToAdd;


    }



    public static String downloadFile(final File destinationDirectory, final String value) {
        FileOutputStream fos = null;
        try {

            final String fileName = FilenameUtils.getName(value);

            final File file = new File(destinationDirectory, fileName);
            if (!file.exists()) {
                //download data from S3
                final URL input = extractURL(value);
                if (input != null) {
                    final ReadableByteChannel rbc = Channels.newChannel(input.openStream());
                    fos = new FileOutputStream(file);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
            }
            return file.getAbsolutePath();
        } catch (Exception ee) {
            ee.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





    public static void main(String[] args) throws Exception {
        //pass the path to config file and the output folder
        final String configFile = args[0];
        final URL urlToConfigFile = ClueGsea.extractURL(configFile);
        if(urlToConfigFile == null){
            System.out.println("Could not find config file: " + configFile);
            System.exit(1);
        }
        System.out.println("Could config file found: " + configFile);

        final File outputDirectory = new File(args[1]);
        outputDirectory.mkdirs();

        final Map<String, Object> yamlToJavaMap = yamlToJavaMap(urlToConfigFile);
        final String rptLabel = (String) yamlToJavaMap.get("rpt_label");

        Runtime.getRuntime().addShutdownHook(new GseaThread(outputDirectory, rptLabel));

        final List<String> list = toList(outputDirectory, yamlToJavaMap);
        final String configArgs[] = toStringArray(list);

        xtools.gsea.Gsea.main(configArgs);

    }

    public static class GseaThread extends Thread {
        final File outputDirectory;
        final String rptLabel;


        public GseaThread(final File outDirectory, final String rptLabel) {
            this.outputDirectory = outDirectory;
            this.rptLabel = rptLabel;
        }

        public void run() {
            try {
                String newDirectory = renameGseaoutputDirectory(this.outputDirectory, this.rptLabel, ERROR_FILE_PREFIX);
                if (newDirectory != null) {
                    System.out.println("Renamed output directory successfully to " + newDirectory);
                } else {
                    System.out.println("Renamed of output directory failed");
                }
                System.out.println("Shutdown completed ...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class GseaFileFilter implements FilenameFilter {

        /**
         * @param name
         */
        private String fileNamePrefix;

        public GseaFileFilter(final String fileNamePrefix) {
            this.fileNamePrefix = fileNamePrefix;
        }

        public boolean accept(File dir, String name) {
            String lowercaseName = name.toLowerCase();
            if (lowercaseName.startsWith(this.fileNamePrefix)) {
                return true;
            } else {
                return false;
            }
        }
    }
}

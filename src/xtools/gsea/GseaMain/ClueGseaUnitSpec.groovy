package xtools.gsea.GseaMain

import org.apache.commons.io.FilenameUtils
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Created by jasiedu on 10/20/15.
 */
@Unroll
class ClueGseaUnitSpec extends spock.lang.Specification {
    String test = "https://s3.amazonaws.com/data.lincscloud.org/api/jasiedu%40broadinstitute.org/results/Oct21/my_analysis.sig_gsea_tool.job1445480127198/config.yaml"
    @Shared
    private String originalFileName = new File("my_gsea_13343254").getAbsolutePath();
    @Shared
    private String STRING_WITH_PROTOCOL = "http://clue.io";
    @Shared
    private String REMOTE_YAML_FILE = "https://s3.amazonaws.com/data.lincscloud.org/api/jasiedu/results/Oct12/my_analysis.sig_gsea_tool.job1444673999870/config.yaml";
    @Shared
    private File LOCAL_YAML_FILE = new File("test_resources", "test.yaml");
    @Shared
    private String STRING_WITH_OUT_PROTOCOL = "clue.io";
    final private String parentDirName = "stuff3";

    def setup() {

    }

    def cleanup() {

    }

    def "YamlToJavaMap #desc"() {
        given:
        final URL url = ClueGsea.extractURL(fileName);
        when:
        final Map<String, Object> objectMap = ClueGsea.yamlToJavaMap(url);
        then:
        objectMap != null
        !objectMap.isEmpty()
        where:
        desc          | fileName
        "Remote File" | REMOTE_YAML_FILE
        "Local File"  | LOCAL_YAML_FILE.getAbsolutePath()
    }


    def "HasProtocol #desc"() {
        when:
        boolean bool = ClueGsea.hasProtocol(url)
        then:
        assert bool == expectedResults

        where:
        desc                      | url                      | expectedResults
        "String with protocol"    | STRING_WITH_PROTOCOL     | true
        "String without protocol" | STRING_WITH_OUT_PROTOCOL | false
    }

    def "ExtractURL #desc"() {
        when:
        URL url = ClueGsea.extractURL(urlString);
        then:
        assert url?.toString() == expectedURL

        where:
        desc                         | urlString                | expectedURL
        "Empty string"               | ""                       | null
        "Null String"                | null                     | null
        "String with protocol"       | STRING_WITH_PROTOCOL     | "http://clue.io"
        "String without protocol"    | STRING_WITH_OUT_PROTOCOL | null
        "With an existing file path" | originalFileName         | "file:" + originalFileName
    }

    def "ToStringArray"() {
        given:
        final List<String> stringList = new ArrayList<String>();
        stringList.add("a");
        stringList.add("b");
        when:
        final String[] stringArray = ClueGsea.toStringArray(stringList);
        then:
        stringList.size() == stringArray.length;
    }


    def "ToList"() {
        given:
        File f = new File(parentDirName);
        URL url = ClueGsea.extractURL(LOCAL_YAML_FILE.getAbsolutePath());
        final Map<String, Object> objectMap = ClueGsea.yamlToJavaMap(url);
        when:
        final List<String> stringList = ClueGsea.toList(f, objectMap);

        then:
        stringList.size() == objectMap.size() + 3;
    }

    def "DownloadFile"() {
        given:
        final File f = new File("download_test_file");
        f.mkdirs()
        when:
        final String downloadedFile = ClueGsea.downloadFile(f, LOCAL_YAML_FILE.getAbsolutePath());
        then:
        downloadedFile != null
        FilenameUtils.getName(downloadedFile) == FilenameUtils.getName(LOCAL_YAML_FILE.getAbsolutePath())

    }
}


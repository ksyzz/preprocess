package csdc.tool.preprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 停词表
 * @author huangzw
 * @version 1.0
 * @since <pre>2017/8/14</pre>
 */
public class StopWords {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopWords.class);
    private static List<String> STOP_WORDS;
    private StopWords() {}

    static {
        STOP_WORDS = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("stopwords.txt")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replace("\uFEFF", "");
                STOP_WORDS.add(line);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("无法找到停词表文件");
            System.exit(-1);
        } catch (IOException e) {
            LOGGER.error("无法读取停词表文件");
            System.exit(-1);
        }
    }

    public static boolean contains(String string) {
        boolean contains = STOP_WORDS.contains(string);
        return contains;
    }
}

package csdc.tool.preprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 将预处理后的文本存储到txt
 * @author huangzw
 * @version 1.0
 * @since <pre>2017/8/14</pre>
 */

public class TxtFileWriter {
    private TxtFileWriter() {}

    /**
     * 输出文本到对应文件中，以空格分词
     * @param path      目标文件路径
     * @param sentence_words     待输出的词列表
     * @throws TextWriteException
     */
    public static void write(String path, List<List<String>> sentence_words) throws TextWriteException {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))) {
            for (List<String> words : sentence_words) {
                for (String word : words) {
                    bufferedWriter.write(word);
                    bufferedWriter.write(" ");
                }
                bufferedWriter.write("\n");
            }
        } catch (IOException e) {
            throw new TextWriteException(path);
        }
    }
    /**
     * 输出文本到对应文件中，以空格分词
     * @param path      目标文件路径
     * @param content     待输出的词列表
     * @throws TextWriteException
     */
    public static void write(String path, String content) throws TextWriteException {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))) {
            bufferedWriter.write(content);
        } catch (IOException e) {
            throw new TextWriteException(path);
        }
    }
}

package csdc.tool.preprocessor;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.Data;
import lombok.NonNull;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档预处理
 * @author huangzw
 * @version 1.0
 * @since <pre>2017/8/14</pre>
 */
@Data
public class PreProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreProcessor.class);

    @NonNull
    private String srcPath;
    @NonNull
    private String dstPath;
    @NonNull
    private String startContent = "一、";
    @NonNull
    private String endContent = "三、";
    private String section1;
    private String section2;
    private String section3;
    public int err = 0;

    public PreProcessor(String srcPath, String dstPath, String section1, String section2, String section3) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.section1 = section1;
        this.section2 = section2;
        this.section3 = section3;
        totalCount = 0;
        successCount = 0;
        startTime = System.currentTimeMillis();
    }

    public PreProcessor(String srcPath, String dstPath) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        totalCount = 0;
        successCount = 0;
        startTime = System.currentTimeMillis();
    }

    private String dealword = "dealword";
    private File srcRoot;
    private File dstRoot;
    /**
     * 读取异常的文件
     */
    private List<String> errFiles = new ArrayList<>();
    /**
     * 不存在定义的开始内容和截止内容的文件
     */
    private List<String> errIndexFiles = new ArrayList<>();
    /**
     * 写入txt失败的文件
     */
    private List<String> errWriteFiles = new ArrayList<>();
    private long totalCount;
    private long successCount;
    private long startTime;


    /**
     *  每5s汇报进度
     */
    public void startReport() {
        Thread thread = new Thread(() -> {
            while (true) {
                report();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    LOGGER.debug("cannot sleep", e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 日志记录目前处理的进度
     */
    private void report() {
        LOGGER.info("Time(ms): {}\tSuccess: {}\tTotal: {}", System.currentTimeMillis() - startTime, successCount, totalCount);
    }


    /**
     * 预处理
     * @Param rootPath 停词/不停词路径名称
     */
    public void process() {
        srcRoot = new File(srcPath);
        dstRoot = new File(dstPath);
        if (!srcRoot.exists()) {
            LOGGER.error("source root does not exists.");
            System.exit(1);
            return;
        }
        if (!dstRoot.exists()) {
            dstRoot.mkdir();
        }
        process1(srcRoot, srcRoot.isFile() ? srcRoot.getParentFile() : srcRoot, dstPath);
        LOGGER.info("Finished processing from {} to {}", srcPath, dstPath);
    }

    /**
     * 分词
     * @param dir
     * @param srcRoot
     * @param dstRoot
     */
    private void process(File dir, File srcRoot, File dstRoot) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                process(file, srcRoot, dstRoot);
            }
        } else {
            if (!dir.getName().endsWith(".doc")) {
                LOGGER.debug("Bad file type: ", dir.getAbsolutePath());
            }

            try {
                String rawBody = extractContent(dir);
                try {
                    String content = splitContent(rawBody, dir, true);

                    String dealword_dstPath;
                    dealword_dstPath = dstRoot.toPath().resolve(srcRoot.toPath().relativize(dir.toPath())) + ".txt";

                    String[] sentences = content.split("[?.!？。！；;]");
                    List<List<String>> dealword_sentence_words = new ArrayList<>();
                    Arrays.stream(sentences).forEach(s -> {
                        List<String> dealword_words = segment(s, true);
                        dealword_sentence_words.add(dealword_words);
                    });

                    try {
                        TxtFileWriter.write(dealword_dstPath, dealword_sentence_words);
                        successCount++;
                    } catch (TextWriteException e) {
                        errWriteFiles.add(dir.getAbsolutePath());
                        LOGGER.debug("Write txt error: ", e);
                    }
                } catch (BadApplicationContentException e) {
                    LOGGER.debug("Bad application content: ", e);
                    errIndexFiles.add(dir.getAbsolutePath());
                }


            } catch (Exception e) {
                LOGGER.error("Unexpected exception: ", e);
                errFiles.add(dir.getAbsolutePath());
            }
            totalCount++;
        }
    }

    /**
     * 部分次
     * @param dir
     * @param srcRoot
     * @param dstPath
     */
    public void process1(File dir, File srcRoot, String dstPath) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                process1(file, srcRoot, dstPath);
            }
        } else {
            if (!dir.getName().endsWith(".doc")) {
                LOGGER.debug("Bad file type: ", dir.getAbsolutePath());
            }

            try {
                String rawBody = extractContent(dir);
                try {
                    String content = splitContent(rawBody, dir, true);

                    String dealword_dstPath;
                    dstRoot = new File(dstPath + "\\target");
                    if (!dstRoot.exists()) {
                        dstRoot.mkdirs();
                    }
                    dealword_dstPath = dstRoot.toPath().resolve(srcRoot.toPath().relativize(dir.toPath())) + ".txt";

                    String content1 = splitContent(rawBody, dir, false);
                    dstRoot = new File(dstPath + "\\content");
                    if (!dstRoot.exists()) {
                        dstRoot.mkdirs();
                    }
                    String dealword_dstPath1;
                    dealword_dstPath1 = dstRoot.toPath().resolve(srcRoot.toPath().relativize(dir.toPath())) + ".txt";

                    try {
                        TxtFileWriter.write(dealword_dstPath, content);
                        TxtFileWriter.write(dealword_dstPath1, content1);
                        successCount++;
                    } catch (TextWriteException e) {
                        errWriteFiles.add(dir.getAbsolutePath());
                        LOGGER.debug("Write txt error: ", e);
                        err++;
                    }
                } catch (BadApplicationContentException e) {
                    LOGGER.debug("Bad application content: ");
                    errIndexFiles.add(dir.getAbsolutePath());
                    err++;

                }
            } catch (Exception e) {
                LOGGER.error("Unexpected exception: ", e);
                err++;
                errFiles.add(dir.getAbsolutePath());
            }
            totalCount++;
        }
    }



    /**
     * 分词并移除停词
     * @param content
     * @return
     */
    public List<String> segment(String content, boolean dealword) {
        List<Term> terms = HanLP.segment(content);
        List<String> result = terms.stream()
                .map(term -> term.word)
                .map(word -> word.trim())
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());
        if (dealword){
            return result.stream().filter(word -> !StopWords.contains(word)).collect(Collectors.toList());
        }else {
            return result;
        }
    }

    /**
     * 提取申请书中的主要内容
     * @param file
     * @return
     * @throws IOException                      文件读取异常
     * @throws TikaException                    Tika提取错误
     * @throws SAXException                     文档解析错误
     * @throws BadApplicationContentException   不符合申请书格式
     */
    private String extractContent(File file) throws IOException, TikaException, SAXException {
        //构建源文件流
        InputStream input=new FileInputStream(file);
//        HWPFDocument document = new HWPFDocument(input);
        //内容处理器
        BodyContentHandler textHandler=new BodyContentHandler();
        //元信息容器
        Metadata metaData=new Metadata();
        //根据文件MIME自动选择Parser
        Parser parser=new AutoDetectParser();
        //解析过程中使用的上下文对象
        ParseContext context=new ParseContext();

        parser.parse(input, textHandler, metaData, context);
        input.close();

        String rawBody = textHandler.toString();
        return rawBody;
    }

    /**
     * 获取文章中特定章节的内容
     * @param rawBody
     * @param file
     * @return
     * @throws BadApplicationContentException
     */
    private String splitContent(String rawBody, File file, boolean target) throws BadApplicationContentException, IOException {
        if (target) {
            startContent = section1;
            endContent = section2;
        } else {
            startContent = section2;
            endContent = section3;
        }
        int start = rawBody.indexOf(startContent + "\n");
        if (start == -1) {
            start = rawBody.indexOf(startContent + ":\n");
        }
        if (start == -1) {
            start = rawBody.indexOf(startContent + "：\n");
        }
        if (start == -1) {
            throw new BadApplicationContentException(file.getAbsolutePath());
        }
        rawBody = rawBody.substring(start);
        int end = rawBody.indexOf(endContent + "\n");
        if (end == -1) {
            end = rawBody.indexOf(endContent + ":\n");
        }
        if (end == -1) {
            end = rawBody.indexOf(endContent + "：\n");
        }
        if (end == -1) {
            throw new BadApplicationContentException(file.getAbsolutePath());
        }
        String body = rawBody.substring(0 + startContent.length(), end).trim().replace("\t", "");
        body = body.substring(0, body.lastIndexOf("\n")).replace("\n", "").replace(" ", "");
        return body;

    }

}

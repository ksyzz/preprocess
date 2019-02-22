package csdc.tool.preprocessor;

import java.util.List;


/**
 * @author fengqian
 * @since <pre>2018/10/26</pre>
 */
public class Deal {
    public static void main(String[] args) {
        String section1 = "研究目标";
        String section2 = "研究内容";
        String section3 = "难点";

        // 待处理的doc文档路径
        String docPath = "C:\\Users\\csdc01\\smdb_lda_data\\data\\doc\\2018";
        // 处理后生成的txt的存储路径
        String txtPath = "C:\\Users\\csdc01\\smdb_lda_data\\data\\doc\\2018";
        // 记录不存在开始和截止内容的文档
        List<String> errorIndexDocs;

        // 获取需要更新的项目
        PreProcessor preProcessor = new PreProcessor(docPath, txtPath, section1, section2, section3);
        preProcessor.process();
        errorIndexDocs = preProcessor.getErrIndexFiles();
        errorIndexDocs.forEach(s -> {
            System.out.println("文档起止内容异常：" + s);
        });
        System.out.println("异常文件数量为:" + preProcessor.err);
    }
}

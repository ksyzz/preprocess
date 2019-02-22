package csdc.tool.preprocessor;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档申请书内容不全
 * @author huangzw
 * @version 1.0
 * @since <pre>2017/8/14</pre>
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BadApplicationContentException extends Exception {
    String filePath;
}

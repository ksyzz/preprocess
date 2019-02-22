package csdc.tool.preprocessor;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 写输出文件时失败
 * @author huangzw
 * @version 1.0
 * @since <pre>2017/8/14</pre>
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TextWriteException extends Exception {
    String filePath;
}

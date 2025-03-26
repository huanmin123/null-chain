package com.gitee.huanminabc.nullchain.register;

import com.gitee.huanminabc.nullchain.tool.base64.*;
import com.gitee.huanminabc.nullchain.tool.base64.Base64StrDecodeTool;
import com.gitee.huanminabc.nullchain.tool.file.BytesToWriteFileTool;
import com.gitee.huanminabc.nullchain.tool.file.ReadFileToStrTool;
import com.gitee.huanminabc.nullchain.tool.file.ZipToBytesByteTool;
import com.gitee.huanminabc.nullchain.tool.hash.*;
import com.gitee.huanminabc.nullchain.tool.hash.Sha256Tool;
import com.gitee.huanminabc.nullchain.tool.hash.Sha512Tool;
import com.gitee.huanminabc.nullchain.tool.NullToolFactory;
import com.gitee.huanminabc.nullchain.tool.object.DeserializeTool;
import com.gitee.huanminabc.nullchain.tool.object.SerializeTool;
import com.gitee.huanminabc.nullchain.tool.other.NumberToCnTool;
import com.gitee.huanminabc.nullchain.tool.base64.Base64StrEncodeTool;
import com.gitee.huanminabc.nullchain.tool.file.StrToWriteFileTool;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class RegisterTool {

    static {
        //注册内置的转换器

        //1.数字转中文
        NullToolFactory.registerTool(NumberToCnTool.class);

        //2.hash
        NullToolFactory.registerTool(MD5Tool.class);
        NullToolFactory.registerTool(Sha1Tool.class);
        NullToolFactory.registerTool(Sha256Tool.class);
        NullToolFactory.registerTool(Sha512Tool.class);

        //3.base64
        NullToolFactory.registerTool(Base64ByteDecodeTool.class);
        NullToolFactory.registerTool(Base64ByteEncodeTool.class);
        NullToolFactory.registerTool(Base64StrDecodeTool.class);
        NullToolFactory.registerTool(Base64StrEncodeTool.class);

        NullToolFactory.registerTool(ReadFileToStrTool.class);
        NullToolFactory.registerTool(StrToWriteFileTool.class);
        NullToolFactory.registerTool(BytesToWriteFileTool.class);
        NullToolFactory.registerTool(ZipToBytesByteTool.class);

        NullToolFactory.registerTool(SerializeTool.class);
        NullToolFactory.registerTool(DeserializeTool.class);
    }
}

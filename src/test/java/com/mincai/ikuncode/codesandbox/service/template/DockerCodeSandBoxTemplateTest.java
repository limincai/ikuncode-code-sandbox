package com.mincai.ikuncode.codesandbox.service.template;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * @author limincai
 */
@SpringBootTest
class DockerCodeSandBoxTemplateTest {

    @Test
    void executeCode() {
        DockerCodeSandBoxTemplate codeSandBox = new JavaDockerCodeSandBox();
        String userCode = FileUtil.readString("testcode/Main.java", StandardCharsets.UTF_8);
        System.out.println(codeSandBox.executeCode(userCode, Collections.singletonList("你好")));
    }
}
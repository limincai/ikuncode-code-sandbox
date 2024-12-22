package com.mincai.ikuncode.codesandbox.service.template;

import com.mincai.ikuncode.codesandbox.constant.DockerImage;
import com.mincai.ikuncode.codesandbox.model.ExecuteCodeResponse;

import java.util.List;

/**
 * @author limincai
 */
public class JavaDockerCodeSandBox extends DockerCodeSandBoxTemplate {

    public JavaDockerCodeSandBox() {
        super();
        super.globalCodeFileName = "Main.java";
        super.language = "java";
        super.dockerImage = DockerImage.JAVA;
        super.volumeName = "/app/java";
    }

    @Override
    public ExecuteCodeResponse executeCode(String code, List<String> inputList) {
        return super.executeCode(code, inputList);
    }
}

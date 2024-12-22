package com.mincai.ikuncode.codesandbox.service;

import com.mincai.ikuncode.codesandbox.service.template.JavaDockerCodeSandBox;

/**
 * 代码沙箱工厂
 *
 * @author limincai
 */
public class CodeSandBoxFactory {

    public static CodeSandBox getInstance(String language) {
        switch (language) {
            case "java":
                return new JavaDockerCodeSandBox();
        }
        return null;
    }
}

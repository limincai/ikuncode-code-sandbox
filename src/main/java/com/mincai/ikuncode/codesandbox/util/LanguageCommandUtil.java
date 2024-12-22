package com.mincai.ikuncode.codesandbox.util;

import java.io.File;

/**
 * 代码命令工具类
 *
 * @author limincai
 */
public class LanguageCommandUtil {

    private static final String JAVA_COMPILE_CMD = "javac -encoding utf-8 %s";

    // private static final String JAVA_RUN = "java -Xmx512m -Dfile.encoding=UTF-8 -cp %s
    // Main";

    private static final String JAVA_RUN = "java -Xmx512m -Dfile.encoding=UTF-8 -cp %s:%s  Main";

    private static final String PYTHON_RUN = "python3 %s";

    private static final String C_PLUS_PLUS_COMPILE = "g++ %s -o %s/a.exe";

    private static final String C_PLUS_PLUS_RUN = "%s/a.exe";

    private static final String C_COMPILE = "gcc %s -o %s/a.exe";

    private static final String C_RUN = "%s/a.exe";

    private static final String JAVA_DOCKER_RUN = "docker exec -i  %s java -Dfile.encoding=UTF-8 -cp %s Main";

    private static final String PYTHON_Docker_RUN = "docker exec -i %s python3 %s";

    private static final String C_PLUS_PLUS_Docker_RUN = "docker exec -i %s %s/a.exe";

    private static final String C_Docker_RUN = "docker exec -i %s %s/a.exe";

    /**
     * 获取编译命令
     *
     * @param userCodePath   用户代码路径
     * @param userCodeParent 用户代码父路径
     * @param language       语言
     * @return 编译命令
     */
    public static String getCompileCommand(String userCodePath, String userCodeParent, String language) {
        switch (language) {
            case "java":
                return String.format(JAVA_COMPILE_CMD, userCodePath);
            case "c++":
                return String.format(C_PLUS_PLUS_COMPILE, userCodePath, userCodeParent);
            case "python":
                return null;
            case "c":
                return String.format(C_COMPILE, userCodePath, userCodeParent);
            default:
                return String.format(JAVA_COMPILE_CMD, userCodePath);
        }
    }

    /**
     * 获取执行命令
     *
     * @param userCodeParent 用户代码父路径
     * @param language       语言
     * @param containId      docker 容器 id
     * @return
     */
    public static String getRunCommand(String userCodeParent, String language, String containId) {
        switch (language) {
            case "java":
                return String.format(JAVA_DOCKER_RUN, containId, userCodeParent);
            case "c++":
                return String.format(C_PLUS_PLUS_Docker_RUN, containId, userCodeParent);
            case "python":
                return String.format(PYTHON_Docker_RUN, containId, userCodeParent + File.separator + "main.py");
            case "c":
                return String.format(C_Docker_RUN, containId, userCodeParent);
            default:
                return null;
        }
    }

}
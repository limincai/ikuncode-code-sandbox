package com.mincai.ikuncode.codesandbox.service.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.mincai.ikuncode.codesandbox.constant.ExecuteCodeResponseMessage;
import com.mincai.ikuncode.codesandbox.constant.ExecuteCodeResponseStatus;
import com.mincai.ikuncode.codesandbox.model.ExecuteCodeResponse;
import com.mincai.ikuncode.codesandbox.model.ExecuteMessage;
import com.mincai.ikuncode.codesandbox.model.QuestionJudgeInfo;
import com.mincai.ikuncode.codesandbox.service.CodeSandBox;
import com.mincai.ikuncode.codesandbox.threads.TimeManagerThread;
import com.mincai.ikuncode.codesandbox.util.LanguageCommandUtil;
import com.mincai.ikuncode.codesandbox.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 代码代码沙箱模板类
 *
 * @author limincai
 */
@Slf4j
public abstract class DockerCodeSandBoxTemplate implements CodeSandBox {

    /**
     * 代码语言
     */
    String language;

    /**
     * 全局代码文件名
     */
    String globalCodeFileName;

    /**
     * docker 容器卷名
     */
    String volumeName;


    /**
     * docker 容器
     */
    private DockerClient dockerClient;

    /**
     * docker 容器 id
     */
    private String containerId;

    /**
     * docker 镜像
     */
    String dockerImage;

    /**
     * 全局代码的保存目录名
     */
    private static final String GLOBAL_CODE_DIR_NAME = "tmpcode";


    /**
     * 执行代码超时时间
     */
    public static final int TIME_OUT = 5000;

    /**
     * 容器运行所需内存
     */
    private long memory;

    /**
     * @param code      代码
     * @param inputList 代码的输入用例
     * @return 执行代码的返回类
     */
    @Override
    public ExecuteCodeResponse executeCode(String code, List<String> inputList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<ExecuteMessage> runExecuteMessageList;

        // 1. 将代码写入到文件
        File userCodeFile = saveCodeFile(code);

        // 2. 编译保存的代码文件
        ExecuteMessage compileExecuteMessage = compileCodeFile(userCodeFile);
        // 退出码不为0，编译错误，直接返回
        if (compileExecuteMessage.getExitValue() != 0) {
            executeCodeResponse.setStatus(ExecuteCodeResponseStatus.FAILED);
            executeCodeResponse.setMessage(ExecuteCodeResponseMessage.COMPILE_FAILURE + "\n" + compileExecuteMessage.getErrorMessage());
            return executeCodeResponse;
        }

        // 3. 获取 docker 实例
        dockerClient = DockerClientBuilder.getInstance().build();

        // 4. 创建容器，获取容器 id
        containerId = createContainer(userCodeFile.getParent());

        // 5. 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 6.计算容器执行所需最大内存
        StatsCmd statsCmd = calculateMemory(containerId);

        // 7. 运行文件，得到运行信息列表
        try {
            runExecuteMessageList = runDockerFile(inputList, containerId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 停止容器
            stopContainer();
            // 删除容器
            removeContainer(statsCmd);
        }

        // 8. 获取执行输出对象
        executeCodeResponse = getOutputResponse(runExecuteMessageList);

        // 9. 清除文件
        boolean isDel = clearFile(userCodeFile);
        if (!isDel) {
            log.error("删除文件失败,用户代码路径={}", userCodeFile.getAbsolutePath());
        }

        return executeCodeResponse;
    }

    /**
     * 保存代码文件
     *
     * @return 保存后的代码文件
     */
    public File saveCodeFile(String userCode) {
        // 把用户的代码写入到文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + FileUtil.FILE_SEPARATOR + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在
        if (FileUtil.exist(globalCodePathName)) {
            // 第一次执行不存在直接创建
            FileUtil.mkdir(globalCodePathName);
        }
        // 用户代码父路径
        String userCodeParentPath = globalCodePathName + FileUtil.FILE_SEPARATOR + UUID.randomUUID();
        // 写入代码到 Main.java
        String userCodePath = userCodeParentPath + FileUtil.FILE_SEPARATOR + globalCodeFileName;
        // 返回写入的文件
        return FileUtil.writeString(userCode, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 编译代码文件
     *
     * @param userCodeFile 编译的用户代码文件
     * @return 执行信息
     */
    public ExecuteMessage compileCodeFile(File userCodeFile) {
        // javac -classpath Main
        // 编译命令
        String compileCommand = LanguageCommandUtil.getCompileCommand(userCodeFile.getAbsolutePath(), "", language);
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCommand);
            ExecuteMessage executeMessage = ProcessUtil.compileCodeFile(compileProcess);
            if (executeMessage.getExitValue() != 0) {
                throw new RuntimeException("编译错误：\n错误的原因是：" + executeMessage.getErrorMessage());
            }
            return executeMessage;
        } catch (IOException e) {
            log.error("代码编译失败,错误原因：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建容器
     */
    public String createContainer(String userCodeParentPath) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(dockerImage);
        HostConfig hostConfig = new HostConfig();
        // 设置容器内存 volumeName 与用户代码的服路径绑定
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume(volumeName)));
        // 设置可用的cpu核心数量
        hostConfig.withCpuCount(1L);
        hostConfig.withMemorySwap(0L);
        // 设置最大可用内存大小
        hostConfig.withMemory(100 * 1000 * 1000L);
        CreateContainerResponse createContainerResponse = containerCmd.withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                // 只读
                .withReadonlyRootfs(true)
                // 标准输入和输出
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        return createContainerResponse.getId();
    }

    /**
     * 计算容器执行所需最大内存
     */
    public StatsCmd calculateMemory(String containerId) {
        memory = Long.MIN_VALUE;
        // 获取容器的运行状态
        StatsCmd statsCmd = dockerClient.statsCmd(containerId);
        ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Statistics statistics) {
                log.info("内存占用：{}", statistics.getMemoryStats().getUsage());
                // 获取容器运行最大内存
                memory = Math.max(statistics.getMemoryStats().getUsage(), memory);
            }

            @Override
            public void close() throws IOException {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
            }
        });
        statsCmd.exec(statisticsResultCallback);
        return statsCmd;
    }

    /**
     * 运行 docker 中的编译后的代码
     *
     * @param inputList 代码的输入用例
     * @return 执行信息列表
     */
    public List<ExecuteMessage> runDockerFile(List<String> inputList, String containerId) throws IOException {
        long startMemory = memory;
        log.info("开始的内存---{}", memory);
        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        // 如果输出列表为空，直接获取程序的输出
        if (inputList == null || inputList.isEmpty()) {
            Runtime runtime = Runtime.getRuntime();
            String runCmd = LanguageCommandUtil.getRunCommand(volumeName, language, containerId);
            log.info("运行命令：{}", runCmd);
            Process runProcess = runtime.exec(runCmd);
            ExecuteMessage executeMessage;
            TimeManagerThread timeManagerThread = new TimeManagerThread(TIME_OUT);
            timeManagerThread.setProcess(runProcess);
            timeManagerThread.start();
            executeMessage = ProcessUtil.runDockerFile(runProcess, null);
            executeMessage.setMemory(memory);
            executeMessagesList.add(executeMessage);
            if (timeManagerThread.isTimeout()) {
                throw new RuntimeException("超时异常");
            }
            timeManagerThread.stop();
        } else {
            // 遍历输入列表
            for (String inputArgs : inputList) {
                try {
                    Runtime runtime = Runtime.getRuntime();
                    String runCmd = LanguageCommandUtil.getRunCommand(volumeName, language, containerId);
                    log.info("运行命令：{}", runCmd);
                    Process runProcess = runtime.exec(runCmd);
                    ExecuteMessage executeMessage;
                    TimeManagerThread timeManagerThread = new TimeManagerThread(TIME_OUT);
                    timeManagerThread.setProcess(runProcess);
                    timeManagerThread.start();
                    executeMessage = ProcessUtil.runDockerFile(runProcess, inputArgs);
                    executeMessage.setMemory(memory);
                    executeMessagesList.add(executeMessage);
                    // 超时删除容器
                    if (timeManagerThread.isTimeout()) {
                        stopContainer();
                        removeContainer();
                        // todo 设置运行结果
                        throw new RuntimeException("超时异常");
                    }
                    timeManagerThread.stop();
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        return executeMessagesList;
    }

    /**
     * 停止容器
     */
    public void stopContainer() {
        try {
            Runtime.getRuntime().exec(String.format("docker stop -f %s", containerId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除容器
     */
    public void removeContainer(StatsCmd statsCmd) {
        try {
            Runtime.getRuntime().exec(String.format("docker rm -f %s", containerId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        statsCmd.close();
    }

    /**
     * 删除容器
     */
    public void removeContainer() {
        try {
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取输出响应对象
     *
     * @param runExecuteMessageList 执行代码信息列表
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> runExecuteMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        QuestionJudgeInfo questionJudgeInfo = new QuestionJudgeInfo();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0L;
        long maxMemory = 0L;
        for (ExecuteMessage executeMessage : runExecuteMessageList) {
            String errorMessageStr = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessageStr)) {
                executeCodeResponse.setMessage(ExecuteCodeResponseMessage.RUN_EXCEPTION);
                executeCodeResponse.setStatus(ExecuteCodeResponseStatus.FAILED);
                break;
            }
            Long time = executeMessage.getTime();
            Long memory = executeMessage.getMemory();
            maxTime = Math.max(time, maxTime);
            maxMemory = Math.max(maxMemory, memory);
            outputList.add(executeMessage.getMessage());
        }
        if (outputList.size() == runExecuteMessageList.size()) {
            executeCodeResponse.setMessage(ExecuteCodeResponseMessage.EXECUTE_SUCCESS);
            executeCodeResponse.setStatus(ExecuteCodeResponseStatus.SUCCESS);
        }
        questionJudgeInfo.setMemory(maxMemory / 1024 / 8);
        questionJudgeInfo.setTime(maxTime);
        executeCodeResponse.setOutputList(outputList);
        executeCodeResponse.setQuestionJudgeInfo(questionJudgeInfo);
        return executeCodeResponse;
    }

    /**
     * 清除文件
     *
     * @param userCodeFile 用户代码文件
     * @return 是否删除
     */
    public boolean clearFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            return FileUtil.del(userCodeFile.getParentFile());
        }
        return true;
    }
}

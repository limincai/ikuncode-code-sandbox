package com.mincai.ikuncode.codesandbox.util;

import com.mincai.ikuncode.codesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author limincai
 */
@Slf4j
public class ProcessUtil {

    /**
     * 编译代码文件
     *
     * @return 执行信息
     */
    public static ExecuteMessage compileCodeFile(Process compileProcess) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        InputStream inputStream = null;
        InputStream errorInputStream = null;
        BufferedReader bufferedReader = null;
        BufferedReader errorBufferedReader = null;

        try {
            inputStream = compileProcess.getInputStream();
            // 获取退出码
            int exitValue = compileProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 退出码为0，表示编译成功
            if (exitValue == 0) {
                StringBuilder compileResultStr = new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileResultStr.append(compileOutputLine).append("\n");
                }
                executeMessage.setMessage(compileResultStr.toString());
            } else {
                // 退出码不为0表示编译失败
                StringBuilder compileResultStr = new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream(), "GBK"));
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileResultStr.append(compileOutputLine).append("\n");
                }
                executeMessage.setMessage(compileResultStr.toString());
                errorInputStream = compileProcess.getErrorStream();
                StringBuilder compileErrorResultStr = new StringBuilder();
                errorBufferedReader = new BufferedReader(new InputStreamReader(errorInputStream));
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    compileErrorResultStr.append(errorCompileOutputLine).append("\n");
                }
                executeMessage.setErrorMessage(compileErrorResultStr.toString());
            }
        } catch (Exception e) {
            log.error("编译失败，原因为：{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (errorInputStream != null) {
                    errorInputStream.close();
                }
                if (errorBufferedReader != null) {
                    errorBufferedReader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            compileProcess.destroy();
        }
        return executeMessage;
    }

    /**
     * 运行 docker 代码文件
     *
     * @param runProcess 运行 process
     * @param args       参数
     * @return 执行信息
     */
    public static ExecuteMessage runDockerFile(Process runProcess, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        InputStream errorInputStream = null;
        BufferedReader errorBufferedReader = null;
        try {
            if (args != null) {
                inputArgs(runProcess, args);
            }
            setTime(executeMessage, runProcess);
            setRunResult(runProcess, executeMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (errorInputStream != null) {
                    errorInputStream.close();
                }
                if (errorBufferedReader != null) {
                    errorBufferedReader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            runProcess.destroy();
        }
        return executeMessage;
    }


    /**
     * 设置运行信息
     *
     * @param executeMessage 执行信息
     */
    private static void setTime(ExecuteMessage executeMessage, Process process) throws InterruptedException {
        long start = 0L;
        long end = 0L;
        if (process.isAlive()) {
            start = System.currentTimeMillis();
            process.waitFor();
            end = System.currentTimeMillis();
        }
        executeMessage.setTime((end - start));
    }

    /**
     * 输出参数到 docker 中
     *
     * @param args 输入参数
     */
    private static void inputArgs(Process runProcess, String args) throws IOException {
        OutputStream outputStream = runProcess.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write(args + "\n");
        outputStreamWriter.flush();
    }


    /**
     * 设置程序的输出信息
     *
     * @param executeMessage 执行信息
     */
    private static void setRunResult(Process runProcess, ExecuteMessage executeMessage)
            throws IOException {

        InputStream inputStream = runProcess.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder runOutputStringBuilder = new StringBuilder();
        String runOutputLine;
        while ((runOutputLine = bufferedReader.readLine()) != null) {
            runOutputStringBuilder.append(runOutputLine);
        }
        executeMessage.setMessage(runOutputStringBuilder.toString());

        InputStream errorInputStream = runProcess.getErrorStream();
        bufferedReader = new BufferedReader(new InputStreamReader(errorInputStream));
        StringBuilder errorRunStringBuilder = new StringBuilder();
        String errorRunOutputLine;
        while ((errorRunOutputLine = bufferedReader.readLine()) != null) {
            errorRunStringBuilder.append(errorRunOutputLine);
        }
        executeMessage.setErrorMessage(errorRunStringBuilder.toString());
    }
}

package com.mincai.ikuncode.codesandbox.constant;

/**
 * 运行代码信息常量
 *
 * @author limincai
 */
public interface ExecuteCodeResponseMessage {

    /**
     * 编译失败
     */
    String COMPILE_FAILURE = "编译失败";

    /**
     * 运行异常
     */
    String RUN_EXCEPTION = "运行异常";

    /**
     * 执行成功
     */
    String EXECUTE_SUCCESS = "执行成功";

    /**
     * 超时
     */
    String OUT_OF_TIME = "超时";
}

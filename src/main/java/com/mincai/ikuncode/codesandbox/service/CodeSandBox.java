package com.mincai.ikuncode.codesandbox.service;

import com.mincai.ikuncode.codesandbox.model.ExecuteCodeResponse;

import java.util.List;

/**
 * @author limincai
 */
public interface CodeSandBox {

    /**
     * 执行代码
     *
     * @param code      代码
     * @param inputList 代码的输入用例
     * @return 执行代码返回类
     */
    ExecuteCodeResponse executeCode(String code, List<String> inputList);
}

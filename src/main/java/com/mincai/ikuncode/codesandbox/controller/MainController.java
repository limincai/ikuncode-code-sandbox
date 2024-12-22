package com.mincai.ikuncode.codesandbox.controller;

import cn.hutool.core.util.StrUtil;
import com.mincai.ikuncode.codesandbox.enums.CodeLanguageEnum;
import com.mincai.ikuncode.codesandbox.model.ExecuteCodeRequest;
import com.mincai.ikuncode.codesandbox.model.ExecuteCodeResponse;
import com.mincai.ikuncode.codesandbox.service.CodeSandBoxFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author limincai
 */
@RestController
@RequestMapping
public class MainController {


    /**
     * 执行代码
     */
    @PostMapping("execute-code")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        String language = executeCodeRequest.getLanguage();
        CodeLanguageEnum enumByValue = CodeLanguageEnum.getEnumByValue(language);
        if (enumByValue == null) {
            throw new RuntimeException("语言不支持");
        }
        if (StrUtil.isEmpty(executeCodeRequest.getCode())) {
            throw new RuntimeException("代码为空");
        }
        return CodeSandBoxFactory.getInstance(language).executeCode(executeCodeRequest.getCode(), executeCodeRequest.getInputList());
    }
}

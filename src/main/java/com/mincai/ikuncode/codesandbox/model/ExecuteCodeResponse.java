package com.mincai.ikuncode.codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author limincai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse implements Serializable {

    /**
     * 运行代码的输出用例
     */
    private List<String> outputList;

    /**
     * 代码的运行信息
     */
    private String message;

    /**
     * 代码的运行状态
     */
    private String status;

    /**
     * 判题信息
     */
    private QuestionJudgeInfo questionJudgeInfo;


    private static final long serialVersionUID = 1L;
}

package com.chickling.face;

import com.chickling.bean.result.ResultMap;

/**
 * Created by gl08 on 2017/4/12.
 */
public interface PrestoResult {
    ResultMap getPrestoResult(String sql);
}

package com.campus.job.util;

import com.campus.job.entity.SysJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 定时任务执行（禁止并发执行）
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends com.campus.job.util.AbstractQuartzJob {
    
    @Override
    protected void doExecute(JobExecutionContext context, SysJob job) throws Exception {
        com.campus.job.util.JobInvokeUtil.invokeMethod(job);
    }
}

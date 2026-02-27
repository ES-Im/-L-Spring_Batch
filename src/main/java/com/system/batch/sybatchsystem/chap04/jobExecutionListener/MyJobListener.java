package com.system.batch.sybatchsystem.chap04.jobExecutionListener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[Job 시작] id : {}", jobExecution.getJobInstanceId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.FAILED) {
            log.warn("[Job 실패] 관리자에게 연락");
        } else {
            log.info("[Job 종료] 정상적으로 배치 실행");
        }
    }
}

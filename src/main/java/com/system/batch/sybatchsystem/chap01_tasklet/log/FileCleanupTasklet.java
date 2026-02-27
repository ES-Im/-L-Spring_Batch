package com.system.batch.sybatchsystem.chap01_tasklet.log;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.io.File;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
public class FileCleanupTasklet implements Tasklet {

    private final String rootPath;
    private final int retentionDays;

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution,
                                          ChunkContext chunkContext) throws Exception {
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);    // 오늘날짜 - 보관일자
        File folder = new File(rootPath);
        File[] files = folder.listFiles();

        if(files == null) return RepeatStatus.FINISHED;

        for (File file : files) {
            String name = file.getName();

            if(name.endsWith(".log")) {
                // access_2026-01-08.log => '2026-01-08' 추출
                String dateStr = name.substring(name.lastIndexOf("_") + 1, name.lastIndexOf("."));
                LocalDate fileDate = LocalDate.parse(dateStr);

                if(fileDate.isBefore(cutoffDate)) {
                    file.delete();
                    log.info("삭제된 로그 파일 : {}", name);
                }
            }
        }

        return RepeatStatus.FINISHED;
    }

}

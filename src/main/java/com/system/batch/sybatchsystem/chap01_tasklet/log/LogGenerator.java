package com.system.batch.sybatchsystem.chap01_tasklet.log;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class LogGenerator {

    private static final String ROOT_PATH = "./test-logs";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) throws IOException {
        File dir = new File(ROOT_PATH);

        if(!dir.exists()){
            dir.mkdir();
        }

        createLogFile(dir, "access", 2);    // 2일 전
        createLogFile(dir, "access", 0);    // 0일 전
        createLogFile(dir, "access", 50);    // 50일 전
        createLogFile(dir, "access", 100);    // 100일 전
        createLogFile(dir, "system_config.conf", -1);    // 날짜 형식 x

        log.info("테스트용 로그 생성 완료 [경로 : {}]", ROOT_PATH);

    }

    private static void createLogFile(File dir, String prefix, int day) throws IOException {
        String filename;

        if(day == -1) {
            filename = prefix;
        } else {
            LocalDate targetDate = LocalDate.now().minusDays(day);
            String dateStr = targetDate.format(DATE_FORMATTER);
            filename = prefix + "_" + dateStr + ".log";
        }

        File file = new File(dir, filename);
        if(file.createNewFile()) {
            log.info("파일 생성, {}", filename);
        } else {
            log.info("이미 존재하는 파일");
        }
    }
}

